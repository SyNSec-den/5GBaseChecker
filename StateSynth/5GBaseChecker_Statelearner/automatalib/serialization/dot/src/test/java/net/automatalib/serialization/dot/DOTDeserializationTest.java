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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.commons.util.io.UnclosableInputStream;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.serialization.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class DOTDeserializationTest {

    @Test
    public void testRegularDFADeserialization() throws IOException {

        final CompactDFA<String> dfa = DOTSerializationUtil.DFA;

        final DFA<?, String> parsed =
                DOTParsers.dfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.DFA_RESOURCE)).
                        model;

        checkIsomorphism(dfa, parsed, dfa.getInputAlphabet());
    }

    @Test
    public void testRegularNFADeserialization() throws IOException {

        final CompactNFA<String> nfa = DOTSerializationUtil.NFA;

        final CompactNFA<String> parsed =
                DOTParsers.nfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.NFA_RESOURCE)).
                        model;

        checkIsomorphism(nfa, parsed, nfa.getInputAlphabet());
    }

    @Test
    public void testRegularNFA2Deserialization() throws IOException {

        final CompactNFA<String> parsed = DOTParsers.fsa(new CompactNFA.Creator<>(),
                                                         DOTParsers.DEFAULT_FSA_NODE_PARSER,
                                                         DOTParsers.DEFAULT_EDGE_PARSER,
                                                         Arrays.asList("s0", "s1", "s2"),
                                                         false)
                                                    .readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.NFA2_RESOURCE)).
                model;

        Assert.assertEquals(3, parsed.size());
        Assert.assertEquals(3, parsed.getInitialStates().size());
        Assert.assertFalse(parsed.accepts(Word.fromSymbols("a", "a", "a")));
        Assert.assertFalse(parsed.accepts(Word.fromSymbols("b", "b")));
        Assert.assertFalse(parsed.accepts(Word.fromSymbols("c")));
        Assert.assertEquals(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("a", "a", "a")).size(), 1);
        Assert.assertEquals(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("b", "b")).size(), 1);
        Assert.assertEquals(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("c")).size(), 1);
        Assert.assertTrue(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("a", "b")).isEmpty());
        Assert.assertTrue(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("c", "a")).isEmpty());
        Assert.assertTrue(parsed.getSuccessors(parsed.getInitialStates(), Word.fromSymbols("b", "c")).isEmpty());
    }

    @Test
    public void testRegularMealyDeserialization() throws IOException {

        final CompactMealy<String, String> mealy = DOTSerializationUtil.MEALY;

        final MealyMachine<?, String, ?, String> parsed =
                DOTParsers.mealy().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MEALY_RESOURCE)).
                        model;

        checkIsomorphism(mealy, parsed, mealy.getInputAlphabet());
    }

    @Test
    public void testRegularMooreDeserialization() throws IOException {
        final CompactMoore<String, String> moore = DOTSerializationUtil.MOORE;

        final MooreMachine<?, String, ?, String> parsed =
                DOTParsers.moore().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MOORE_RESOURCE)).
                        model;

        checkIsomorphism(moore, parsed, moore.getInputAlphabet());
    }

    @Test
    public void testRegularGraphDeserialization() throws IOException {
        final UniversalGraph<?, ?, String, String> graph = DOTSerializationUtil.GRAPH;

        final UniversalGraph<?, ?, String, String> parsed =
                DOTParsers.graph().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.GRAPH_RESOURCE));

        checkGraphEquivalence(graph, parsed);
    }

    @Test
    public void testRegularMTSDeserialization() throws IOException {
        final CompactMTS<String> mts = DOTSerializationUtil.MTS;

        InputModelData<String, CompactMTS<String>> model =
                DOTParsers.mts().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MTS_RESOURCE));

        final Alphabet<String> alphabet = model.alphabet;
        final CompactMTS<String> parsed = model.model;

        checkIsomorphism(mts, parsed, alphabet);
    }

    @Test
    public void testRegularMCDeserialization() throws IOException {
        final CompactMC<String> mc = DOTSerializationUtil.MC;

        InputModelData<String, CompactMC<String>> model =
                DOTParsers.mc().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MC_RESOURCE));

        final Alphabet<String> alphabet = model.alphabet;
        final CompactMC<String> parsed = model.model;

        checkIsomorphism(mc, parsed, alphabet);
    }

    @Test(expectedExceptions = FormatException.class)
    public void testFaultyAutomatonDeserialization() throws IOException {
        DOTParsers.dfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.FAULTY_AUTOMATON_RESOURCE));
    }

    @Test(expectedExceptions = FormatException.class)
    public void testFaultyGraphDeserialization() throws IOException {
        DOTParsers.graph().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.FAULTY_GRAPH_RESOURCE));
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException {
        try (InputStream dfa = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.DFA_RESOURCE);
             InputStream nfa = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.NFA_RESOURCE);
             InputStream graph = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.GRAPH_RESOURCE);
             InputStream mealy = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.MEALY_RESOURCE);
             InputStream moore = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.MOORE_RESOURCE)) {
            DOTParsers.dfa().readModel(new UnclosableInputStream(dfa));
            DOTParsers.nfa().readModel(new UnclosableInputStream(nfa));
            DOTParsers.graph().readModel(new UnclosableInputStream(graph));
            DOTParsers.mealy().readModel(new UnclosableInputStream(mealy));
            DOTParsers.moore().readModel(new UnclosableInputStream(moore));
        }
    }

    @Test
    public void testDuplicateTransitions() throws IOException {

        final CompactDFA<String> dfa = new CompactDFA<>(Alphabets.closedCharStringRange('a', 'b'));
        final Integer s0 = dfa.addInitialState();
        final Integer s1 = dfa.addState(true);

        dfa.addTransition(s0, "a", s1);
        dfa.addTransition(s0, "b", s1);
        dfa.addTransition(s1, "a", s0);
        dfa.addTransition(s1, "b", s0);

        final StringWriter w = new StringWriter();
        GraphDOT.write(dfa, dfa.getInputAlphabet(), w);

        final ByteArrayInputStream bais = new ByteArrayInputStream(w.toString().getBytes());
        final DFA<?, String> parsed = DOTParsers.dfa().readModel(bais).model;

        checkIsomorphism(dfa, parsed, dfa.getInputAlphabet());
    }

    private static <S1, S2, I, T1, T2, SP, TP> void checkIsomorphism(UniversalAutomaton<S1, I, T1, SP, TP> source,
                                                                     UniversalAutomaton<S2, I, T2, SP, TP> target,
                                                                     Alphabet<I> alphabet) {

        Assert.assertEquals(source.size(), target.size());

        final Queue<S1> sourceQueue = new ArrayDeque<>(source.getInitialStates());
        final Queue<S2> targetQueue = new ArrayDeque<>(target.getInitialStates());

        Assert.assertEquals(sourceQueue.size(), 1, "This check currently only works for single initial states");
        Assert.assertEquals(targetQueue.size(), 1, "This check currently only works for single initial states");

        final Set<S1> sourceCache = new HashSet<>();
        final Set<S2> targetCache = new HashSet<>();

        while (!sourceQueue.isEmpty() && !targetQueue.isEmpty()) {
            S1 sourceState = sourceQueue.remove();
            S2 targetState = targetQueue.remove();

            Assert.assertEquals(source.getStateProperty(sourceState), target.getStateProperty(targetState));

            sourceCache.add(sourceState);
            targetCache.add(targetState);

            for (I i : alphabet) {
                final List<T1> sourceTransitions = new ArrayList<>(source.getTransitions(sourceState, i));
                final List<T2> targetTransistions = new ArrayList<>(target.getTransitions(targetState, i));

                Assert.assertEquals(sourceTransitions.size(), targetTransistions.size());

                for (int j = 0; j < sourceTransitions.size(); j++) {
                    final T1 sourceTrans = sourceTransitions.get(j);
                    final T2 targetTrans = targetTransistions.get(j);

                    Assert.assertEquals(source.getTransitionProperty(sourceTrans),
                                        target.getTransitionProperty(targetTrans));

                    final S1 sourceSucc = source.getSuccessor(sourceTrans);
                    final S2 targetSucc = target.getSuccessor(targetTrans);

                    Assert.assertEquals(sourceCache.contains(sourceSucc), targetCache.contains(targetSucc));

                    if (!sourceCache.contains(sourceSucc)) {
                        sourceQueue.add(sourceSucc);
                        sourceCache.add(sourceSucc);
                        targetQueue.add(targetSucc);
                        targetCache.add(targetSucc);
                    }
                }
            }
        }

        Assert.assertEquals(sourceQueue.isEmpty(), targetQueue.isEmpty());
    }

    private static <N1, E1, NP extends Comparable<NP>, EP extends Comparable<EP>, N2, E2> void checkGraphEquivalence(
            UniversalGraph<N1, E1, NP, EP> source,
            UniversalGraph<N2, E2, NP, EP> target) {

        Assert.assertEquals(source.size(), target.size());

        final Queue<N1> sourceQueue = new ArrayDeque<>();
        final Queue<N2> targetQueue = new ArrayDeque<>();

        // since the node ids are parsed natural order, the first nodes are equal
        sourceQueue.add(source.nodeIDs().getNode(0));
        targetQueue.add(target.nodeIDs().getNode(0));

        while (!sourceQueue.isEmpty() && !targetQueue.isEmpty()) {
            N1 sourceNode = sourceQueue.remove();
            N2 targetNode = targetQueue.remove();

            Assert.assertEquals(source.getNodeProperty(sourceNode), target.getNodeProperty(targetNode));

            final List<E1> sourceEdges = new ArrayList<>(source.getOutgoingEdges(sourceNode));
            final List<E2> targetEdges = new ArrayList<>(target.getOutgoingEdges(targetNode));

            Assert.assertEquals(sourceEdges.size(), targetEdges.size());

            // since we have unique node properties, these uniquely identify states
            sourceEdges.sort(Comparator.comparing(e -> source.getNodeProperty(source.getTarget(e))));
            targetEdges.sort(Comparator.comparing(e -> target.getNodeProperty(target.getTarget(e))));

            for (int j = 0; j < sourceEdges.size(); j++) {
                final E1 sourceEdge = sourceEdges.get(j);
                final E2 targetEdge = targetEdges.get(j);

                Assert.assertEquals(source.getEdgeProperty(sourceEdge), target.getEdgeProperty(targetEdge));
                Assert.assertEquals(source.getNodeProperty(source.getTarget(sourceEdge)),
                                    target.getNodeProperty(target.getTarget(targetEdge)));
            }
        }

        Assert.assertEquals(sourceQueue.isEmpty(), targetQueue.isEmpty());
    }
}
