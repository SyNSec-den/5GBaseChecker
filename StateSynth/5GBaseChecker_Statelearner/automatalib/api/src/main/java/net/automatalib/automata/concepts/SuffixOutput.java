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

import net.automatalib.words.Word;

/**
 * Feature for automata that compute a <i>suffix-observable</i> output function, i.e., they compute an output containing
 * a part that can be attributed to a suffix of the input.
 * <p>
 * Note that this is a special case of the {@link Output} feature, as <code>computeOutput(input) =
 * computeSuffixOutput(epsilon, input)</code>.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
public interface SuffixOutput<I, D> extends Output<I, D> {

    @Override
    default D computeOutput(Iterable<? extends I> input) {
        return computeSuffixOutput(Word.epsilon(), input);
    }

    D computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix);
}
