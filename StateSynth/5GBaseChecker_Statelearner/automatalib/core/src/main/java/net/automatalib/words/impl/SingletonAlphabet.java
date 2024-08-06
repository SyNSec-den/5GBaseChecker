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

import net.automatalib.words.Alphabet;
import net.automatalib.words.abstractimpl.AbstractAlphabet;

/**
 * A specialized implementation for {@link Alphabet}s containing only a single symbol.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class SingletonAlphabet<I> extends AbstractAlphabet<I> implements Alphabet<I> {

    private final I symbol;

    public SingletonAlphabet(I symbol) {
        this.symbol = symbol;
    }

    @Override
    public I getSymbol(int index) {
        if (index == 0) {
            return symbol;
        }
        throw new IllegalArgumentException("Index: " + index + ", size = 1");
    }

    @Override
    public int getSymbolIndex(I symbol) {
        if (Objects.equals(this.symbol, symbol)) {
            return 0;
        }
        throw new IllegalArgumentException("Symbol " + symbol + " is not contained in the alaphabet");
    }

    @Override
    public boolean containsSymbol(I symbol) {
        return Objects.equals(this.symbol, symbol);
    }

    @Override
    public int size() {
        return 1;
    }
}
