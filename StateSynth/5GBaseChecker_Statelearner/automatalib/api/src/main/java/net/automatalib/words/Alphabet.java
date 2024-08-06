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
package net.automatalib.words;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import net.automatalib.commons.smartcollections.ArrayWritable;
import net.automatalib.commons.util.mappings.Mapping;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Class implementing an (indexed) alphabet. An alphabet is a collection of symbols, where each symbol has a (unique)
 * index. Apart from serving as a collection, this class also provides a one-to-one mapping between symbols and
 * indices.
 *
 * @param <I>
 *         symbol type
 *
 * @author Malte Isberner
 */
public interface Alphabet<I> extends ArrayWritable<I>, Collection<I>, Comparator<I>, IntFunction<I>, ToIntFunction<I> {

    @Override
    default I apply(int index) {
        return getSymbol(index);
    }

    /**
     * Returns the symbol with the given index in this alphabet.
     *
     * @param index
     *         the index of the requested symbol.
     *
     * @return symbol with the given index.
     *
     * @throws IllegalArgumentException
     *         if there is no symbol with this index.
     */
    I getSymbol(int index);

    @Override
    default int applyAsInt(I symbol) {
        return getSymbolIndex(symbol);
    }

    /**
     * Returns the index of the given symbol in the alphabet.
     *
     * @param symbol
     *         the symbol whose index should be determined
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the alphabet.
     */
    int getSymbolIndex(I symbol);

    @Override
    default int compare(I o1, I o2) {
        return getSymbolIndex(o1) - getSymbolIndex(o2);
    }

    @Override
    default void writeToArray(int offset, @Nullable Object[] array, int tgtOfs, int num) {
        for (int i = 0; i < num; i++) {
            array[tgtOfs + i] = getSymbol(offset + i);
        }
    }

    default <I2> Mapping<I2, I> translateFrom(Alphabet<I2> other) {
        if (other.size() > size()) {
            throw new IllegalArgumentException(
                    "Cannot translate from an alphabet with " + other.size() + " elements into an alphabet with only " +
                    size() + " elements");
        }
        return i -> getSymbol(other.getSymbolIndex(i));
    }

    /**
     * Checks whether the given symbol is part of the alphabet.
     * <p>
     * <b>Caution:</b> the default implementation is rather inefficient and should be overridden, if possible.
     *
     * @param symbol
     *         the symbol to check
     *
     * @return {@code true} iff the symbol is part of the alphabet
     */
    default boolean containsSymbol(I symbol) {
        try {
            int index = getSymbolIndex(symbol);
            if (index < 0 || index >= size()) {
                return false;
            }
            return Objects.equals(symbol, getSymbol(index));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
