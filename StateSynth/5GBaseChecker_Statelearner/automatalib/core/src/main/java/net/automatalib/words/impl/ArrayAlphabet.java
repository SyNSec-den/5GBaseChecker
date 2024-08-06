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

import java.util.Objects;

import net.automatalib.words.abstractimpl.AbstractAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ArrayAlphabet<I> extends AbstractAlphabet<I> {

    protected final I[] symbols;

    @SafeVarargs
    public ArrayAlphabet(I... symbols) {
        this.symbols = symbols;
    }

    @Override
    public I getSymbol(int index) {
        if (index < 0 || index >= symbols.length) {
            throw new IllegalArgumentException("Index not within its expected bounds");
        }
        return symbols[index];
    }

    @Override
    public int getSymbolIndex(I symbol) {
        final int idx = getSymbolIndexInternal(symbol);

        if (idx >= 0) {
            return idx;
        }

        throw new IllegalArgumentException("Alphabet does not contain the queried symbol");
    }

    private int getSymbolIndexInternal(I symbol) {
        for (int i = 0; i < symbols.length; i++) {
            if (Objects.equals(symbols[i], symbol)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void writeToArray(int offset, @Nullable Object[] array, int tgtOfs, int num) {
        System.arraycopy(symbols, offset, array, tgtOfs, num);
    }

    @Override
    public boolean containsSymbol(I symbol) {
        return getSymbolIndexInternal(symbol) != -1;
    }

    @Override
    public int size() {
        return symbols.length;
    }

}
