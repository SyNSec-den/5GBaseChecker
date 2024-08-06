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
package net.automatalib.commons.util.mappings;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Class for transforming integer index values into string values (using latin characters, therefore effectively
 * realizing a radix-26 representation of numbers).
 *
 * @author Malte Isberner
 */
public final class StringIndexMapping {

    private static final int ALPHABET_SIZE = 26;

    private static final char BASE = 'a';

    private StringIndexMapping() {}

    public static long stringToIndex(String sidx) {
        long idx = 0;
        long value = 1;

        for (int i = 0; i < sidx.length(); i++) {
            char c = sidx.charAt(i);
            idx += value * getInteger(c);
            value *= ALPHABET_SIZE;
        }

        return idx;
    }

    public static String indexToString(@NonNegative long idx) {
        final StringBuilder sb = new StringBuilder();

        long idxIter = idx;
        do {
            sb.append(getChar((int) idxIter % ALPHABET_SIZE));
            idxIter /= ALPHABET_SIZE;
        } while (idxIter > 0);

        return sb.toString();
    }

    private static int getInteger(char c) {
        return Character.toLowerCase(c) - BASE;
    }

    private static char getChar(int idx) {
        return (char) (BASE + idx);
    }

}
