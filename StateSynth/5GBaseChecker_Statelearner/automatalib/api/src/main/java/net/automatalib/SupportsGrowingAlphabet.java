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
package net.automatalib;

import net.automatalib.exception.GrowingAlphabetNotSupportedException;
import net.automatalib.words.GrowingAlphabet;

/**
 * Interface for declaring that a data structure supports adding new alphabet symbols after its instantiation.
 *
 * @param <I>
 *         input alphabet type
 *
 * @author frohme
 */
public interface SupportsGrowingAlphabet<I> {

    /**
     * Notifies the data structure that a new symbol should be added to the alphabet. Behavior depends on the
     * implementation:
     * <ul>
     * <li>After adding a new symbol, the symbol-related data may either be initialized with default values or
     * undefined.</li>
     * <li>Duplicate symbols may: (1) be handled accordingly, (2) be ignored or (3) result in an error.</li>
     * </ul>
     * Some data structures may need to be properly initialized (e.g. with a {@link GrowingAlphabet}) to handle
     * potentially shared state across multiple instances. If the needed requirements are not met, a {@link
     * GrowingAlphabetNotSupportedException} can be thrown.
     *
     * @param symbol
     *         the symbol to add to the alphabet.
     *
     * @throws GrowingAlphabetNotSupportedException
     *         if the data structure was not properly initialized (e.g. with a {@link GrowingAlphabet}).
     */
    void addAlphabetSymbol(I symbol);

}
