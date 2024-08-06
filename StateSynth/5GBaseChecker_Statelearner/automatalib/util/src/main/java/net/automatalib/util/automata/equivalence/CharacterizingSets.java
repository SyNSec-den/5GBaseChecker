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
package net.automatalib.util.automata.equivalence;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import com.google.common.collect.AbstractIterator;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.fsa.FiniteStateAcceptor;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Operations for calculating <i>characterizing sets</i>.
 * <p>
 * A characterizing set for a whole automaton is a set <i>W</i> of words such that for every two states
 * <i>s<sub>1</sub></i> and <i>s<sub>2</sub></i>, there exists a word <i>w &isin; W</i> such that <i>w</i> exposes a
 * difference between <i>s<sub>1</sub></i> and <i>s<sub>2</sub></i> (i.e., either covers a transition with differing
 * property (or not defined in only one case), or reaching a successor state with differing properties), or there exists
 * no such word at all.
 * <p>
 * A characterizing set for a single state <i>s</i> is a set <i>W</i> of words such that for every state <i>t</i>, there
 * exists a word <i>w &isin; W</i> such that <i>w</i> exposes a difference between <i>s</i> and <i>t</i>, or there
 * exists no such word at all.
 *
 * @author Malte Isberner
 */
public final class CharacterizingSets {

    private CharacterizingSets() {}

    /**
     * Computes a characterizing set for the given automaton.
     *
     * @param automaton
     *         the automaton for which to determine the characterizing set.
     * @param inputs
     *         the input alphabets to consider
     * @param result
     *         the collection in which to store the characterizing words
     */
    public static <I> void findCharacterizingSet(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton,
                                                 Collection<? extends I> inputs,
                                                 Collection<? super Word<I>> result) {
        findIncrementalCharacterizingSet(automaton, inputs, Collections.emptyList(), result);
    }

    /**
     * Computes a characterizing set for a specified state in the given automaton.
     *
     * @param automaton
     *         the automaton containing the state
     * @param inputs
     *         the input alphabets to consider
     * @param state
     *         the state for which to determine the characterizing set
     * @param result
     *         the collection in which to store the characterizing words
     */
    public static <S, I> void findCharacterizingSet(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                    Collection<? extends I> inputs,
                                                    S state,
                                                    Collection<? super Word<I>> result) {

        Object prop = automaton.getStateProperty(state);

        List<S> currentBlock = new ArrayList<>();

        boolean multipleStateProps = false;

        for (S s : automaton) {
            if (Objects.equals(s, state)) {
                continue;
            }

            Object sProp = automaton.getStateProperty(s);
            if (!Objects.equals(sProp, prop)) {
                multipleStateProps = true;
            } else {
                currentBlock.add(s);
            }
        }

        if (multipleStateProps) {
            result.add(Word.epsilon());
        }

        while (!currentBlock.isEmpty()) {
            Iterator<S> it = currentBlock.iterator();

            Word<I> suffix = null;
            while (it.hasNext() && suffix == null) {
                S s = it.next();
                suffix = Automata.findSeparatingWord(automaton, state, s, inputs);
            }

            if (suffix == null) {
                return;
            }

            result.add(suffix);

            List<?> trace = buildTrace(automaton, state, suffix);

            List<S> nextBlock = new ArrayList<>();
            while (it.hasNext()) {
                S s = it.next();
                if (checkTrace(automaton, s, suffix, trace)) {
                    nextBlock.add(s);
                }
            }

            currentBlock = nextBlock;
        }
    }

    public static <I> Iterator<Word<I>> characterizingSetIterator(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton,
                                                                  Collection<? extends I> inputs) {
        return new IncrementalCharacterizingSetIterator<>(automaton, inputs, Collections.emptyList());
    }

