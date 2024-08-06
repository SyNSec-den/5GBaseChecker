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
package net.automatalib.automata.concepts;

import net.automatalib.exception.UndefinedPropertyAccessException;

public interface DetSuffixOutputAutomaton<S, I, T, D> extends DetOutputAutomaton<S, I, T, D>, SuffixOutput<I, D> {

    @Override
    default D computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
        final S state = getState(prefix);

        if (state == null) {
            throw new UndefinedPropertyAccessException("The state accessed by " + prefix + " is undefined");
        }

        return computeStateOutput(state, suffix);
    }

    D computeStateOutput(S state, Iterable<? extends I> input);

    @Override
    default D computeOutput(Iterable<? extends I> input) {
        final S state = getInitialState();

        if (state == null) {
            throw new UndefinedPropertyAccessException("No initial state defined");
        }

        return computeStateOutput(state, input);
    }
}
