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
 * {@link Alphabet} class that supports adding new symbols.
 *
 * @param <I>
 *         symbol class.
 *
 * @author Malte Isberner
 */
public interface GrowingAlphabet<I> extends Alphabet<I> {

    /**
     * Adds a new symbol to the alphabet. Some alphabets may prevent symbols from being added twice. In this case, the
     * original alphabet remains unchanged, but this is not considered an error.
     *
     * @return the index of the symbol in the alphabet, after adding it.
     */
    int addSymbol(I a);

}
