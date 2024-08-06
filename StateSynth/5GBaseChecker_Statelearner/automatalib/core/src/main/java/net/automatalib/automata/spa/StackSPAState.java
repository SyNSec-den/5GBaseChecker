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
package net.automatalib.automata.spa;

import net.automatalib.automata.fsa.DFA;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A state in a {@link StackSPA}. Consists of a back-reference to the previous stack-element a values for the current
 * top-of-stack configuration.
 *
 * @param <I>
 *         input symbol type
 * @param <S>
 *         hypothesis state type
 *
 * @author frohme
 */
final class StackSPAState<I, S> {

    private static final StackSPAState<?, ?> INIT = new StackSPAState<>();
    private static final StackSPAState<?, ?> SINK = new StackSPAState<>();
    private static final StackSPAState<?, ?> TERM = new StackSPAState<>();

    private final @Nullable StackSPAState<I, S> prev;
    private final @Nullable DFA<S, I> procedure;
    private final @Nullable S procedureState;

    private StackSPAState() {
        this.prev = null;
        this.procedure = null;
        this.procedureState = null;
    }

    private StackSPAState(StackSPAState<I, S> prev, DFA<S, I> procedure, S procedureState) {
        this.prev = prev;
        this.procedure = procedure;
        this.procedureState = procedureState;
    }

    StackSPAState<I, S> push(DFA<S, I> newProcedure, S newState) {
        return new StackSPAState<>(this, newProcedure, newState);
    }

    StackSPAState<I, S> pop() {
        assert !isStatic() : "This method should never be called on static states";
        return prev;
    }

    StackSPAState<I, S> updateState(S state) {
        assert !isStatic() : "This method should never be called on static states";
        return new StackSPAState<>(prev, procedure, state);
    }

    DFA<S, I> getProcedure() {
        assert !isStatic() : "This method should never be called on static states";
        return procedure;
    }

    S getCurrentState() {
        assert !isStatic() : "This method should never be called on static states";
        return procedureState;
    }

    @SuppressWarnings("unchecked")
    static <I, S> StackSPAState<I, S> sink() {
        return (StackSPAState<I, S>) SINK;
    }

    boolean isSink() {
        return this == SINK;
    }

    @SuppressWarnings("unchecked")
    static <I, S> StackSPAState<I, S> init() {
        return (StackSPAState<I, S>) INIT;
    }

    boolean isInit() {
        return this == INIT;
    }

    @SuppressWarnings("unchecked")
    static <I, S> StackSPAState<I, S> term() {
        return (StackSPAState<I, S>) TERM;
    }

    boolean isTerm() {
        return this == TERM;
    }

    // contract is satisfied by definition of constructors
    @SuppressWarnings("contracts.conditional.postcondition.not.satisfied")
    @EnsuresNonNullIf(expression = {"this.prev", "this.procedure", "this.procedureState"}, result = false)
    private boolean isStatic() {
        return isInit() || isTerm() || isSink();
    }

}
