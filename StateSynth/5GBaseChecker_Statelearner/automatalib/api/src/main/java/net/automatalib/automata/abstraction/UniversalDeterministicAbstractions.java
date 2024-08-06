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

import net.automatalib.automata.UniversalDeterministicAutomaton;

/**
 * Default implementations for {@link UniversalDeterministicAutomaton} abstractions.
 */
public interface UniversalDeterministicAbstractions {

    class StateIntAbstraction<S, I, T, SP, TP, A extends UniversalDeterministicAutomaton<S, I, T, SP, TP>>
            extends DeterministicAbstractions.StateIntAbstraction<S, I, T, A>
            implements UniversalDeterministicAutomaton.StateIntAbstraction<I, T, SP, TP> {

        public StateIntAbstraction(A automaton) {
            super(automaton);
        }

        @Override
        public SP getStateProperty(int state) {
            return automaton.getStateProperty(intToState(state));
        }

        @Override
        public TP getTransitionProperty(T transition) {
            return automaton.getTransitionProperty(transition);
        }
    }

    class FullIntAbstraction<I, T, SP, TP, A extends UniversalDeterministicAutomaton.StateIntAbstraction<I, T, SP, TP>>
            extends DeterministicAbstractions.FullIntAbstraction<I, T, A>
            implements UniversalDeterministicAutomaton.FullIntAbstraction<T, SP, TP> {

        public FullIntAbstraction(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
            super(stateAbstraction, numInputs, symMapping);
        }

        @Override
        public SP getStateProperty(int state) {
            return stateAbstraction.getStateProperty(state);
        }

        @Override
        public TP getTransitionProperty(T transition) {
            return stateAbstraction.getTransitionProperty(transition);
        }
    }
}
