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

import net.automatalib.automata.concepts.StateOutput;

public interface DeterministicStateOutputTS<S, I, T, O> extends DeterministicOutputTS<S, I, T, O>, StateOutput<S, O> {

    @Override
    default boolean trace(S state, Iterable<? extends I> input, List<? super O> output) {
        S iter = state;

        output.add(getStateOutput(iter));

        for (I sym : input) {
            S succ = getSuccessor(iter, sym);
            if (succ == null) {
                return false;
            }
            O out = getStateOutput(succ);
            output.add(out);
            iter = succ;
        }
        return true;
    }
}
