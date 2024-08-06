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

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.smartcollections.ReflexiveMapView;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.ads.ADSNode;
import net.automatalib.graphs.ads.impl.ADSLeafNode;
import net.automatalib.graphs.ads.impl.ADSSymbolNode;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A class containing methods for computing adaptive distinguishing sequences (for arbitrary sets of states) by means of
 * a backtracking approach.
 *
 * @author frohme
 */
public final class BacktrackingSearch {

    private BacktrackingSearch() {}

    /**
     * Computes an ADS by constructing (growing) splitting words for the current set of states and recursively computing
     * sub-ADSs for the induced partitions. May yield non-optimal ADSs.
     *
     * @param automaton
     *         The automaton for which an ADS should be computed
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

        if (states.size() == 1) {
            return ADS.compute(automaton, input, states);
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
        return compute(automaton, input, node, node.getPartition().size());
    }

    private static <S, I, T, O> Optional<ADSNode<S, I, O>> compute(final MealyMachine<S, I, T, O> automaton,
                                                                   final Alphabet<I> input,
                                                                   final SplitTree<S, I, O> node,
                                                                   final int originalPartitionSize) {

        final long maximumSplittingWordLength = ADSUtil.computeMaximumSplittingWordLength(automaton.size(),
                                                                                          node.getPartition().size(),
                                                                                          originalPartitionSize);
        final Queue<Word<I>> splittingWordCandidates = new LinkedList<>();
        final StateIDs<S> stateIds = automaton.stateIDs();
        final Set<BitSet> cache = new HashSet<>();

        splittingWordCandidates.add(Word.epsilon());

        candidateLoop:
        while (!splittingWordCandidates.isEmpty()) {

            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            final @NonNull Word<I> prefix = splittingWordCandidates.poll();
            @SuppressWarnings("nullness") // the initial prefix (epsilon) guarantees non-nullness.
            final Map<S, S> currentToInitialMapping = node.getPartition()
                                                          .stream()
                                                          .collect(Collectors.toMap(x -> automaton.getSuccessor(x,
                                                                                                                prefix),
                                                                                    Function.identity()));
            final BitSet currentSetAsBitSet = new BitSet();
            for (final S s : currentToInitialMapping.keySet()) {
                currentSetAsBitSet.set(stateIds.getStateId(s));
            }

            if (cache.contains(currentSetAsBitSet)) {
                continue candidateLoop;
            }

            oneSymbolFuture:
            for (final I i : input) {
                // compute successors
                final Map<O, SplitTree<S, I, O>> successors = new HashMap<>();

                for (final Map.Entry<S, S> entry : currentToInitialMapping.entrySet()) {
                    final S current = entry.getKey();
                    final T trans = automaton.getTransition(current, i);

                    if (trans == null) {
                        throw new IllegalArgumentException("Partial automata are not supported");
                    }

                    final S nextState = automaton.getSuccessor(trans);
                    final O nextOutput = automaton.getTransitionOutput(trans);

                    final SplitTree<S, I, O> child;
                    if (!successors.containsKey(nextOutput)) {
                        child = new SplitTree<>(new HashSet<>());
                        successors.put(nextOutput, child);
                    } else {
                        child = successors.get(nextOutput);
                    }

                    // invalid input
                    if (!child.getPartition().add(nextState)) {
                        continue oneSymbolFuture;
                    }
                    child.getMapping().put(nextState, node.getMapping().get(entry.getValue()));
                }

                //splitting word
                if (successors.size() > 1) {
                    final Map<O, ADSNode<S, I, O>> results = new HashMap<>();

                    for (final Map.Entry<O, SplitTree<S, I, O>> entry : successors.entrySet()) {

                        final SplitTree<S, I, O> currentNode = entry.getValue();

                        final BitSet currentNodeAsBitSet = new BitSet();
                        for (final S s : currentNode.getPartition()) {
                            currentNodeAsBitSet.set(stateIds.getStateId(s));
                        }

                        if (cache.contains(currentNodeAsBitSet)) {
                            continue oneSymbolFuture;
                        }

                        final Optional<ADSNode<S, I, O>> succ;
                        if (currentNode.getPartition().size() > 2) {
                            succ = BacktrackingSearch.compute(automaton, input, currentNode, originalPartitionSize);
                        } else {
                            succ = ADS.compute(automaton, input, currentNode);
                        }

                        if (!succ.isPresent()) {
                            cache.add(currentNodeAsBitSet);
                            continue oneSymbolFuture;
                        }

                        results.put(entry.getKey(), succ.get());
                    }

                    // create ADS (if we haven't continued until here)
                    final Pair<ADSNode<S, I, O>, ADSNode<S, I, O>> ads =
                            ADSUtil.buildFromTrace(automaton, prefix.append(i), node.getPartition().iterator().next());
                    final ADSNode<S, I, O> head = ads.getFirst();
                    final ADSNode<S, I, O> tail = ads.getSecond();

                    for (final Map.Entry<O, ADSNode<S, I, O>> entry : results.entrySet()) {
                        entry.getValue().setParent(tail);
                        tail.getChildren().put(entry.getKey(), entry.getValue());
                    }

                    return Optional.of(head);
                } else if (prefix.length() < maximumSplittingWordLength) { // no splitting word
                    splittingWordCandidates.add(prefix.append(i));
                }
            }

            cache.add(currentSetAsBitSet);
        }

        return Optional.empty();
    }

    /**
     * Computes an ADS by iterating over the successor tree in a breadth-first manner, yielding an optimal (dependant on
     * the passed optimization function) ADS.
     *
     * @param automaton
     *         The automaton for which an ADS should be computed
     * @param input
     *         the input alphabet of the automaton
     * @param states
     *         the set of states which should be distinguished by the computed ADS
     * @param costAggregator
     *         the optimization function by which solutions should be pruned
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
    public static <S, I, O> Optional<ADSNode<S, I, O>> computeOptimal(final MealyMachine<S, I, ?, O> automaton,
                                                                      final Alphabet<I> input,
                                                                      final Set<S> states,
                                                                      final CostAggregator costAggregator) {

        if (states.size() == 1) {
            return ADS.compute(automaton, input, states);
        }

        final Optional<SearchState<S, I, O>> searchState = exploreSearchSpace(automaton,
                                                                              input,
                                                                              states,
                                                                              costAggregator,
                                                                              new HashMap<>(),
                                                                              new HashSet<>(),
                                                                              Integer.MAX_VALUE);

        return searchState.map(s -> constructADS(automaton, new ReflexiveMapView<>(states), s));
    }

    private static <S, I, T, O> Optional<SearchState<S, I, O>> exploreSearchSpace(final MealyMachine<S, I, T, O> automaton,
                                                                                  final Alphabet<I> alphabet,
                                                                                  final Set<S> targets,
                                                                                  final CostAggregator costAggregator,
                                                                                  final Map<Set<S>, Optional<SearchState<S, I, O>>> stateCache,
                                                                                  final Set<Set<S>> currentTraceCache,
                                                                                  final int costsBound) {

        final Optional<SearchState<S, I, O>> cachedValue = stateCache.get(targets);

        if (cachedValue != null) {
            return cachedValue;
        }

        if (currentTraceCache.contains(targets)) {
            return Optional.empty();
        }

        if (targets.size() == 1) {
            final SearchState<S, I, O> resultSS = new SearchState<>();
            final Optional<SearchState<S, I, O>> result = Optional.of(resultSS);
            stateCache.put(targets, result);
            return result;
        }

        // any further expansion would lead to a worse result, hence stop here.
        if (costsBound == 0) {
            return Optional.empty();
        }

        boolean foundValidSuccessor = false;
        boolean convergingStates = true;
        int bestCosts = costsBound;
        Map<O, SearchState<S, I, O>> bestSuccessor = null;
        I bestInputSymbol = null;

        alphabetLoop:
        for (final I i : alphabet) {

            // compute successors
            final Map<O, Set<S>> successors = new HashMap<>();

            for (final S s : targets) {
                final T trans = automaton.getTransition(s, i);

                if (trans == null) {
                    throw new IllegalArgumentException("Partial automata are not supported");
                }

                final S nextState = automaton.getSuccessor(trans);
                final O nextOutput = automaton.getTransitionOutput(trans);

                final Set<S> child;
                if (!successors.containsKey(nextOutput)) {
                    child = new HashSet<>();
                    successors.put(nextOutput, child);
                } else {
                    child = successors.get(nextOutput);
                }

                // invalid input
                if (!child.add(nextState)) {
                    continue alphabetLoop;
                }
            }

            convergingStates = false;

            final int costsForInputSymbol;
            final Map<O, SearchState<S, I, O>> successorsForInputSymbol;

            if (successors.size() > 1) {

                successorsForInputSymbol = Maps.newHashMapWithExpectedSize(successors.size());
                int partitionCosts = 0;

                for (final Map.Entry<O, Set<S>> entry : successors.entrySet()) {

                    final Optional<SearchState<S, I, O>> potentialResult = exploreSearchSpace(automaton,
                                                                                              alphabet,
                                                                                              entry.getValue(),
                                                                                              costAggregator,
                                                                                              stateCache,
                                                                                              new HashSet<>(),
                                                                                              bestCosts);

                    if (!potentialResult.isPresent()) {
                        continue alphabetLoop;
                    }

                    final SearchState<S, I, O> subResult = potentialResult.get();
                    successorsForInputSymbol.put(entry.getKey(), subResult);

                    partitionCosts = costAggregator.apply(partitionCosts, subResult.costs);

                    if (partitionCosts >= bestCosts) {
                        continue alphabetLoop;
                    }
                }

                costsForInputSymbol = partitionCosts;
            } else {
                final Map.Entry<O, Set<S>> entry = successors.entrySet().iterator().next();
                final Set<S> nextTargets = entry.getValue();

                final Set<Set<S>> nextTraceCache = new HashSet<>(currentTraceCache);
                nextTraceCache.add(targets);

                final Optional<SearchState<S, I, O>> potentialResult = exploreSearchSpace(automaton,
                                                                                          alphabet,
                                                                                          nextTargets,
                                                                                          costAggregator,
                                                                                          stateCache,
                                                                                          nextTraceCache,
                                                                                          bestCosts);

                if (!potentialResult.isPresent()) {
                    continue alphabetLoop;
                }

                final SearchState<S, I, O> subResult = potentialResult.get();

                costsForInputSymbol = subResult.costs;
                successorsForInputSymbol = Collections.singletonMap(entry.getKey(), subResult);
            }

            // update result
            if (costsForInputSymbol < bestCosts) {
                foundValidSuccessor = true;
                bestCosts = costsForInputSymbol;
                bestSuccessor = successorsForInputSymbol;
                bestInputSymbol = i;
            }
        }

        if (convergingStates) {
            stateCache.put(targets, Optional.empty());
            return Optional.empty();
        }

        if (!foundValidSuccessor) {
            return Optional.empty();
        }

        final SearchState<S, I, O> resultSS = new SearchState<>();
        resultSS.costs = bestCosts + 1;
        resultSS.successors = bestSuccessor;
        resultSS.symbol = bestInputSymbol;

        final Optional<SearchState<S, I, O>> result = Optional.of(resultSS);
        stateCache.put(targets, result);
        return result;
    }

    private static <S, I, O> ADSNode<S, I, O> constructADS(final MealyMachine<S, I, ?, O> automaton,
                                                           final Map<S, S> currentToInitialMapping,
                                                           final SearchState<S, I, O> searchState) {

        if (currentToInitialMapping.size() == 1) {
            return new ADSLeafNode<>(null, currentToInitialMapping.values().iterator().next());
        }

        final I i = searchState.symbol;
        final Map<O, Map<S, S>> successors = new HashMap<>();

        for (final Map.Entry<S, S> entry : currentToInitialMapping.entrySet()) {
            final S current = entry.getKey();
            final S nextState = automaton.getSuccessor(current, i);
            final O nextOutput = automaton.getOutput(current, i);

            final Map<S, S> nextMapping;
            if (!successors.containsKey(nextOutput)) {
                nextMapping = new HashMap<>();
                successors.put(nextOutput, nextMapping);
            } else {
                nextMapping = successors.get(nextOutput);
            }

            // invalid input
            if (nextMapping.put(nextState, entry.getValue()) != null) {
                throw new IllegalStateException();
            }
        }

        final ADSNode<S, I, O> result = new ADSSymbolNode<>(null, i);

        for (final Map.Entry<O, Map<S, S>> entry : successors.entrySet()) {

            final O output = entry.getKey();
            final Map<S, S> nextMapping = entry.getValue();

            final ADSNode<S, I, O> successor = constructADS(automaton, nextMapping, searchState.successors.get(output));

            result.getChildren().put(output, successor);
            successor.setParent(result);
        }

        return result;
    }

    /**
     * Utility enum, that allows to specify the optimization criterion when performing and optimal ADS search. See
     * {@link BacktrackingSearch#computeOptimal(MealyMachine, Alphabet, Set, CostAggregator)}.
     */
    public enum CostAggregator implements BiFunction<Integer, Integer, Integer> {
        MIN_LENGTH() {
            @Override
            public Integer apply(Integer oldValue, Integer newValue) {
                return Math.max(oldValue, newValue);
            }
        },

        MIN_SIZE() {
            @Override
            public Integer apply(Integer oldValue, Integer newValue) {
                return oldValue + newValue;
            }
        }
    }

    /**
     * Internal utility class that encapsulates information of a node in a successor tree.
     *
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     */
    private static class SearchState<S, I, O> {

        private I symbol;

        private Map<O, SearchState<S, I, O>> successors;

        private int costs;
    }
}
