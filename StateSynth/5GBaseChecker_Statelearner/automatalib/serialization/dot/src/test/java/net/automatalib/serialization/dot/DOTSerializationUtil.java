/* Copyright (C) 2013-2022 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.serialization.dot;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.automata.transducers.impl.compact.CompactSST;
import net.automatalib.graphs.base.DefaultCFMPS;
import net.automatalib.graphs.base.compact.CompactGraph;
import net.automatalib.graphs.base.compact.CompactPMPG;
import net.automatalib.graphs.base.compact.CompactPMPGEdge;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.transition.ModalContractEdgeProperty.EdgeColor;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import net.automatalib.ts.modal.transition.ProceduralModalEdgePropertyImpl;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
final class DOTSerializationUtil {

    static final String EMPTY_RESOURCE = "/empty.dot";
    static final String DFA_RESOURCE = "/dfa.dot";
    static final String NFA_RESOURCE = "/nfa.dot";
    static final String NFA2_RESOURCE = "/nfa2.dot";
    static final String MEALY_RESOURCE = "/mealy.dot";
    static final String MOORE_RESOURCE = "/moore.dot";
    static final String SST_RESOURCE = "/sst.dot";
    static final String GRAPH_RESOURCE = "/graph.dot";
    static final String GRAPH_GLOBAL_RESOURCE = "/graph_global.dot";
    static final String GRAPH_HTML_RESOURCE = "/graph_html.dot";
    static final String MTS_RESOURCE = "/mts.dot";
    static final String MC_RESOURCE = "/mc.dot";
    static final String CLUSTER_RESOURCE = "/cluster.dot";
    static final String PMPG_RESOURCE = "/pmpg.dot";
    static final String CFMPS_RESOURCE = "/cfmps.dot";

    static final String FAULTY_AUTOMATON_RESOURCE = "/faulty_automaton.dot";
    static final String FAULTY_GRAPH_RESOURCE = "/faulty_graph.dot";

    static final String PARSER_RESOURCE = "/parser.dot";

    static final Alphabet<String> STRING_ALPHABET;
    static final Alphabet<String> INTEGER_ALPHABET;

    static final CompactDFA<String> DFA;
    static final CompactNFA<String> NFA;
    static final CompactMealy<String, String> MEALY;
    static final CompactMoore<String, String> MOORE;
    static final CompactSST<Character, Character> SST;
    static final CompactGraph<String, String> GRAPH;
    static final CompactMTS<String> MTS;
    static final CompactMC<String> MC;
    static final DefaultCFMPS<Character, Character> CFMPS;

    static {
        STRING_ALPHABET = Alphabets.closedCharStringRange('a', 'c');
        INTEGER_ALPHABET = Alphabets.closedCharStringRange('1', '2');

        DFA = buildDFA();
        NFA = buildNFA();
        MEALY = buildMealy();
        MOORE = buildMoore();
        SST = buildSST();
        GRAPH = buildGraph();
        MTS = buildMTS();
        MC = buildMC();
        CFMPS = buildCFMPS();
    }

    private DOTSerializationUtil() {}

    static URL getResource(String resource) {
        return DOTSerializationUtil.class.getResource(resource);
    }

    private static CompactDFA<String> buildDFA() {
        final CompactDFA<String> result = new CompactDFA<>(STRING_ALPHABET);

        final Integer s0 = result.addInitialState();
        final Integer s1 = result.addState();
        final Integer s2 = result.addState(true);

        result.addTransition(s0, "a", s1);
        result.addTransition(s1, "b", s2);
        result.addTransition(s2, "c", s0);

        return result;
    }

    private static CompactNFA<String> buildNFA() {
        final CompactNFA<String> result = new CompactNFA<>(STRING_ALPHABET);

        final Integer s0 = result.addInitialState(true);
        final Integer s1 = result.addState();
        final Integer s2 = result.addState();

        result.addTransition(s0, "a", s1);
        result.addTransition(s0, "a", s2);
        result.addTransition(s1, "b", s0);
        result.addTransition(s1, "b", s2);
        result.addTransition(s2, "c", s0);
        result.addTransition(s2, "c", s1);

        return result;
    }

    private static CompactMealy<String, String> buildMealy() {
        final CompactMealy<String, String> result = new CompactMealy<>(STRING_ALPHABET);

        final Integer s0 = result.addInitialState();
        final Integer s1 = result.addState();
        final Integer s2 = result.addState();

        result.addTransition(s0, "a", s1, "1");
        result.addTransition(s1, "b", s2, "2");
        result.addTransition(s2, "c", s0, "3");

        return result;
    }

    private static CompactMoore<String, String> buildMoore() {
        final CompactMoore<String, String> result = new CompactMoore<>(INTEGER_ALPHABET);

        final Integer s0 = result.addInitialState("a");
        final Integer s1 = result.addState("b");
        final Integer s2 = result.addState("c");

        result.addTransition(s0, "1", s1);
        result.addTransition(s0, "2", s2);
        result.addTransition(s1, "2", s0);
        result.addTransition(s1, "1", s2);
        result.addTransition(s2, "1", s0);
        result.addTransition(s2, "2", s1);

        return result;
    }

    private static CompactSST<Character, Character> buildSST() {
        final CompactSST<Character, Character> result = new CompactSST<>(Alphabets.characters('a', 'c'));

        final Integer s0 = result.addInitialState(Word.fromCharSequence("x"));
        final Integer s1 = result.addState(Word.epsilon());
        final Integer s2 = result.addState(Word.epsilon());
        final Integer s3 = result.addState(Word.epsilon());

        result.addTransition(s0, 'a', s1, Word.fromCharSequence("x"));
        result.addTransition(s0, 'b', s1, Word.fromCharSequence("x"));
        result.addTransition(s0, 'c', s1, Word.fromCharSequence("x"));

        result.addTransition(s1, 'a', s2, Word.fromCharSequence("xx"));
        result.addTransition(s1, 'b', s2, Word.fromCharSequence("yx"));
        result.addTransition(s1, 'c', s2, Word.fromCharSequence("zx"));

        result.addTransition(s2, 'a', s3, Word.fromCharSequence("xx"));
        result.addTransition(s2, 'b', s3, Word.fromCharSequence("yx"));
        result.addTransition(s2, 'c', s3, Word.fromCharSequence("zx"));

        result.addTransition(s3, 'a', s1, Word.epsilon());
        result.addTransition(s3, 'b', s1, Word.epsilon());
        result.addTransition(s3, 'c', s1, Word.epsilon());

        return result;
    }

    private static CompactGraph<String, String> buildGraph() {
        final CompactGraph<String, String> result = new CompactGraph<>();
        result.addIntNode();

        for (int i = 2; i < 2 << 3; i++) {
            result.addIntNode();
            result.connect((i / 2 + i % 2) - 1, i - 1, Integer.toString(i - 1));
        }

        // update to unique labels "<id>_<level>"
        for (Integer n : result) {
            result.setNodeProperty(n, n.toString() + '_' + (Integer.SIZE - Integer.numberOfLeadingZeros(n + 1)));
        }

        return result;
    }

    private static CompactMTS<String> buildMTS() {
        final CompactMTS<String> result = new CompactMTS<>(STRING_ALPHABET);
        final Integer s0 = result.addInitialState();
        final Integer s1 = result.addState();
        final Integer s2 = result.addState();

        result.addModalTransition(s0, "a", s0, ModalType.MAY);
        result.addModalTransition(s0, "b", s1, ModalType.MUST);
        result.addModalTransition(s0, "c", s2, ModalType.MAY);

        result.addModalTransition(s1, "a", s0, ModalType.MUST);
        result.addModalTransition(s1, "b", s1, ModalType.MAY);
        result.addModalTransition(s1, "c", s2, ModalType.MUST);

        result.addModalTransition(s2, "a", s0, ModalType.MAY);
        result.addModalTransition(s2, "b", s1, ModalType.MUST);
        result.addModalTransition(s2, "c", s2, ModalType.MAY);

        return result;
    }

    private static CompactMC<String> buildMC() {
        final CompactMC<String> result = new CompactMC<>(STRING_ALPHABET, STRING_ALPHABET);
        final Integer s0 = result.addInitialState();
        final Integer s1 = result.addState();
        final Integer s2 = result.addState();

        result.addContractTransition(s0, "a", s0, ModalType.MAY, false, EdgeColor.NONE);
        result.addContractTransition(s0, "b", s1, ModalType.MUST, false, EdgeColor.RED);
        result.addContractTransition(s0, "c", s2, ModalType.MAY, false, EdgeColor.GREEN);

        result.addContractTransition(s1, "a", s0, ModalType.MUST, false, EdgeColor.RED);
        result.addContractTransition(s1, "b", s1, ModalType.MAY, false, EdgeColor.NONE);
        result.addContractTransition(s1, "c", s2, ModalType.MUST, false, EdgeColor.GREEN);

        result.addContractTransition(s2, "a", s0, ModalType.MAY, false, EdgeColor.GREEN);
        result.addContractTransition(s2, "b", s1, ModalType.MUST, false, EdgeColor.RED);
        result.addContractTransition(s2, "c", s2, ModalType.MAY, false, EdgeColor.NONE);

        return result;
    }

    private static DefaultCFMPS<Character, Character> buildCFMPS() {
        final ProceduralModalEdgePropertyImpl p1 =
                new ProceduralModalEdgePropertyImpl(ProceduralType.INTERNAL, ModalType.MUST);
        final ProceduralModalEdgePropertyImpl p2 =
                new ProceduralModalEdgePropertyImpl(ProceduralType.INTERNAL, ModalType.MAY);
        final ProceduralModalEdgePropertyImpl p3 =
                new ProceduralModalEdgePropertyImpl(ProceduralType.PROCESS, ModalType.MUST);
        final ProceduralModalEdgePropertyImpl p4 =
                new ProceduralModalEdgePropertyImpl(ProceduralType.PROCESS, ModalType.MAY);

        final CompactPMPG<Character, Character> s = new CompactPMPG<>('?');
        final int s0 = s.addIntNode();
        final int s1 = s.addIntNode(Sets.newHashSet('a', 'b'));
        final int s2 = s.addIntNode(Collections.singleton('c'));

        final CompactPMPGEdge<Character, MutableProceduralModalEdgeProperty> e1 = s.connect(s0, s1, p1);
        final CompactPMPGEdge<Character, MutableProceduralModalEdgeProperty> e2 = s.connect(s0, s1, p2);
        final CompactPMPGEdge<Character, MutableProceduralModalEdgeProperty> e3 = s.connect(s1, s2, p3);
        final CompactPMPGEdge<Character, MutableProceduralModalEdgeProperty> e4 = s.connect(s2, s0, p4);

        s.setEdgeLabel(e1, '1');
        s.setEdgeLabel(e2, '2');
        s.setEdgeLabel(e3, '3');
        s.setEdgeLabel(e4, '4');
        s.setInitialNode(s0);

        final CompactPMPG<Character, Character> t = new CompactPMPG<>('?');
        final int t0 = t.addIntNode();
        final int t1 = t.addIntNode(Collections.singleton('d'));

        t.connect(t0, t1, p1);
        t.connect(t0, t1, p2);
        t.connect(t1, t1, p3);
        t.connect(t1, t0, p4);

        final Map<Character, CompactPMPG<Character, Character>> pmpgs = Maps.newHashMapWithExpectedSize(2);
        pmpgs.put('s', s);
        pmpgs.put('t', t);

        return new DefaultCFMPS<>('s', pmpgs);
    }
}
