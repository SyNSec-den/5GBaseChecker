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

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.simple.SimpleDeterministicAutomaton;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Default implementations for {@link SimpleDeterministicAutomaton} abstractions.
 */
public interface SimpleDeterministicAbstractions {

    /**
     * Base class implementing the default way of obtaining an integer abstraction from an automaton, i.e., by mapping
     * states to integers and vice versa using the {@link StateIDs} mapping obtained via {@link
     * SimpleDeterministicAutomaton#stateIDs()}.
     *
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @author Malte Isberner
     */
    class IntAbstraction<S, A extends SimpleDeterministicAutomaton<S, ?>>
            implements SimpleDeterministicAutomaton.IntAbstraction {

        protected final A automaton;
        protected final StateIDs<S> stateIds;

        public IntAbstraction(A automaton) {
            this.automaton = automaton;
            this.stateIds = automaton.stateIDs();
        }

        @Override
        public int size() {
            return automaton.size();
        }

        protected final S intToState(int stateId) {
            return stateIds.getState(stateId);
        }

        protected final @Nullable S safeIntToState(int stateId) {
            return (stateId == INVALID_STATE) ? null : intToState(stateId);
        }

        @Override
        public int getIntInitialState() {
            return safeStateToInt(automaton.getInitialState());
        }

        protected final int stateToInt(S state) {
            return stateIds.getStateId(state);
        }

        protected final int safeStateToInt(@Nullable S state) {
            return (state == null) ? INVALID_STATE : stateToInt(state);
        }
    }

    /**
     * Base class implementing the default way of obtaining a {@link SimpleDeterministicAutomaton.StateIntAbstraction}.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @author Malte Isberner
     * @see IntAbstraction
     */
    class StateIntAbstraction<S, I, A extends SimpleDeterministicAutomaton<S, I>> extends IntAbstraction<S, A>
            implements SimpleDeterministicAutomaton.StateIntAbstraction<I> {

        public StateIntAbstraction(A automaton) {
            super(automaton);
        }

        @Override
        public int getSuccessor(int state, I input) {
            return safeStateToInt(automaton.getSuccessor(intToState(state), input));
        }
    }

    /**
     * Base class implementing the default way of obtaining a {@link FullIntAbstraction}, i.e., building on top of a
     * {@link StateIntAbstraction} and a mapping from integers to (concrete) input symbols.
     *
     * @param <I>
     *         input symbol type
     * @param <A>
     *         state abstraction type
     *
     * @author Malte Isberner
     */
    class FullIntAbstraction<I, A extends SimpleDeterministicAutomaton.StateIntAbstraction<I>>
            implements SimpleDeterministicAutomaton.FullIntAbstraction {

        protected final A stateAbstraction;
        protected final int numInputs;
        protected final IntFunction<? extends I> symMapping;

        public FullIntAbstraction(A stateAbstraction, int numInputs, IntFunction<? extends I> symMapping) {
            this.stateAbstraction = stateAbstraction;
            this.numInputs = numInputs;
            this.symMapping = symMapping;
        }

        @Override
        public int getSuccessor(int state, int input) {
            return stateAbstraction.getSuccessor(state, intToSym(input));
        }

        @Override
        public int numInputs() {
            return numInputs;
        }

        protected final I intToSym(int input) {
            return symMapping.apply(input); // TODO range checks?
        }

        @Override
        public int size() {
            return stateAbstraction.size();
        }

        @Override
        public int getIntInitialState() {
            return stateAbstraction.getIntInitialState();
        }
    }
}
