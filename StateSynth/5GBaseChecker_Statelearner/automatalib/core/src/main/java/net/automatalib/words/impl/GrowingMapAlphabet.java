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

import java.util.Collection;

import net.automatalib.words.GrowingAlphabet;

/**
 * An extension of the {@link MapAlphabet} that also allows adding new symbol after construction.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class GrowingMapAlphabet<I> extends MapAlphabet<I> implements GrowingAlphabet<I> {

    public GrowingMapAlphabet() {
        super();
    }

    public GrowingMapAlphabet(Collection<? extends I> symbols) {
        super(symbols);
    }

    @Override
    public boolean add(I a) {
        int s = size();
        int idx = addSymbol(a);
        return idx == s;
    }

    @Override
    public int addSymbol(I a) {
        //int idx = indexMap.get(a);
        //if(idx != -1)
        Integer idx = indexMap.get(a); // TODO: replace by primitive specialization
        if (idx != null) {
            return idx;
        }
        idx = size();
        symbols.add(a);
        indexMap.put(a, idx);
        return idx;
    }

}
