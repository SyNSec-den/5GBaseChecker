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
package net.automatalib.util.automata.cover;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.google.common.collect.Sets;
import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author Malte Isberner
 * @author frohme
 */
public final class Covers {

    private Covers() {}

    /**
     * Computes a state cover for a given automaton.
     * <p>
     * A state cover is a set <i>C</i> of input sequences, such that for each state <i>s</i> of an automaton, there
     * exists an input sequence in <i>C</i> that transitions the automaton from its initial state to state s.
     * <p>
     * Note: if restrictions on the {@code inputs} parameter do not allow to reach certain states, the computed cover is
     * not complete.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param states
     *         the collection in which the sequences will be stored
     * @param <I>
     *         input symbol type
     */
    public static <I> void stateCover(DeterministicAutomaton<?, I, ?> automaton,
                                      Collection<? extends I> inputs,
                                      Collection<? super Word<I>> states) {
        cover(automaton, inputs, states::add, w -> {});
    }

    /**
     * Returns an iterator for the sequences of a state cover. Sequences are computed lazily (i.e. as requested by the
     * iterators {@link Iterator#next() next} method.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param <I>
     *         input symbol type
     *
     * @return an iterator for the input sequences of the cover.
     *
     * @see #stateCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <I> Iterator<Word<I>> stateCoverIterator(DeterministicAutomaton<?, I, ?> automaton,
                                                           Collection<? extends I> inputs) {
        return new IncrementalStateCoverIterator<>(automaton, inputs, Collections.emptyList());
    }

    /**
     * Computes a transition cover for a given automaton.
     * <p>
     * A transition cover is a set <i>C</i> of input sequences, such that for each state <i>s</i> and each input symbol
     * <i>i</i> of an automaton, there exists an input sequence in <i>C</i> that starts from the initial state of the
     * automaton and ends with the transition that applies <i>i</i> to state <i>s</i>.
     * <p>
     * Note: if restrictions on the {@code inputs} parameter do not allow to reach certain transitions, the computed
     * cover is not complete.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param transitions
     *         the collection in which the sequences will be stored
     * @param <I>
     *         input symbol type
     */
    public static <I> void transitionCover(DeterministicAutomaton<?, I, ?> automaton,
                                           Collection<? extends I> inputs,
                                           Collection<? super Word<I>> transitions) {
        cover(automaton, inputs, w -> {}, transitions::add);
    }