    private static <S, I, T, SP, TP> List<?> buildTrace(UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton,
                                                        S state,
                                                        Word<I> suffix) {
        if (suffix.isEmpty()) {
            SP prop = automaton.getStateProperty(state);
            return Collections.singletonList(prop);
        }
        List<@Nullable Object> trace = new ArrayList<>(2 * suffix.length());

        S curr = state;

        for (I sym : suffix) {
            T trans = automaton.getTransition(curr, sym);

            if (trans == null) {
                return trace;
            }

            TP transitionProperty = automaton.getTransitionProperty(trans);
            trace.add(transitionProperty);

            curr = automaton.getSuccessor(trans);
            SP stateProperty = automaton.getStateProperty(curr);
            trace.add(stateProperty);
        }

        // acceptors are evaluated on the reached state, therefore no prefixes discriminate
        if (automaton instanceof FiniteStateAcceptor) {
            return trace.subList(trace.size() - 2, trace.size());
        }

        return trace;
    }

    private static <S, I, T, SP, TP, P> boolean checkTrace(UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton,
                                                           S state,
                                                           Word<I> suffix,
                                                           List<P> trace) {

        Iterator<P> it = trace.iterator();
        S curr = state;

        for (I sym : suffix) {
            T trans = automaton.getTransition(curr, sym);

            if (!it.hasNext()) {
                return trans == null;
            }

            TP transitionProperty = automaton.getTransitionProperty(trans);

            if (!Objects.equals(transitionProperty, it.next())) {
                return false;
            }

            curr = automaton.getSuccessor(trans);
            SP stateProperty = automaton.getStateProperty(curr);

            if (!Objects.equals(stateProperty, it.next())) {
                return false;
            }
        }

        return true;
    }

    public static <S, I> boolean findIncrementalCharacterizingSet(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                                  Collection<? extends I> inputs,
                                                                  Collection<? extends Word<I>> oldSuffixes,
                                                                  Collection<? super Word<I>> newSuffixes) {

        boolean refined = false;

        // We need a list to ensure a stable iteration order
        List<? extends Word<I>> oldSuffixList = toList(oldSuffixes);

        Queue<List<S>> blocks = buildInitialBlocks(automaton, oldSuffixList);

        if (!oldSuffixes.contains(Word.epsilon()) && epsilonRefine(automaton, blocks)) {
            newSuffixes.add(Word.epsilon());
            refined = true;
        }

        Word<I> suffix;

        while ((suffix = refine(automaton, inputs, blocks)) != null) {
            newSuffixes.add(suffix);
            refined = true;
        }

        return refined;
    }

    public static <I> Iterator<Word<I>> incrementalCharacterizingSetIterator(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton,
                                                                             Collection<? extends I> inputs,
                                                                             Collection<? extends Word<I>> oldSuffixes) {
        return new IncrementalCharacterizingSetIterator<>(automaton, inputs, oldSuffixes);
    }

    private static <T> List<T> toList(Collection<T> collection) {
        if (collection instanceof List) {
            return (List<T>) collection;
        } else {
            return new ArrayList<>(collection);
        }
    }

    private static <S, I> Queue<List<S>> buildInitialBlocks(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                            List<? extends Word<I>> oldSuffixes) {
        Map<List<List<?>>, List<S>> initialPartitioning = new HashMap<>();
        Queue<List<S>> blocks = new ArrayDeque<>();
        for (S state : automaton) {
            List<List<?>> sig = buildSignature(automaton, oldSuffixes, state);
            List<S> block = initialPartitioning.get(sig);
            if (block == null) {
                block = new ArrayList<>();
                blocks.add(block);
                initialPartitioning.put(sig, block);
            }
            block.add(state);
        }

        return blocks;
    }

    private static <S, I> List<List<?>> buildSignature(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                       List<? extends Word<I>> suffixes,
                                                       S state) {
        List<List<?>> signature = new ArrayList<>(suffixes.size());

        for (Word<I> suffix : suffixes) {
            List<?> trace = buildTrace(automaton, state, suffix);
            signature.add(trace);
        }

        return signature;
    }

