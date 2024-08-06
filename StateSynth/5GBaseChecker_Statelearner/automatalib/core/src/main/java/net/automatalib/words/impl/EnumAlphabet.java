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
package net.automatalib.words.impl;

import java.util.Arrays;

public class EnumAlphabet<E extends Enum<E>> extends ArrayAlphabet<E> {

    public EnumAlphabet(Class<E> enumClazz, boolean withNull) {
        super(extractEnumValues(enumClazz, withNull));
    }

    private static <E> E[] extractEnumValues(Class<E> enumClazz, boolean withNull) {
        E[] enumValues = enumClazz.getEnumConstants();
        if (enumValues == null) {
            throw new IllegalArgumentException("Class " + enumClazz.getName() + " is not an enumeration class!");
        }
        if (!withNull) {
            return enumValues;
        }
        return Arrays.copyOf(enumValues, enumValues.length + 1);
    }

    @Override
    public int getSymbolIndex(E symbol) {
        if (symbol == null) {
            int lastIdx = symbols.length - 1;
            if (symbols[lastIdx] == null) {
                return lastIdx;
            }
            throw new IllegalArgumentException("No such symbol: null");
        }
        return symbol.ordinal();
    }

    @Override
    public boolean containsSymbol(E symbol) {
        if (symbol == null) {
            return symbols[symbols.length - 1] == null;
        }

        int index = symbol.ordinal();
        return index < symbols.length && symbols[index] == symbol;
    }

    @Override
    public int compare(E o1, E o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        return o1.compareTo(o2);
    }

}
