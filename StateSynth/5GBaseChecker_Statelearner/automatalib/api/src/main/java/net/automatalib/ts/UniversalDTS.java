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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Universal deterministic transition system.
 *
 * @author Malte Isberner
 * @see UniversalTransitionSystem
 * @see DeterministicTransitionSystem
 */
public interface UniversalDTS<S, I, T, SP, TP>
        extends UniversalTransitionSystem<S, I, T, SP, TP>, DeterministicTransitionSystem<S, I, T> {

    /**
     * Retrieves the transition property of the outgoing transition corresponding to the given state and input, if it
     * exists. Otherwise, {@code null} is returned.
     * <p>
     * Note that this method alone is insufficient for determining whether a transition actually exists, as {@code null}
     * might either be property of an existing transition, or indicate that the transition does not exist.
     *
     * @param state
     *         the source state
     * @param input
     *         the input symbol
     *
     * @return the property of the outgoing transition, or {@code null}
     */
    default @Nullable TP getTransitionProperty(S state, I input) {
        T trans = getTransition(state, input);
        if (trans != null) {
            return getTransitionProperty(trans);
        }
        return null;
    }
}
