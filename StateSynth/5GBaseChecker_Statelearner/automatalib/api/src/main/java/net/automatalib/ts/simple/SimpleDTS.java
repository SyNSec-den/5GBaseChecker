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
package net.automatalib.ts.simple;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import net.automatalib.ts.TransitionSystem;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simple deterministic transition system. In a deterministic transition system, there exists in each state at most
 * one successor state for each input symbol.
 * <p>
 * <i>Implementation note:</i> It is suggested to use a non-null type for the state class, as {@code null} will be used
 * to denote an undefined successor. Allowing {@code null} to identify a state won't allow you to differentiate between
 * a defined and undefined successor.
 *
 * @param <S>
 *         state class
 * @param <I>
 *         input symbol class
 *
 * @author Malte Isberner
 */
public interface SimpleDTS<S, I> extends SimpleTS<S, I> {

    @Override
    default Set<S> getSuccessors(S state, Iterable<? extends I> input) {
        return stateToSet(getSuccessor(state, input));
    }

    @Override
    default Set<S> getSuccessors(S state, I input) {
        return stateToSet(getSuccessor(state, input));
    }

    @Override
    default Set<S> getStates(Iterable<? extends I> input) {
        return stateToSet(getState(input));
    }

    @Override
    default Set<S> getInitialStates() {
        return stateToSet(getInitialState());
    }

    /**
     * Retrieves the state reachable by the given sequence of input symbols from the initial state.
     *
     * @param input
     *         the input word.
     *
     * @return the state reachable by the given input word, or <code>null</code> if no state is reachable by this word.
     *
     * @see TransitionSystem#getStates(Iterable)
     */
    default @Nullable S getState(Iterable<? extends I> input) {
        final S init = getInitialState();
        return init == null ? null : getSuccessor(init, input);
    }

    /**
     * Retrieves the initial state of this transition system.
     *
     * @return the initial state.
     *
     * @see TransitionSystem#getInitialStates()
     */
    @Nullable S getInitialState();

    static <S> Set<S> stateToSet(@Nullable S state) {
        if (state == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(state);
    }

    /**
     * Retrieves the successor state reachable by the given sequence of input symbols.
     *
     * @param state
     *         the source state.
     * @param input
     *         the input symbol.
     *
     * @return the successor state reachable by the given sequence of input symbols, or <code>null</code> if no state is
     * reachable by this symbol.
     *
     * @see TransitionSystem#getSuccessors(Object, Iterable)
     */
    default @Nullable S getSuccessor(S state, Iterable<? extends I> input) {
        S curr = state;
        Iterator<? extends I> it = input.iterator();

        while (curr != null && it.hasNext()) {
            I sym = it.next();
            curr = getSuccessor(curr, sym);
        }

        return curr;
    }

    /**
     * Retrieves the successor state reachable by the given input symbol.
     *
     * @param state
     *         the source state.
     * @param input
     *         the input symbol.
     *
     * @return the successor state reachable by the given input symbol, or <code>null</code> if no state is reachable by
     * this symbol.
     *
     * @see TransitionSystem#getSuccessors(Object, Object)
     */
    @Nullable S getSuccessor(S state, I input);
}
