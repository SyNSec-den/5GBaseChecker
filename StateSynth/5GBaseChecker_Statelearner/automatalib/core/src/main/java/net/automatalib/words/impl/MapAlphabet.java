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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import net.automatalib.words.abstractimpl.AbstractAlphabet;

/**
 * A map-based alphabet implementation, that does not impose any restriction on the input symbol class. This
 * implementation stores the alphabet symbols in a {@link List} for fast idx to symbol look-ups as well as a {@link Map}
 * for fast symbol to idx look-ups.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class MapAlphabet<I> extends AbstractAlphabet<I> {

    protected final List<I> symbols;

    //private final TObjectIntMap<I> indexMap = new TObjectIntHashMap<I>(10, 0.75f, -1);
    protected final Map<I, Integer> indexMap; // TODO: replace by primitive specialization

    public MapAlphabet(Collection<? extends I> symbols) {
        this.symbols = new ArrayList<>(symbols);
        this.indexMap = Maps.newHashMapWithExpectedSize(symbols.size()); // TODO: replace by primitive specialization
        int i = 0;
        for (I sym : this.symbols) {
            indexMap.put(sym, i++);
        }
    }

    MapAlphabet() {
        this.symbols = new ArrayList<>();
        this.indexMap = new HashMap<>(); // TODO: replace by primitive specialization
    }

    @Override
    public int size() {
        return symbols.size();
    }

    @Override
    public I getSymbol(int index) {
        return symbols.get(index);
    }

    @Override
    public int getSymbolIndex(I symbol) {
        final Integer result = indexMap.get(symbol);
        if (result == null) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is not contained in the alphabet");
        }
        return result;
    }

    @Override
    public boolean containsSymbol(I symbol) {
        return indexMap.containsKey(symbol);
    }
}