    private static <S, I> boolean epsilonRefine(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                Queue<List<S>> blockQueue) {

        int initialSize = blockQueue.size();

        boolean refined = false;

        for (int i = 0; i < initialSize; i++) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull List<S> block = blockQueue.poll();
            if (block.size() <= 1) {
                continue;
            }
            Map<?, List<S>> propCluster = clusterByProperty(automaton, block);
            if (propCluster.size() > 1) {
                refined = true;
            }
            blockQueue.addAll(propCluster.values());
        }

        return refined;
    }

    private static <S, I> @Nullable Word<I> refine(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                   Collection<? extends I> inputs,
                                                   Queue<List<S>> blockQueue) {

        List<S> currBlock;
        while ((currBlock = blockQueue.poll()) != null) {
            if (currBlock.size() <= 1) {
                continue; // we cannot split further
            }

            Iterator<S> it = currBlock.iterator();

            S ref = it.next();

            Word<I> suffix = null;
            S state = null;
            while (it.hasNext() && suffix == null) {
                state = it.next();
                suffix = Automata.findSeparatingWord(automaton, ref, state, inputs);
            }

            if (suffix != null) {
                int otherBlocks = blockQueue.size();

                Map<List<?>, List<S>> buckets = new HashMap<>();

                List<S> firstBucket = new ArrayList<>();
                List<S> secondBucket = new ArrayList<>();
                firstBucket.add(ref);
                buckets.put(buildTrace(automaton, ref, suffix), firstBucket);
                secondBucket.add(state);
                buckets.put(buildTrace(automaton, state, suffix), secondBucket);

                cluster(automaton, suffix, it, buckets);

                blockQueue.addAll(buckets.values());

                // Split all other blocks that were in the queue
                for (int i = 0; i < otherBlocks; i++) {
                    @SuppressWarnings("nullness") // we know that there are at least 'otherBlocks' items in the queue
                    @NonNull List<S> otherBlock = blockQueue.poll();
                    if (otherBlock.size() > 1) {
                        buckets.clear();
                        cluster(automaton, suffix, otherBlock.iterator(), buckets);
                        blockQueue.addAll(buckets.values());
                    }
                }

                return suffix;
            }
        }
        return null;
    }

    private static <S, I, SP> Map<?, List<S>> clusterByProperty(UniversalDeterministicAutomaton<S, I, ?, SP, ?> automaton,
                                                                List<S> states) {
        Map<SP, List<S>> result = new HashMap<>();

        for (S state : states) {
            SP prop = automaton.getStateProperty(state);
            List<S> block = result.computeIfAbsent(prop, k -> new ArrayList<>());
            block.add(state);
        }

        return result;
    }

    private static <S, I> void cluster(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                       Word<I> suffix,
                                       Iterator<S> stateIt,
                                       Map<List<?>, List<S>> bucketMap) {

        while (stateIt.hasNext()) {
            S state = stateIt.next();
            List<?> trace = buildTrace(automaton, state, suffix);
            List<S> bucket = bucketMap.computeIfAbsent(trace, k -> new ArrayList<>());
            bucket.add(state);
        }
    }

    private static class IncrementalCharacterizingSetIterator<S, I> extends AbstractIterator<Word<I>> {

        private final UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton;
        private final Collection<? extends I> inputs;
        private final List<? extends Word<I>> oldSuffixes;
        private Queue<List<S>> blocks;

        IncrementalCharacterizingSetIterator(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                             Collection<? extends I> inputs,
                                             Collection<? extends Word<I>> oldSuffixes) {
            this.automaton = automaton;
            this.inputs = inputs;
            this.oldSuffixes = toList(oldSuffixes);
        }

        @Override
        protected Word<I> computeNext() {

            // first call
            if (blocks == null) {
                blocks = buildInitialBlocks(automaton, oldSuffixes);
                if (!oldSuffixes.contains(Word.epsilon()) && epsilonRefine(automaton, blocks)) {
                    return Word.epsilon();
                }
            }

            final Word<I> suffix = refine(automaton, inputs, blocks);

            if (suffix != null) {
                return suffix;
            }

            return endOfData();
        }
    }
}
