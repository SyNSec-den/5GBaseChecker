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
package net.automatalib.examples.vpda;

import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultVPDAlphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A small example for constructing a {@link net.automatalib.automata.vpda.OneSEVPA visibly pushdown automaton} and
 * displaying it.
 *
 * @author frohme
 */
public final class OneSEVPAExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(OneSEVPAExample.class);
    private static final VPDAlphabet<Character> ALPHABET =
            new DefaultVPDAlphabet<>(Alphabets.singleton('i'), Alphabets.singleton('c'), Alphabets.singleton('r'));

    private OneSEVPAExample() {}

    public static void main(String[] args) {

        final DefaultOneSEVPA<Character> vpa = buildAutomaton();

        traceVisiblePushdownWords(vpa, "i");
        traceVisiblePushdownWords(vpa, "cir");
        traceVisiblePushdownWords(vpa, "cccirrr");
        traceVisiblePushdownWords(vpa, "cirrr");
        traceVisiblePushdownWords(vpa, "cccir");
        traceVisiblePushdownWords(vpa, "cciiirr");
        traceVisiblePushdownWords(vpa, "cirircr");

        Visualization.visualize(vpa);
    }

    private static void traceVisiblePushdownWords(DefaultOneSEVPA<Character> vpa, String input) {
        final boolean accept = vpa.accepts(Word.fromCharSequence(input));

        LOGGER.info("The VPA does {}accept the word '{}'", accept ? "" : "not ", input);
    }

    /**
     * Returns a visibly pushdown automaton accepting the language {c<sup>m</sup>i<sup>2n+1</sup>r<sup>m</sup> | m,n \in N}.
     *
     * @return a visibly pushdown automaton accepting the language {c<sup>m</sup>ir<sup>m</sup> | m \in N}
     *
     * @see #ALPHABET
     */
    private static DefaultOneSEVPA<Character> buildAutomaton() {

        final DefaultOneSEVPA<Character> result = new DefaultOneSEVPA<>(ALPHABET);

        final Location l0 = result.addInitialLocation(false);
        final Location l1 = result.addLocation(true);

        result.setInternalSuccessor(l0, 'i', l1);
        result.setInternalSuccessor(l1, 'i', l0);

        result.setReturnSuccessor(l0, 'r', result.encodeStackSym(l0, (Character) 'c'), l0);
        result.setReturnSuccessor(l0, 'r', result.encodeStackSym(l1, (Character) 'c'), l0);

        result.setReturnSuccessor(l1, 'r', result.encodeStackSym(l0, (Character) 'c'), l1);
        result.setReturnSuccessor(l1, 'r', result.encodeStackSym(l1, (Character) 'c'), l0);

        return result;
    }
}
