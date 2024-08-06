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
package net.automatalib.ts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.ts.powerset.DirectPowersetDTS;
import net.automatalib.ts.simple.SimpleTS;

/**
 * Transition system interface. This interface extends {@link SimpleTS} by introducing the concept of inspectable
 * <i>transitions</i>, allowing to associate other information apart from the successor state with each transition.
 *
 * @param <S>
 *         state class
 * @param <I>
 *         input symbol class
 * @param <T>
 *         transition class
 *
 * @author Malte Isberner
 */
public interface TransitionSystem<S, I, T> extends SimpleTS<S, I> {

    @Override
    default Set<S> getSuccessors(S state, I input) {
        Collection<T> transitions = getTransitions(state, input);
        if (transitions.isEmpty()) {
            return Collections.emptySet();
        }
        Set<S> result = new HashSet<>(transitions.size());
        for (T trans : transitions) {
            result.add(getSuccessor(trans));
        }
        return result;
    }

    /**
     * Retrieves the transitions that can be triggered by the given input symbol.
     * <p>
     * The return value must not be {@code null}; if there are no transitions triggered by the specified input, {@link
     * Collections#emptySet()} should be returned.
     *
     * @param state
     *         the source state.
     * @param input
     *         the input symbol.
     *
     * @return the transitions triggered by the given input
     */
    Collection<T> getTransitions(S state, I input);

    /**
     * Retrieves the successor state of a given transition.
     *
     * @param transition
     *         the transition.
     *
     * @return the successor state.
     */
    S getSuccessor(T transition);

    /**
     * Retrieves a "powerset view" of this transition system.
     *
     * @return a powerset view of this transition system.
     */
    default PowersetViewTS<?, I, ?, S, T> powersetView() {
        return new DirectPowersetDTS<>(this);
    }
}
