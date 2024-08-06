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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.commons.util.mappings.MapMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simple transition system. A transition system is a (not necessarily finite) collection of states. For an arbitrary
 * input symbol, each state has a set of successors.
 *
 * @param <S>
 *         state class.
 * @param <I>
 *         symbol class.
 *
 * @author Malte Isberner
 */
public interface SimpleTS<S, I> {

    /**
     * Retrieves the set of successors for the given sequence of input symbols.
     *
     * @param state
     *         the source state.
     * @param input
     *         the sequence of input symbols.
     *
     * @return the set of successors reachable by this input.
     */
    default Set<S> getSuccessors(S state, Iterable<? extends I> input) {
        return getSuccessors(Collections.singleton(state), input);
    }

    /**
     * Retrieves the set of all successors that can be reached from any of the given source states by the specified
     * sequence of input symbols.
     *
     * @param states
     *         the source states.
     * @param input
     *         the sequence of input symbols.
     *
     * @return the set of successors reachable by this input, or <code>null</code> if no successor states are reachable.
     */
    default Set<S> getSuccessors(Collection<? extends S> states, Iterable<? extends I> input) {
        Set<S> current = new HashSet<>(states);
        Set<S> succs = new HashSet<>();

        for (I sym : input) {
            for (S state : current) {
                Set<? extends S> currSuccs = getSuccessors(state, sym);
                succs.addAll(currSuccs);
            }

            Set<S> tmp = current;
            current = succs;
            succs = tmp;
            succs.clear();
        }

        return current;
    }

    /**
     * Retrieves the set of successors for the given input symbol.
     *
     * @param state
     *         the source state.
     * @param input
     *         the input symbol.
     *
     * @return the set of successors reachable by this input.
     */
    Set<S> getSuccessors(S state, I input);

    /**
     * Retrieves the set of all states reachable by the given sequence of input symbols from an initial state. Calling
     * this method is equivalent to <code>getSuccessors(getInitialStates(), input)</code>.
     *
     * @param input
     *         the sequence of input symbols.
     *
     * @return the set of states reachable by this input from an initial state, or <code>null</code> if no successor
     * state is reachable.
     */
    default Set<S> getStates(Iterable<? extends I> input) {
        return getSuccessors(getInitialStates(), input);
    }

    /**
     * Retrieves the set of initial states of the transition system.
     *
     * @return the initial states.
     */
    Set<S> getInitialStates();

    /**
     * Creates a {@link MutableMapping} allowing to associate arbitrary data with this transition system's states. The
     * returned mapping is however only guaranteed to work correctly if the transition system is not modified.
     *
     * @return the mutable mapping
     */
    default <@Nullable V> MutableMapping<S, V> createStaticStateMapping() {
        return new MapMapping<>();
    }

    /**
     * Creates a {@link MutableMapping} allowing to associate arbitrary data with this transition system's states. The
     * returned mapping maintains the association even when the transition system is modified.
     *
     * @return the mutable mapping
     */
    default <@Nullable V> MutableMapping<S, V> createDynamicStateMapping() {
        return new MapMapping<>();
    }

}
