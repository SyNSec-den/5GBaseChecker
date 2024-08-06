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
package net.automatalib.automata;

import java.util.function.IntFunction;

import net.automatalib.automata.abstraction.UniversalDeterministicAbstractions;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link DeterministicAutomaton} with state and transition properties.
 *
 * @param <S>
 *         state class
 * @param <I>
 *         input symbol class
 * @param <T>
 *         transition class
 * @param <SP>
 *         state property class
 * @param <TP>
 *         transition property class
 *
 * @author Malte Isberner
 * @see UniversalAutomaton
 */
public interface UniversalDeterministicAutomaton<S, I, T, SP, TP>
        extends DeterministicAutomaton<S, I, T>, UniversalDTS<S, I, T, SP, TP>, UniversalAutomaton<S, I, T, SP, TP> {

    @Override
    default FullIntAbstraction<T, SP, TP> fullIntAbstraction(Alphabet<I> alphabet) {
        return fullIntAbstraction(alphabet.size(), alphabet);
    }

    @Override
    default FullIntAbstraction<T, SP, TP> fullIntAbstraction(int numInputs, IntFunction<? extends I> symMapping) {
        return new UniversalDeterministicAbstractions.FullIntAbstraction<>(stateIntAbstraction(),
                                                                           numInputs,
                                                                           symMapping);
    }

    @Override
    default StateIntAbstraction<I, T, SP, TP> stateIntAbstraction() {
        return new UniversalDeterministicAbstractions.StateIntAbstraction<>(this);
    }

    /**
     * Base interface for {@link DeterministicAutomaton.IntAbstraction integer abstractions} of a {@link
     * UniversalDeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     *
     * @author Malte Isberner
     */
    interface IntAbstraction<T, SP, TP> extends DeterministicAutomaton.IntAbstraction<T> {

        /**
         * Retrieves the state property of a given (abstracted) state.
         *
         * @param state
         *         the integer representing the state of which to retrieve the property
         *
         * @return the property for the given state
         */
        SP getStateProperty(int state);

        /**
         * Retrieves the transition property of a given transition.
         *
         * @param transition
         *         the transition of which to retrieve the property
         *
         * @return the property for the given transition
         */
        TP getTransitionProperty(T transition);
    }

    /**
     * Interface for {@link DeterministicAutomaton.StateIntAbstraction state integer abstractions} of a {@link
     * UniversalDeterministicAutomaton}.
     *
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     *
     * @author Malte Isberner
     */
    interface StateIntAbstraction<I, T, SP, TP>
            extends IntAbstraction<T, SP, TP>, DeterministicAutomaton.StateIntAbstraction<I, T> {

        default @Nullable TP getTransitionProperty(int state, I input) {
            T trans = getTransition(state, input);
            if (trans != null) {
                return getTransitionProperty(trans);
            }
            return null;
        }

    }

    /**
     * Interface for {@link DeterministicAutomaton.FullIntAbstraction full integer abstractions} of a {@link
     * UniversalDeterministicAutomaton}.
     *
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     *
     * @author Malte Isberner
     */
    interface FullIntAbstraction<T, SP, TP>
            extends IntAbstraction<T, SP, TP>, DeterministicAutomaton.FullIntAbstraction<T> {

        default @Nullable TP getTransitionProperty(int state, int input) {
            T trans = getTransition(state, input);
            if (trans != null) {
                return getTransitionProperty(trans);
            }
            return null;
        }

    }
}

