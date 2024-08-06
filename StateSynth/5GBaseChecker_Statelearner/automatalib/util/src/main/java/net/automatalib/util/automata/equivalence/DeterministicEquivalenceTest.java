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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DeterministicEquivalenceTest<I> {

    private static final int MAP_THRESHOLD = 10_000;
    private final UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference;

    public DeterministicEquivalenceTest(UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference) {
        this.reference = reference;
    }

    public @Nullable Word<I> findSeparatingWord(UniversalDeterministicAutomaton<?, I, ?, ?, ?> other,
                                                Collection<? extends I> inputs) {
        return findSeparatingWord(reference, other, inputs);
    }

    public static <I, S, T, SP, TP, S2, T2, SP2, TP2> @Nullable Word<I> findSeparatingWord(
            UniversalDeterministicAutomaton<S, I, T, SP, TP> reference,
            UniversalDeterministicAutomaton<S2, I, T2, SP2, TP2> other,
            Collection<? extends I> inputs) {
        int refSize = reference.size();
        int totalStates = refSize * other.size();

        if (totalStates < 0 || totalStates > MAP_THRESHOLD) {
            return findSeparatingWordLarge(reference, other, inputs);
        }

        S refInit = reference.getInitialState();
        S2 otherInit = other.getInitialState();

        if (refInit == null || otherInit == null) {
            return refInit == null && otherInit == null ? null : Word.epsilon();
        }

        SP refStateProp = reference.getStateProperty(refInit);
        SP2 otherStateProp = other.getStateProperty(otherInit);

        if (!Objects.equals(refStateProp, otherStateProp)) {
            return Word.epsilon();
        }

        Queue<StatePair<S, S2>> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(new StatePair<>(refInit, otherInit));

        StateIDs<S> refStateIds = reference.stateIDs();
        StateIDs<S2> otherStateIds = other.stateIDs();

        StatePair<S, S2> currPair;
        int lastId = otherStateIds.getStateId(otherInit) * refSize + refStateIds.getStateId(refInit);

        @SuppressWarnings("unchecked")
        Pred<I>[] preds = new Pred[totalStates];
        preds[lastId] = new Pred<>();

        int currDepth = 0;
        int inCurrDepth = 1;
        int inNextDepth = 0;

        I lastSym = null;

        bfs:
        while ((currPair = bfsQueue.poll()) != null) {
            S refState = currPair.ref;
            S2 otherState = currPair.other;

            int currId = otherStateIds.getStateId(otherState) * refSize + refStateIds.getStateId(refState);
            lastId = currId;

            for (I in : inputs) {
                lastSym = in;
                T refTrans = reference.getTransition(refState, in);
                T2 otherTrans = other.getTransition(otherState, in);

                if (refTrans == null || otherTrans == null) {
                    if (refTrans == null && otherTrans == null) {
                        continue;
                    } else {
                        break bfs;
                    }
                }

                TP refProp = reference.getTransitionProperty(refTrans);
                TP2 otherProp = other.getTransitionProperty(otherTrans);
                if (!Objects.equals(refProp, otherProp)) {
                    break bfs;
                }

                S refSucc = reference.getSuccessor(refTrans);
                S2 otherSucc = other.getSuccessor(otherTrans);

                int succId = otherStateIds.getStateId(otherSucc) * refSize + refStateIds.getStateId(refSucc);

                if (preds[succId] == null) {
                    refStateProp = reference.getStateProperty(refSucc);
                    otherStateProp = other.getStateProperty(otherSucc);

                    if (!Objects.equals(refStateProp, otherStateProp)) {
                        break bfs;
                    }

                    preds[succId] = new Pred<>(currId, in);
                    bfsQueue.add(new StatePair<>(refSucc, otherSucc));
                    inNextDepth++;
                }
            }

            lastSym = null;

            // Next level in BFS reached
            if (--inCurrDepth == 0) {
                inCurrDepth = inNextDepth;
                inNextDepth = 0;
                currDepth++;
            }
        }

        if (lastSym == null) {
            return null;
        }

        @SuppressWarnings("nullness") // we make sure to set each index to a value of type I
        WordBuilder<I> sep = new WordBuilder<>(null, currDepth + 1);
        int index = currDepth;
        sep.setSymbol(index--, lastSym);

        Pred<I> pred = preds[lastId];
        while (pred.id >= 0) {
            sep.setSymbol(index--, pred.symbol);
            pred = preds[pred.id];
        }

        return sep.toWord();
    }

    public static <I, S, T, SP, TP, S2, T2, SP2, TP2> @Nullable Word<I> findSeparatingWordLarge(
            UniversalDeterministicAutomaton<S, I, T, SP, TP> reference,
            UniversalDeterministicAutomaton<S2, I, T2, SP2, TP2> other,
            Collection<? extends I> inputs) {
        S refInit = reference.getInitialState();
        S2 otherInit = other.getInitialState();

        if (refInit == null || otherInit == null) {
            return refInit == null && otherInit == null ? null : Word.epsilon();
        }

        SP refStateProp = reference.getStateProperty(refInit);
        SP2 otherStateProp = other.getStateProperty(otherInit);

        if (!Objects.equals(refStateProp, otherStateProp)) {
            return Word.epsilon();
        }

        Queue<StatePair<S, S2>> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(new StatePair<>(refInit, otherInit));

        int refSize = reference.size();

        StateIDs<S> refStateIds = reference.stateIDs();
        StateIDs<S2> otherStateIds = other.stateIDs();

        StatePair<S, S2> currPair;
        int lastId = otherStateIds.getStateId(otherInit) * refSize + refStateIds.getStateId(refInit);

        //TIntObjectMap<Pred<I>> preds = new TIntObjectHashMap<>();
        Map<Integer, Pred<I>> preds = new HashMap<>(); // TODO: replace by primitive specialization
        preds.put(lastId, new Pred<>());

        int currDepth = 0;
        int inCurrDepth = 1;
        int inNextDepth = 0;

        I lastSym = null;

        bfs:
        while ((currPair = bfsQueue.poll()) != null) {
            S refState = currPair.ref;
            S2 otherState = currPair.other;

            int currId = otherStateIds.getStateId(otherState) * refSize + refStateIds.getStateId(refState);
            lastId = currId;

            for (I in : inputs) {
                lastSym = in;
                T refTrans = reference.getTransition(refState, in);
                T2 otherTrans = other.getTransition(otherState, in);

                if (refTrans == null || otherTrans == null) {
                    if (refTrans == null && otherTrans == null) {
                        continue;
                    } else {
                        break bfs;
                    }
                }

                TP refProp = reference.getTransitionProperty(refTrans);
                TP2 otherProp = other.getTransitionProperty(otherTrans);
                if (!Objects.equals(refProp, otherProp)) {
                    break bfs;
                }

                S refSucc = reference.getSuccessor(refTrans);
                S2 otherSucc = other.getSuccessor(otherTrans);

                int succId = otherStateIds.getStateId(otherSucc) * refSize + refStateIds.getStateId(refSucc);

                if (preds.get(succId) == null) {
                    refStateProp = reference.getStateProperty(refSucc);
                    otherStateProp = other.getStateProperty(otherSucc);

                    if (!Objects.equals(refStateProp, otherStateProp)) {
                        break bfs;
                    }

                    preds.put(succId, new Pred<>(currId, in));
                    bfsQueue.add(new StatePair<>(refSucc, otherSucc));
                    inNextDepth++;
                }
            }

            lastSym = null;

            // Next level in BFS reached
            if (--inCurrDepth == 0) {
                inCurrDepth = inNextDepth;
                inNextDepth = 0;
                currDepth++;
            }
        }

        if (lastSym == null) {
            return null;
        }

        @SuppressWarnings("nullness") // we make sure to set each index to a value of type I
        WordBuilder<I> sep = new WordBuilder<>(null, currDepth + 1);
        int index = currDepth;
        sep.setSymbol(index--, lastSym);

        Pred<I> pred = preds.get(lastId);
        assert pred != null;
        while (pred.id >= 0) {
            sep.setSymbol(index--, pred.symbol);
            pred = preds.get(pred.id);
            assert pred != null;
        }

        return sep.toWord();
    }

    private static final class StatePair<S, S2> {

        public final S ref;
        public final S2 other;

        StatePair(S ref, S2 other) {
            this.ref = ref;
            this.other = other;
        }
    }

    private static final class Pred<I> {

        public final int id;
        public final I symbol;

        @SuppressWarnings("nullness") // we check this special element using its id value
        Pred() {
            this.id = -1;
            this.symbol = null;
        }

        Pred(int id, I input) {
            this.id = id;
            this.symbol = input;
        }
    }
}
