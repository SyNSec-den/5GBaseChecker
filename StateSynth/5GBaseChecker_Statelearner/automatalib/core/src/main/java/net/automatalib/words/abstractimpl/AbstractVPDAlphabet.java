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
package net.automatalib.words.abstractimpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;

/**
 * Abstract utility class that implements functionality shared across different subtypes.
 *
 * @author frohme
 */
public abstract class AbstractVPDAlphabet<I> extends AbstractAlphabet<I> implements VPDAlphabet<I> {

    private final Alphabet<I> internalAlphabet;
    private final Alphabet<I> callAlphabet;
    private final Alphabet<I> returnAlphabet;

    public AbstractVPDAlphabet(Alphabet<I> internalAlphabet, Alphabet<I> callAlphabet, Alphabet<I> returnAlphabet) {
        validateDisjointness(internalAlphabet, SymbolType.INTERNAL, callAlphabet, returnAlphabet);
        validateDisjointness(callAlphabet, SymbolType.CALL, returnAlphabet);

        this.internalAlphabet = internalAlphabet;
        this.callAlphabet = callAlphabet;
        this.returnAlphabet = returnAlphabet;
    }

    @Override
    public Alphabet<I> getCallAlphabet() {
        return callAlphabet;
    }

    @Override
    public I getCallSymbol(int index) {
        return callAlphabet.getSymbol(index);
    }

    @Override
    public int getCallSymbolIndex(I symbol) {
        return callAlphabet.getSymbolIndex(symbol);
    }

    @Override
    public int getNumCalls() {
        return callAlphabet.size();
    }

    @Override
    public Alphabet<I> getInternalAlphabet() {
        return internalAlphabet;
    }

    @Override
    public I getInternalSymbol(int index) {
        return internalAlphabet.getSymbol(index);
    }

    @Override
    public int getInternalSymbolIndex(I symbol) {
        return internalAlphabet.getSymbolIndex(symbol);
    }

    @Override
    public int getNumInternals() {
        return internalAlphabet.size();
    }

    @Override
    public Alphabet<I> getReturnAlphabet() {
        return returnAlphabet;
    }

    @Override
    public I getReturnSymbol(int index) {
        return returnAlphabet.getSymbol(index);
    }

    @Override
    public int getReturnSymbolIndex(I symbol) {
        return returnAlphabet.getSymbolIndex(symbol);
    }

    @Override
    public int getNumReturns() {
        return returnAlphabet.size();
    }

    @Override
    public SymbolType getSymbolType(I symbol) {
        if (internalAlphabet.containsSymbol(symbol)) {
            return SymbolType.INTERNAL;
        } else if (callAlphabet.containsSymbol(symbol)) {
            return SymbolType.CALL;
        } else if (returnAlphabet.containsSymbol(symbol)) {
            return SymbolType.RETURN;
        } else {
            throw new IllegalArgumentException("Symbol is not contained in this alphabet");
        }
    }

    @Override
    public boolean isCallSymbol(I symbol) {
        return this.callAlphabet.containsSymbol(symbol);
    }

    @Override
    public boolean isInternalSymbol(I symbol) {
        return this.internalAlphabet.containsSymbol(symbol);
    }

    @Override
    public boolean isReturnSymbol(I symbol) {
        return this.returnAlphabet.containsSymbol(symbol);
    }

    @Override
    public int size() {
        return internalAlphabet.size() + callAlphabet.size() + returnAlphabet.size();
    }

    @Override
    public I getSymbol(int index) {
        int localIndex = index;

        if (localIndex < internalAlphabet.size()) {
            return internalAlphabet.getSymbol(localIndex);
        } else {
            localIndex -= internalAlphabet.size();
        }

        if (localIndex < callAlphabet.size()) {
            return callAlphabet.getSymbol(localIndex);
        } else {
            localIndex -= callAlphabet.size();
        }

        if (localIndex < returnAlphabet.size()) {
            return returnAlphabet.getSymbol(localIndex);
        } else {
            throw new IllegalArgumentException("Index not within its expected bounds");
        }
    }

    @Override
    public int getSymbolIndex(I symbol) {
        int offset = 0;

        if (internalAlphabet.containsSymbol(symbol)) {
            return internalAlphabet.getSymbolIndex(symbol);
        } else {
            offset += internalAlphabet.size();
        }

        if (callAlphabet.containsSymbol(symbol)) {
            return offset + callAlphabet.getSymbolIndex(symbol);
        } else {
            offset += callAlphabet.size();
        }

        if (returnAlphabet.containsSymbol(symbol)) {
            return offset + returnAlphabet.getSymbolIndex(symbol);
        } else {
            throw new IllegalArgumentException("Alphabet does not contain the queried symbol");
        }
    }

    @Override
    public boolean containsSymbol(I symbol) {
        return internalAlphabet.containsSymbol(symbol) || callAlphabet.containsSymbol(symbol) ||
               returnAlphabet.containsSymbol(symbol);
    }

    @SafeVarargs
    private static <I> void validateDisjointness(Collection<I> source,
                                                 VPDAlphabet.SymbolType type,
                                                 Collection<I>... rest) {
        final Set<I> sourceAsSet = new HashSet<>(source);
        final int initialSize = sourceAsSet.size();

        for (Collection<I> c : rest) {
            sourceAsSet.removeAll(c);
        }

        if (sourceAsSet.size() < initialSize) {
            throw new IllegalArgumentException(
                    "The set of " + type + " symbols is not disjoint with the sets of other symbols.");
        }
    }
}
