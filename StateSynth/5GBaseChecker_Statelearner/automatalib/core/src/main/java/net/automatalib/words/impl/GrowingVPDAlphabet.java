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
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.abstractimpl.AbstractAlphabet;
import net.automatalib.words.abstractimpl.AbstractVPDAlphabet;

/**
 * A {@link VPDAlphabet} implementation that allows to add new symbols after its construction. Wraps input symbols in a
 * {@link VPDSym} instance to allow faster mappings from symbols to indexes.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class GrowingVPDAlphabet<I> extends AbstractVPDAlphabet<VPDSym<I>> implements VPDAlphabet<VPDSym<I>> {

    private final List<VPDSym<I>> allSyms;
    private final List<VPDSym<I>> callSyms;
    private final List<VPDSym<I>> internalSyms;
    private final List<VPDSym<I>> returnSyms;

    public GrowingVPDAlphabet() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private GrowingVPDAlphabet(List<VPDSym<I>> internalSyms, List<VPDSym<I>> callSyms, List<VPDSym<I>> returnSyms) {
        super(new AlphabetView<>(internalSyms), new AlphabetView<>(callSyms), new AlphabetView<>(returnSyms));
        this.internalSyms = internalSyms;
        this.callSyms = callSyms;
        this.returnSyms = returnSyms;
        this.allSyms = new ArrayList<>();
    }

    public VPDSym<I> addNewSymbol(final I userObject, final SymbolType type) {
        final List<VPDSym<I>> localList;
        switch (type) {
            case CALL:
                localList = callSyms;
                break;
            case RETURN:
                localList = returnSyms;
                break;
            default:
                localList = internalSyms;
                break;
        }

        final VPDSym<I> vpdSym = new VPDSym<>(userObject, type, localList.size(), allSyms.size());
        allSyms.add(vpdSym);
        localList.add(vpdSym);

        return vpdSym;
    }

    @Override
    public SymbolType getSymbolType(VPDSym<I> symbol) {
        return symbol.getType();
    }

    @Override
    public int size() {
        return allSyms.size();
    }

    @Override
    public VPDSym<I> getSymbol(int index) {
        return allSyms.get(index);
    }

    @Override
    public int getSymbolIndex(VPDSym<I> symbol) {
        if (!containsSymbol(symbol)) {
            throw new IllegalArgumentException();
        }

        return symbol.getGlobalIndex();
    }

    @Override
    public boolean containsSymbol(VPDSym<I> symbol) {
        final int idx = symbol.getGlobalIndex();
        return idx < allSyms.size() && allSyms.get(idx) == symbol;
    }

    private static class AlphabetView<I> extends AbstractAlphabet<VPDSym<I>> implements Alphabet<VPDSym<I>> {

        private final List<VPDSym<I>> list;

        AlphabetView(List<VPDSym<I>> list) {
            this.list = list;
        }

        @Override
        public VPDSym<I> getSymbol(int index) {
            return list.get(index);
        }

        @Override
        public int getSymbolIndex(VPDSym<I> symbol) {
            if (!containsSymbol(symbol)) {
                throw new IllegalArgumentException();
            }

            return symbol.getLocalIndex();
        }

        @Override
        public boolean containsSymbol(VPDSym<I> symbol) {
            final int idx = symbol.getLocalIndex();
            return idx < list.size() && list.get(idx) == symbol;
        }

        @Override
        public int size() {
            return list.size();
        }
    }
}
