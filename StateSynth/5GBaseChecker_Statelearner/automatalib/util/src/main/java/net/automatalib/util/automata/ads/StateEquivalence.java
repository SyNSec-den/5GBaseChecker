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
package net.automatalib.util.automata.ads;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.smartcollections.ReflexiveMapView;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.ads.ADSNode;
import net.automatalib.graphs.ads.impl.ADSLeafNode;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * A utility class for computing an adaptive distinguishing sequence by means of solving the state equivalence problems,
 * i.e. computing and ADS for two states only.
 *
 * @author frohme
 */
public final class StateEquivalence {

    private StateEquivalence() {}

    /**
     * Computes a two-state ADS by using {@link Automata#findSeparatingWord(UniversalDeterministicAutomaton,
     * UniversalDeterministicAutomaton, Collection)}.
     *
     * @param automaton
     *         the automaton for which an ADS should be computed
     * @param input
     *         the input alphabet of the automaton
     * @param states
     *         the set of states which should be distinguished by the computed ADS
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return {@code Optional.empty()} if there exists no ADS that distinguishes the given states, a valid ADS
     * otherwise.
     *
     * @throws IllegalArgumentException
     *         if passed anything other than two states.
     */
    public static <S, I, O> Optional<ADSNode<S, I, O>> compute(final MealyMachine<S, I, ?, O> automaton,
                                                               final Alphabet<I> input,
                                                               final Set<S> states) {

        if (states.size() != 2) {
            throw new IllegalArgumentException("StateEquivalence can only distinguish 2 states");
        }

        final SplitTree<S, I, O> node = new SplitTree<>(states, new ReflexiveMapView<>(states));

        return compute(automaton, input, node);
    }

    /**
     * See {@link #compute(MealyMachine, Alphabet, Set)}. Internal version, that uses the {@link SplitTree}
     * representation.
     */
    static <S, I, O> Optional<ADSNode<S, I, O>> compute(final MealyMachine<S, I, ?, O> automaton,
                                                        final Alphabet<I> input,
                                                        final SplitTree<S, I, O> node) {

        final Iterator<S> targetStateIterator = node.getPartition().iterator();
        final S s1 = targetStateIterator.next();
        final S s2 = targetStateIterator.next();

        final Word<I> separatingWord = Automata.findSeparatingWord(automaton, s1, s2, input);

        // sep word may be non existent, if current hypothesis is not consistent
        if (separatingWord == null) {
            return Optional.empty();
        }

        final Word<O> s1Output = automaton.computeStateOutput(s1, separatingWord);
        final Word<O> s2Output = automaton.computeStateOutput(s2, separatingWord);
        final Word<O> sharedOutput = s1Output.longestCommonPrefix(s2Output);
        final Word<I> trace = separatingWord.prefix(sharedOutput.length() + 1);

        final Pair<ADSNode<S, I, O>, ADSNode<S, I, O>> ads = ADSUtil.buildFromTrace(automaton, trace, s1);

        final ADSNode<S, I, O> head = ads.getFirst();
        final ADSNode<S, I, O> tail = ads.getSecond();

        final ADSNode<S, I, O> s1FinalNode = new ADSLeafNode<>(tail, node.getMapping().get(s1));
        final ADSNode<S, I, O> s2FinalNode = new ADSLeafNode<>(tail, node.getMapping().get(s2));

        tail.getChildren().put(s1Output.getSymbol(sharedOutput.length()), s1FinalNode);
        tail.getChildren().put(s2Output.getSymbol(sharedOutput.length()), s2FinalNode);

        return Optional.of(head);
    }
}
