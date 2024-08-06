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

/**
 * A "universal" transition system, which captures the possibility to assign properties to states and transitions.
 * <p>
 * Generally speaking, these properties capture characteristics which are in general observable from the outside, but
 * not captured by the {@link TransitionSystem} interface. For example, neither is whether a state is initial or not a
 * state property, nor is a transition's successor a transition property.
 * <p>
 * A common example are finite state acceptors (FSAs), such as deterministic finite automata (DFAs). A state can be
 * accepting or non-accepting, thus the state property would likely be a {@link Boolean} signaling acceptance.
 * Transitions have are characterized by their successor state only, thus the transition property would most adequately
 * be realized by the {@link Void} class.
 * <p>
 * In contrast, in a Mealy Machine do not distinguish between accepting or rejecting states, but transitions generate
 * output symbols. The state property would therefore be {@link Void}, but the transition property would be the output
 * produced by this transition.
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
 */
public interface UniversalTransitionSystem<S, I, T, SP, TP> extends TransitionSystem<S, I, T> {

    /**
     * Retrieves the state property for the given state.
     *
     * @param state
     *         the state.
     *
     * @return the corresponding property.
     */
    SP getStateProperty(S state);

    /**
     * Retrieves the transition property for the given state.
     *
     * @param transition
     *         the transition.
     *
     * @return the corresponding property.
     */
    TP getTransitionProperty(T transition);
}
