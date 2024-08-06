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
package net.automatalib.ts.output;

import java.util.List;

import net.automatalib.automata.concepts.TransitionOutput;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface DeterministicTransitionOutputTS<S, I, T, O>
        extends DeterministicOutputTS<S, I, T, O>, TransitionOutput<T, O> {

    /**
     * Retrieves the output for the given input symbol in the given state. This is roughly equivalent to calling {@link
     * #getTransitionOutput(Object)} on the transition returned by {@link #getTransition(Object, Object)}, however it
     * should be noted that this function does not allow distinguishing between a <code>null</code> output and an
     * undefined transition.
     *
     * @param state
     *         the source state
     * @param input
     *         the input symbol
     *
     * @return the output symbol (or <code>null</code> if the transition is undefined)
     */
    default @Nullable O getOutput(S state, I input) {
        T trans = getTransition(state, input);
        if (trans == null) {
            return null;
        }
        return getTransitionOutput(trans);
    }

    @Override
    default boolean trace(S state, Iterable<? extends I> input, List<? super O> output) {
        S iter = state;

        for (I sym : input) {
            T trans = getTransition(iter, sym);
            if (trans == null) {
                return false;
            }
            O out = getTransitionOutput(trans);
            output.add(out);
            iter = getSuccessor(trans);
        }
        return true;
    }
}
