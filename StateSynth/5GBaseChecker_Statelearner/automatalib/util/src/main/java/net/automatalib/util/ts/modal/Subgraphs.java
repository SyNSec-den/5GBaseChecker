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
package net.automatalib.util.ts.modal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.automata.predicates.TransitionPredicates;
import net.automatalib.util.fixpoint.Closures;

public final class Subgraphs {

    private Subgraphs() {
        // prevent instantiation
    }

    public enum SubgraphType {
        DISREGARD_UNKNOWN_LABELS {
            @Override
            <S, I, T> TransitionPredicate<S, I, T> getTransitionPredicate(Collection<I> inputs) {
                return TransitionPredicates.alwaysFalse();
            }
        },
        HIDE_UNKNOWN_LABELS {
            @Override
            <S, I, T> TransitionPredicate<S, I, T> getTransitionPredicate(Collection<I> inputs) {
                return TransitionPredicates.inputNotIn(inputs);
            }

        };

        abstract <S, I, T> TransitionPredicate<S, I, T> getTransitionPredicate(Collection<I> inputs);
    }

    /**
     * Returns the subgraph of ts with labels from inputs.
     * <p>
     * Creates a new instance of creator and copies ts into it. All symbols not in inputs are handled according to
     * strategy.
     */
    public static <A extends MutableAutomaton<S1, I, T1, SP1, TP1>, B extends UniversalFiniteAlphabetAutomaton<S2, I, T2, SP2, TP2>, S1, I, T1, SP1, TP1, S2, T2, SP2, TP2> Pair<Map<Set<S2>, S1>, A> subgraphView(
            AutomatonCreator<A, I> creator,
            SubgraphType type,
            B ts,
            Collection<I> inputs) {

        return Closures.closure(ts,
                                inputs,
                                creator,
                                Closures.toClosureOperator(ts,
                                                           ts.getInputAlphabet(),
                                                           type.getTransitionPredicate(inputs)),
                                TransitionPredicates.inputIn(inputs));
    }
}
