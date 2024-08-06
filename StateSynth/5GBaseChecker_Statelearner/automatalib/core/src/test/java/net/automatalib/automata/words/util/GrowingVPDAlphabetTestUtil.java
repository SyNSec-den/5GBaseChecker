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
package net.automatalib.automata.words.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.automatalib.words.VPDAlphabet.SymbolType;
import net.automatalib.words.impl.GrowingVPDAlphabet;
import net.automatalib.words.impl.VPDSym;

/**
 * Utility class, aggregating definitions used for testing {@link net.automatalib.words.GrowingAlphabet}s.
 *
 * @author frohme
 */
public final class GrowingVPDAlphabetTestUtil {

    public static final GrowingVPDAlphabet<Character> ALPHABET;

    public static final List<VPDSym<Character>> INTERNAL_SYMBOLS;
    public static final List<VPDSym<Character>> CALL_SYMBOLS;
    public static final List<VPDSym<Character>> RETURN_SYMBOLS;
    public static final List<VPDSym<Character>> NON_CONTAINED_SYMBOLS;

    public static final List<VPDSym<Character>> JOINED_SYMBOLS;

    static {
        ALPHABET = new GrowingVPDAlphabet<>();

        INTERNAL_SYMBOLS = DefaultVPDAlphabetTestUtil.INTERNAL_SYMBOLS.stream()
                                                                      .map(s -> ALPHABET.addNewSymbol(s,
                                                                                                      SymbolType.INTERNAL))
                                                                      .collect(Collectors.toList());

        CALL_SYMBOLS = DefaultVPDAlphabetTestUtil.CALL_SYMBOLS.stream()
                                                              .map(s -> ALPHABET.addNewSymbol(s, SymbolType.CALL))
                                                              .collect(Collectors.toList());

        RETURN_SYMBOLS = DefaultVPDAlphabetTestUtil.RETURN_SYMBOLS.stream()
                                                                  .map(s -> ALPHABET.addNewSymbol(s, SymbolType.RETURN))
                                                                  .collect(Collectors.toList());

        final GrowingVPDAlphabet<Character> dummyAlphabet = new GrowingVPDAlphabet<>();
        NON_CONTAINED_SYMBOLS = DefaultVPDAlphabetTestUtil.NON_CONTAINED_SYMBOLS.stream()
                                                                                .map(s -> dummyAlphabet.addNewSymbol(s,
                                                                                                                     SymbolType.INTERNAL))
                                                                                .collect(Collectors.toList());

        JOINED_SYMBOLS = new ArrayList<>(DefaultVPDAlphabetTestUtil.JOINED_SYMBOLS.size());
        JOINED_SYMBOLS.addAll(INTERNAL_SYMBOLS);
        JOINED_SYMBOLS.addAll(CALL_SYMBOLS);
        JOINED_SYMBOLS.addAll(RETURN_SYMBOLS);
    }

    private GrowingVPDAlphabetTestUtil() {
        // prevent instantiation
    }
}
