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
package net.automatalib.automata.words.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.automatalib.commons.util.nid.MutableNumericID;

/**
 * Utility class, aggregating definitions used for testing {@link net.automatalib.words.impl.FastAlphabet}s.
 *
 * @author frohme
 */
public final class FastAlphabetTestUtil {

    public static final List<InputSymbol> ALPHABET_SYMBOLS =
            IntStream.range(0, 5).mapToObj(InputSymbol::new).collect(Collectors.toList());
    public static final List<InputSymbol> NON_ALPHABET_SYMBOLS =
            IntStream.range(5, 8).mapToObj(InputSymbol::new).collect(Collectors.toList());

    private FastAlphabetTestUtil() {
        // prevent instantiation
    }

    public static class InputSymbol implements MutableNumericID {

        private int id;

        public InputSymbol(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public void setId(int id) {
            this.id = id;
        }
    }

}
