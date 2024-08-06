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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.graphs.ads.ADSNode;
import net.automatalib.graphs.ads.impl.ADSLeafNode;
import net.automatalib.words.Alphabet;

/**
 * General purpose facade for computing adaptive distinguishing sequences. Delegates to {@link LeeYannakakis}, {@link
 * BacktrackingSearch} (non-optimal) and {@link StateEquivalence} for the actual computation of the ADS.
 *
 * @author frohme
 */
public final class ADS {

    private ADS() {}

    /**
     * Compute an adaptive distinguishing sequence for the given automaton and the given set of states.
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
     */
    public static <S, I, O> Optional<ADSNode<S, I, O>> compute(final MealyMachine<S, I, ?, O> automaton,
                                                               final Alphabet<I> input,
                                                               final Set<S> states) {

        if (states.isEmpty()) {
            return Optional.empty();
        } else if (states.size() == 1) {
            final S singleState = states.iterator().next();
            return Optional.of(new ADSLeafNode<>(null, singleState));
        } else if (states.size() == 2) {
            return StateEquivalence.compute(automaton, input, states);
        } else if (states.size() == automaton.getStates().size()) {
            final LYResult<S, I, O> result = LeeYannakakis.compute(automaton, input);
            return Optional.ofNullable(result.get());
        } else {
            return BacktrackingSearch.compute(automaton, input, states);
        }
    }

    /**
     * See {@link #compute(MealyMachine, Alphabet, Set)}. Internal version that uses a {@link SplitTree} for state
     * tracking.
     *
     * @throws IllegalArgumentException
     *         if <ul>
     *         <li>the partition of the provided node is empty</li>
     *         <li>the partition of the provided node contains a single element, which is not mapped in the node's
     *         mapping</li>
     *         <li>the partition of the provided node contains as many states as the automaton</li>
     *         </ul>
     */
    static <S, I, O> Optional<ADSNode<S, I, O>> compute(final MealyMachine<S, I, ?, O> automaton,
                                                        final Alphabet<I> input,
                                                        final SplitTree<S, I, O> node) {

        if (node.getPartition().isEmpty()) {
            throw new IllegalArgumentException("Empty partitions should be handled by the specific algorithm");
        } else if (node.getPartition().size() == 1) {

            final S state = node.getPartition().iterator().next();
            final Map<S, S> mapping = node.getMapping();

            if (!mapping.containsKey(state)) {
                throw new IllegalStateException();
            }

            final ADSNode<S, I, O> result = new ADSLeafNode<>(null, mapping.get(state));
            return Optional.of(result);
        } else if (node.getPartition().size() == 2) {
            return StateEquivalence.compute(automaton, input, node);
        } else if (node.getPartition().size() == automaton.getStates().size()) {
            throw new IllegalArgumentException("Call LeeYannakakis directly");
        } else {
            return BacktrackingSearch.compute(automaton, input, node);
        }
    }
}
