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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A mutable automaton. This interface adds support for non-destructive modifications, i.e., adding and modifying states
 * and transitions. If also removal of states and single transitions (from the set of outgoing transitions) should be
 * removed, then {@link ShrinkableAutomaton} is the adequate interface.
 *
 * @param <S>
 *         state class.
 * @param <I>
 *         input symbol class.
 * @param <T>
 *         transition class.
 * @param <SP>
 *         state property.
 * @param <TP>
 *         transition property.
 *
 * @author Malte Isberner
 */
public interface MutableAutomaton<S, I, T, SP, TP> extends UniversalAutomaton<S, I, T, SP, TP> {

    /**
     * Removes all states and transitions.
     */
    void clear();

    default S addState() {
        return addState(null);
    }

    /**
     * Adds a state to the automaton.
     */
    S addState(@Nullable SP property);

    default S addInitialState() {
        return addInitialState(null);
    }

    default S addInitialState(@Nullable SP property) {
        S state = addState(property);
        setInitial(state, true);
        return state;
    }

    void setInitial(S state, boolean initial);

    void setStateProperty(S state, SP property);

    void setTransitionProperty(T transition, TP property);

    default void addTransitions(S state, I input, Collection<? extends T> transitions) {
        Set<T> newTransitions = new HashSet<>(getTransitions(state, input));
        if (!newTransitions.addAll(transitions)) {
            return;
        }
        setTransitions(state, input, newTransitions);
    }

    void setTransitions(S state, I input, Collection<? extends T> transitions);

    default void removeTransition(S state, I input, T transition) {
        Set<T> transitions = new HashSet<>(getTransitions(state, input));
        if (!transitions.remove(transition)) {
            return;
        }
        setTransitions(state, input, transitions);
    }

    default void removeAllTransitions(S state, I input) {
        setTransitions(state, input, Collections.emptySet());
    }

    void removeAllTransitions(S state);

    default T addTransition(S state, I input, S successor, TP properties) {
        T trans = createTransition(successor, properties);
        addTransition(state, input, trans);
        return trans;
    }

    default void addTransition(S state, I input, T transition) {
        Set<T> transitions = new HashSet<>(getTransitions(state, input));
        if (!transitions.add(transition)) {
            return;
        }
        setTransitions(state, input, transitions);
    }

    T createTransition(S successor, TP properties);

    default T copyTransition(T trans, S succ) {
        TP property = getTransitionProperty(trans);
        return createTransition(succ, property);
    }
}
