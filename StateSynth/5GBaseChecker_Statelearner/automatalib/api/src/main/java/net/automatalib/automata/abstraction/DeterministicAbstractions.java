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
package net.automatalib.automata.abstraction;

import java.util.function.IntFunction;

import net.automatalib.automata.DeterministicAutomaton;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Default implementations for {@link DeterministicAutomaton} abstractions.
 */
public interface DeterministicAbstractions {

    class StateIntAbstraction<S, I, T, A extends DeterministicAutomaton<S, I, T>>
            extends SimpleDeterministicAbstractions.StateIntAbstraction<S, I, A>
            implements DeterministicAutomaton.StateIntAbstraction<I, T> {

        public StateIntAbstraction(A automaton) {
            super(automaton);
        }

        @Override
        public int getIntSuccessor(T transition) {
            return stateToInt(automaton.getSuccessor(transition));
        }

        @Override
        public @Nullable T getTransition(int state, I input) {
            return automaton.getTransition(intToState(state), input);
        }
    }

    class FullIntAbstraction<I, T, A extends DeterministicAutomaton.StateIntAbstraction<I, T>>
            extends SimpleDeterministicAbstractions.FullIntAbstraction<I, A>
            implements DeterministicAutomaton.FullIntAbstraction<T> {

        public FullIntAbstraction(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
            super(stateAbstraction, numInputs, symMapping);
        }

        @Override
        public @Nullable T getTransition(int state, int input) {
            return stateAbstraction.getTransition(state, intToSym(input));
        }

        @Override
        public int getIntSuccessor(T transition) {
            return stateAbstraction.getIntSuccessor(transition);
        }
    }
}
