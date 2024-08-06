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

import net.automatalib.automata.MutableDeterministic;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Default implementations for {@link MutableDeterministic} abstractions.
 */
public interface MutableDeterministicAbstraction {

    class StateIntAbstraction<S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>>
            extends UniversalDeterministicAbstractions.StateIntAbstraction<S, I, T, SP, TP, A>
            implements MutableDeterministic.StateIntAbstraction<I, T, SP, TP> {

        public StateIntAbstraction(A automaton) {
            super(automaton);
        }

        @Override
        public void setStateProperty(int state, SP property) {
            automaton.setStateProperty(intToState(state), property);
        }

        @Override
        public void setTransition(int state, I input, @Nullable T transition) {
            automaton.setTransition(intToState(state), input, transition);
        }

        @Override
        public void setTransition(int state, I input, int successor, TP property) {
            automaton.setTransition(intToState(state), input, safeIntToState(successor), property);
        }

        @Override
        public void setTransitionProperty(T transition, TP property) {
            automaton.setTransitionProperty(transition, property);
        }

        @Override
        public void setInitialState(int state) {
            automaton.setInitialState(safeIntToState(state));
        }

        @Override
        public T createTransition(int successor, TP property) {
            return automaton.createTransition(intToState(successor), property);
        }

        @Override
        public int addIntState() {
            return stateToInt(automaton.addState());
        }

        @Override
        public int addIntState(@Nullable SP property) {
            return stateToInt(automaton.addState(property));
        }

        @Override
        public int addIntInitialState() {
            return stateToInt(automaton.addInitialState());
        }

        @Override
        public int addIntInitialState(@Nullable SP property) {
            return stateToInt(automaton.addInitialState(property));
        }
    }

    class FullIntAbstraction<I, T, SP, TP, A extends MutableDeterministic.StateIntAbstraction<I, T, SP, TP>>
            extends UniversalDeterministicAbstractions.FullIntAbstraction<I, T, SP, TP, A>
            implements MutableDeterministic.FullIntAbstraction<T, SP, TP> {

        public FullIntAbstraction(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
            super(stateAbstraction, numInputs, symMapping);
        }

        @Override
        public void setTransition(int state, int input, @Nullable T transition) {
            stateAbstraction.setTransition(state, intToSym(input), transition);
        }

        @Override
        public void setTransition(int state, int input, int successor, TP property) {
            stateAbstraction.setTransition(state, intToSym(input), successor, property);
        }

        @Override
        public void setStateProperty(int state, SP property) {
            stateAbstraction.setStateProperty(state, property);
        }

        @Override
        public void setTransitionProperty(T transition, TP property) {
            stateAbstraction.setTransitionProperty(transition, property);
        }

        @Override
        public void setInitialState(int state) {
            stateAbstraction.setInitialState(state);
        }

        @Override
        public T createTransition(int successor, TP property) {
            return stateAbstraction.createTransition(successor, property);
        }

        @Override
        public int addIntState() {
            return stateAbstraction.addIntState();
        }

        @Override
        public int addIntState(@Nullable SP property) {
            return stateAbstraction.addIntState(property);
        }

        @Override
        public int addIntInitialState() {
            return stateAbstraction.addIntInitialState();
        }

        @Override
        public int addIntInitialState(@Nullable SP property) {
            return stateAbstraction.addIntInitialState(property);
        }
    }
}