    /**
     * Returns an iterator for the sequences of a transition cover. Sequences are computed lazily (i.e. as requested by
     * the iterators {@link Iterator#next() next} method.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param <I>
     *         input symbol type
     *
     * @return an iterator for the input sequences of the cover.
     *
     * @see #transitionCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <I> Iterator<Word<I>> transitionCoverIterator(DeterministicAutomaton<?, I, ?> automaton,
                                                                Collection<? extends I> inputs) {
        return new TransitionCoverIterator<>(automaton, inputs);
    }

    /**
     * Computes a structural cover for a given automaton.
     * <p>
     * A structural cover is the union of a state cover and a transition cover
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param cover
     *         the collection in which the sequences will be stored
     * @param <I>
     *         input symbol type
     *
     * @see #stateCover(DeterministicAutomaton, Collection, Collection)
     * @see #transitionCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <I> void structuralCover(DeterministicAutomaton<?, I, ?> automaton,
                                           Collection<? extends I> inputs,
                                           Collection<? super Word<I>> cover) {
        cover(automaton, inputs, cover::add, cover::add);
    }

    /**
     * Utility method that allows to compute a state and transition cover simultaneously.
     *
     * @param automaton
     *         the automaton for which the covers should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param states
     *         the collection in which the state cover sequences will be stored
     * @param transitions
     *         the collection in which the transition cover sequences will be stored
     * @param <I>
     *         input symbol type
     *
     * @see #stateCover(DeterministicAutomaton, Collection, Collection)
     * @see #transitionCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <I> void cover(DeterministicAutomaton<?, I, ?> automaton,
                                 Collection<? extends I> inputs,
                                 Collection<? super Word<I>> states,
                                 Collection<? super Word<I>> transitions) {
        cover(automaton, inputs, states::add, transitions::add);
    }

    private static <S, I> void cover(DeterministicAutomaton<S, I, ?> automaton,
                                     Collection<? extends I> inputs,
                                     Consumer<? super Word<I>> states,
                                     Consumer<? super Word<I>> transitions) {

        S init = automaton.getInitialState();

        if (init == null) {
            return;
        }

        MutableMapping<S, @Nullable Word<I>> reach = automaton.createStaticStateMapping();
        reach.put(init, Word.epsilon());

        Queue<S> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(init);

        states.accept(Word.epsilon());

        S curr;

        while ((curr = bfsQueue.poll()) != null) {
            Word<I> as = reach.get(curr);
            assert as != null;

            for (I in : inputs) {
                S succ = automaton.getSuccessor(curr, in);
                if (succ == null) {
                    continue;
                }

                final Word<I> succAs = as.append(in);

                if (reach.get(succ) == null) {
                    reach.put(succ, succAs);
                    states.accept(succAs);
                    bfsQueue.add(succ);
                }
                transitions.accept(succAs);
            }
        }
    }

    /**
     * Computes an incremental state cover for a given automaton, i.e. a cover that only contains the missing sequences
     * for obtaining a complete state cover.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param oldStates
     *         the collection containing the already existing sequences of the state cover
     * @param newStates
     *         the collection in which the missing sequences will be stored
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if new sequences have been added to the state cover, {@code false} otherwise.
     *
     * @see #stateCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <S, I> boolean incrementalStateCover(DeterministicAutomaton<S, I, ?> automaton,
                                                       Collection<? extends I> inputs,
                                                       Collection<? extends Word<I>> oldStates,
                                                       Collection<? super Word<I>> newStates) {
        S init = automaton.getInitialState();

        if (init == null) {
            return false;
        }

        MutableMapping<S, @Nullable Record<S, I>> reach = automaton.createStaticStateMapping();

        boolean augmented = false;

        Queue<Record<S, I>> bfsQueue = new ArrayDeque<>();

        buildReachFromStateCover(reach, bfsQueue, automaton, oldStates, Record::new);

        if (reach.get(init) == null) {
            // apparently the initial state was not yet covered
            Record<S, I> rec = new Record<>(init, Word.epsilon());
            reach.put(init, rec);
            bfsQueue.add(rec);
            newStates.add(Word.epsilon());
            augmented = true;
        }

        Record<S, I> curr;
        while ((curr = bfsQueue.poll()) != null) {
            S state = curr.state;
            Word<I> as = curr.accessSequence;

            for (I in : inputs) {
                S succ = automaton.getSuccessor(state, in);
                if (succ == null) {
                    continue;
                }

                if (reach.get(succ) == null) {
                    Word<I> succAs = as.append(in);
                    Record<S, I> succRec = new Record<>(succ, succAs);
                    reach.put(succ, succRec);
                    bfsQueue.add(succRec);
                    newStates.add(succAs);
                    augmented = true;
                }
            }
        }

        return augmented;
    }

    /**
     * Returns an iterator for the remaining sequences of a state cover. Sequences are computed lazily (i.e. as
     * requested by the iterators {@link Iterator#next() next} method.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param stateCover
     *         the collection containing the already existing sequences of the state cover
     * @param <I>
     *         input symbol type
     *
     * @return an iterator for the remaining input sequences of the cover.
     *
     * @see #incrementalStateCover(DeterministicAutomaton, Collection, Collection, Collection)
     */
    public static <I> Iterator<Word<I>> incrementalStateCoverIterator(DeterministicAutomaton<?, I, ?> automaton,
                                                                      Collection<? extends I> inputs,
                                                                      Collection<? extends Word<I>> stateCover) {
        return new IncrementalStateCoverIterator<>(automaton, inputs, stateCover);
    }

    /**
     * Computes an incremental transition cover for a given automaton, i.e. a cover that only contains the missing
     * sequences for obtaining a complete transition cover.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param oldTransCover
     *         the collection containing the already existing sequences of the transition cover
     * @param newTransCover
     *         the collection in which the missing sequences will be stored
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if new sequences have been added to the state cover, {@code false} otherwise.
     *
     * @see #transitionCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <I> boolean incrementalTransitionCover(DeterministicAutomaton<?, I, ?> automaton,
                                                         Collection<? extends I> inputs,
                                                         Collection<? extends Word<I>> oldTransCover,
                                                         Collection<? super Word<I>> newTransCover) {
        final int oldTransSize = newTransCover.size();

        incrementalCover(automaton, inputs, Collections.emptySet(), oldTransCover, w -> {}, newTransCover::add);

        return oldTransSize < newTransCover.size();
    }

    /**
     * Returns an iterator for the remaining sequences of a transition cover. Sequences are computed lazily (i.e. as
     * requested by the iterators {@link Iterator#next() next} method.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param transitionCover
     *         the collection containing the already existing sequences of the transition cover
     * @param <I>
     *         input symbol type
     *
     * @return an iterator for the remaining input sequences of the cover.
     *
     * @see #incrementalStateCover(DeterministicAutomaton, Collection, Collection, Collection)
     */
    public static <I> Iterator<Word<I>> incrementalTransitionCoverIterator(DeterministicAutomaton<?, I, ?> automaton,
                                                                           Collection<? extends I> inputs,
                                                                           Collection<? extends Word<I>> transitionCover) {
        return new IncrementalTransitionCoverIterator<>(automaton, inputs, transitionCover);
    }

