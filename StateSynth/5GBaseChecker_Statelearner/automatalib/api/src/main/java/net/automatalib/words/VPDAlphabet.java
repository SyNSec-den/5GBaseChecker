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

/**
 * Alphabet definition for visible push-down automata. Partitions the overall input alphabet into call-, internal-, and
 * return symbols.
 *
 * @param <I>
 *         input alphabet type
 *
 * @author Malte Isberner
 */
public interface VPDAlphabet<I> extends Alphabet<I> {

    Alphabet<I> getCallAlphabet();

    /**
     * The {@link Alphabet#getSymbol(int)} variant for the call alphabet.
     *
     * @param index
     *         the index of the symbol
     *
     * @throws IllegalArgumentException
     *         if there is no symbol with this index
     */
    I getCallSymbol(int index);

    /**
     * The {@link Alphabet#getSymbolIndex(Object)} variant for the call alphabet.
     *
     * @param symbol
     *         the symbol whose index should be determined
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the call alphabet.
     */
    int getCallSymbolIndex(I symbol);

    int getNumCalls();

    Alphabet<I> getInternalAlphabet();

    /**
     * The {@link Alphabet#getSymbol(int)} variant for the internal alphabet.
     *
     * @param index
     *         the index of the symbol
     *
     * @throws IllegalArgumentException
     *         if there is no symbol with this index
     */
    I getInternalSymbol(int index);

    /**
     * The {@link Alphabet#getSymbolIndex(Object)} variant for the internal alphabet.
     *
     * @param symbol
     *         the symbol whose index should be determined
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the internal alphabet.
     */
    int getInternalSymbolIndex(I symbol);

    int getNumInternals();

    Alphabet<I> getReturnAlphabet();

    /**
     * The {@link Alphabet#getSymbol(int)} variant for the return alphabet.
     *
     * @param index
     *         the index of the symbol
     *
     * @throws IllegalArgumentException
     *         if there is no symbol with this index
     */
    I getReturnSymbol(int index);

    /**
     * The {@link Alphabet#getSymbolIndex(Object)} variant for the return alphabet.
     *
     * @param symbol
     *         the symbol whose index should be determined
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the return alphabet.
     */
    int getReturnSymbolIndex(I symbol);

    int getNumReturns();

    /**
     * Returns the {@link SymbolType symbol type} of the given alphabet symbol.
     *
     * @param symbol
     *         the symbol whose type should be returned
     *
     * @return the {@link SymbolType symbol type} of the given alphabet symbol.
     *
     * @throws IllegalArgumentException
     *         if the provided symbol does not belong to the alphabet.
     */
    SymbolType getSymbolType(I symbol);

    default int callReturnBalance(Word<I> word) {
        int crb = 0;
        for (final I sym : word) {
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    crb--;
                    break;
                default:
            }
        }
        return crb;
    }

    default boolean isCallMatched(Word<I> word) {
        int crb = 0;
        for (final I sym : word) {
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    if (crb > 0) {
                        crb--;
                    }
                    break;
                default:
            }
        }
        return crb == 0;
    }

    default boolean isCallSymbol(I symbol) {
        return getSymbolType(symbol) == SymbolType.CALL;
    }

    default boolean isInternalSymbol(I symbol) {
        return getSymbolType(symbol) == SymbolType.INTERNAL;
    }

    default boolean isReturnMatched(Word<I> word) {
        int crb = 0;
        for (final I sym : word) {
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    crb--;
                    if (crb < 0) {
                        return false;
                    }
                    break;
                default:
            }
        }

        return true;
    }

    default boolean isReturnSymbol(I symbol) {
        return getSymbolType(symbol) == SymbolType.RETURN;
    }

    default boolean isWellMatched(Word<I> word) {
        int crb = 0;
        for (I sym : word) {
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    crb--;
                    if (crb < 0) {
                        return false;
                    }
                    break;
                default:
            }
        }

        return crb == 0;
    }

    default Word<I> longestWellMatchedPrefix(Word<I> word) {
        int idx = 0;
        int len = word.length();
        int crb = 0;
        int lastzero = 0;
        outer:
        while (idx < len) {
            final I sym = word.getSymbol(idx);
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    break;
                case RETURN:
                    crb--;
                    if (crb < 0) {
                        break outer;
                    }
                    break;
                default:
            }
            if (crb == 0) {
                lastzero = idx + 1;
            }
            idx++;
        }
        return word.prefix(lastzero);
    }

    default Word<I> longestWellMatchedSuffix(Word<I> word) {
        int idx = word.length();
        int crb = 0;
        int lastZero = idx;
        outer:
        while (idx > 0) {
            final I sym = word.getSymbol(--idx);
            switch (getSymbolType(sym)) {
                case CALL:
                    crb++;
                    if (crb > 0) {
                        break outer;
                    }
                    break;
                case RETURN:
                    crb--;
                    break;
                default:
            }
            if (crb == 0) {
                lastZero = idx;
            }
        }
        return word.subWord(lastZero);
    }

    enum SymbolType {
        CALL,
        INTERNAL,
        RETURN
    }

}
