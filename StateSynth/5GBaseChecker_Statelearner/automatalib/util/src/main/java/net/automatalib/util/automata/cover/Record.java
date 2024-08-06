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
package net.automatalib.util.automata.cover;

import java.util.Collections;
import java.util.Set;

import net.automatalib.words.Word;

/**
 * Utility class, that stores information about access sequences of automaton states.
 *
 * @param <S>
 *         automaton state type
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
final class Record<S, I> {

    final S state;
    final Word<I> accessSequence;
    final Set<I> coveredInputs;

    Record(S state, Word<I> accessSequence) {
        this(state, accessSequence, Collections.emptySet());
    }

    Record(S state, Word<I> accessSequence, Set<I> coveredInputs) {
        this.state = state;
        this.accessSequence = accessSequence;
        this.coveredInputs = coveredInputs;
    }
}