    /**
     * Computes an incremental structural cover for a given automaton, i.e. a cover that only contains the missing
     * sequences for obtaining a complete structural cover.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param oldCover
     *         the collection containing the already existing sequences of the structural cover
     * @param newCover
     *         the collection in which the missing sequences will be stored
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if new sequences have been added to the structural cover, {@code false} otherwise.
     *
     * @see #structuralCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <I> boolean incrementalStructuralCover(DeterministicAutomaton<?, I, ?> automaton,
                                                         Collection<? extends I> inputs,
                                                         Collection<? extends Word<I>> oldCover,
                                                         Collection<? super Word<I>> newCover) {
        final int oldCoverSize = newCover.size();

        incrementalCover(automaton, inputs, oldCover, Collections.emptySet(), newCover::add, newCover::add);

        return oldCoverSize < newCover.size();
    }

    /**
     * Utility method that allows to compute an incremental state and transition cover simultaneously.
     *
     * @param automaton
     *         the automaton for which the covers should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param oldStateCover
     *         the collection containing the already existing sequences of the state cover
     * @param oldTransCover
     *         the collection containing the already existing sequences of the transition cover
     * @param newStateCover
     *         the collection in which the missing state cover sequences will be stored
     * @param newTransCover
     *         the collection in which the missing transition cover sequences will be stored
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if new sequences have been added to the structural cover, {@code false} otherwise.
     *
     * @see #incrementalStateCover(DeterministicAutomaton, Collection, Collection, Collection)
     * @see #incrementalStateCover(DeterministicAutomaton, Collection, Collection, Collection)
     */
    public static <I> boolean incrementalCover(DeterministicAutomaton<?, I, ?> automaton,
                                               Collection<? extends I> inputs,
                                               Collection<? extends Word<I>> oldStateCover,
                                               Collection<? extends Word<I>> oldTransCover,
                                               Collection<? super Word<I>> newStateCover,
                                               Collection<? super Word<I>> newTransCover) {
        final int oldStateSize = newStateCover.size();
        final int oldTransSize = newTransCover.size();

        incrementalCover(automaton, inputs, oldStateCover, oldTransCover, newStateCover::add, newTransCover::add);

        return oldStateSize < newStateCover.size() || oldTransSize < newTransCover.size();
    }

    private static <S, I> void incrementalCover(DeterministicAutomaton<S, I, ?> automaton,
                                                Collection<? extends I> inputs,
                                                Collection<? extends Word<I>> oldStateCover,
                                                Collection<? extends Word<I>> oldTransCover,
                                                Consumer<? super Word<I>> newStateCover,
                                                Consumer<? super Word<I>> newTransCover) {

        S init = automaton.getInitialState();

        if (init == null) {
            return;
        }

        MutableMapping<S, @Nullable Record<S, I>> reach = automaton.createStaticStateMapping();

        Queue<Record<S, I>> bfsQueue = new ArrayDeque<>();

        // We enforce that the initial state *always* is covered by the empty word,
        // regardless of whether other sequence in oldCover cover it
        Record<S, I> initRec = new Record<>(init, Word.epsilon(), Sets.newHashSetWithExpectedSize(inputs.size()));
        bfsQueue.add(initRec);
        reach.put(init, initRec);

        // the call to 'buildReachFromStateCover' not only caches 'hasEpsilon' but also fills its passed collections
        @SuppressWarnings("PMD.PrematureDeclaration")
        boolean hasEpsilon = buildReachFromStateCover(reach,
                                                      bfsQueue,
                                                      automaton,
                                                      oldStateCover,
                                                      (s, as) -> new Record<>(s,
                                                                              as,
                                                                              Sets.newHashSetWithExpectedSize(inputs.size())));

        // Add transition cover information from *state covers*
        for (Word<I> oldStateAs : oldStateCover) {
            if (oldStateAs.isEmpty()) {
                continue;
            }

            Word<I> asPrefix = oldStateAs.prefix(oldStateAs.length() - 1);
            S pred = automaton.getState(asPrefix);
            assert pred != null;

            Record<S, I> predRec = reach.get(pred);
            if (predRec == null) {
                throw new IllegalArgumentException(
                        "State cover was not prefix-closed: prefix of " + oldStateAs + " not in set");
            }
            I lastSym = oldStateAs.lastSymbol();
            predRec.coveredInputs.add(lastSym);
        }

        // Till now, we haven't augmented any set.
        if (!hasEpsilon) {
            newStateCover.accept(Word.epsilon());
        }

        // Add transition covers
        buildReachFromTransitionCover(reach,
                                      bfsQueue,
                                      automaton,
                                      oldTransCover,
                                      (s, as) -> new Record<>(s, as, Sets.newHashSetWithExpectedSize(inputs.size())),
                                      newStateCover);

        Record<S, I> curr;
        while ((curr = bfsQueue.poll()) != null) {
            for (I input : inputs) {
                if (curr.coveredInputs.add(input)) {
                    S succ = automaton.getSuccessor(curr.state, input);

                    Word<I> newAs = curr.accessSequence.append(input);

                    if (succ != null) {
                        Record<S, I> succRec = reach.get(succ);

                        if (succRec == null) {
                            // new state!
                            succRec = new Record<>(succ, newAs, Sets.newHashSetWithExpectedSize(inputs.size()));
                            bfsQueue.add(succRec);
                            reach.put(succ, succRec);

                            newStateCover.accept(newAs);
                        }
                        // new transition
                        newTransCover.accept(newAs);
                    }
                }
            }
        }
    }

    static <S, I> boolean buildReachFromStateCover(MutableMapping<S, @Nullable Record<S, I>> reach,
                                                   Queue<Record<S, I>> bfsQueue,
                                                   DeterministicAutomaton<S, I, ?> automaton,
                                                   Collection<? extends Word<I>> oldStateCover,
                                                   BiFunction<S, Word<I>, Record<S, I>> recordBuilder) {

        boolean hasEpsilon = false;

        for (Word<I> oldStateAs : oldStateCover) {
            S state = automaton.getState(oldStateAs);
            if (state == null || reach.get(state) != null) {
                if (oldStateAs.isEmpty()) {
                    hasEpsilon = true;
                }
                continue; // strange, but we'll ignore it
            }

            Record<S, I> rec = recordBuilder.apply(state, oldStateAs);
            bfsQueue.add(rec);
            reach.put(state, rec);
        }

        return hasEpsilon;
    }

    static <S, I> void buildReachFromTransitionCover(MutableMapping<S, @Nullable Record<S, I>> reach,
                                                     Queue<Record<S, I>> bfsQueue,
                                                     DeterministicAutomaton<S, I, ?> automaton,
                                                     Collection<? extends Word<I>> oldTransCover,
                                                     BiFunction<S, Word<I>, Record<S, I>> recordBuilder,
                                                     Consumer<? super Word<I>> newStateCallback) {

        for (Word<I> oldTransAs : oldTransCover) {
            // Check if this transition now leads to a new state
            S state = automaton.getState(oldTransAs);
            if (state != null) {
                Record<S, I> rec = reach.get(state);
                if (rec == null) {
                    // if so, add it to the state cover and to the queue
                    rec = recordBuilder.apply(state, oldTransAs);
                    bfsQueue.add(rec);
                    reach.put(state, rec);
                    newStateCallback.accept(oldTransAs);
                }
            }

            // In any case, mark the transition as covered
            Word<I> predAs = oldTransAs.prefix(oldTransAs.length() - 1);
            S pred = automaton.getState(predAs);
            if (pred == null) {
                throw new IllegalArgumentException(
                        "Invalid transition: prefix of transition " + oldTransAs + " not covered by state cover");
            }
            I lastSym = oldTransAs.lastSymbol();
            Record<S, I> predRec = reach.get(pred);

            if (predRec == null) {
                predRec = recordBuilder.apply(state, oldTransAs);
                bfsQueue.add(predRec);
                reach.put(pred, predRec);
                newStateCallback.accept(oldTransAs);
            }

            predRec.coveredInputs.add(lastSym);
        }
    }

}
