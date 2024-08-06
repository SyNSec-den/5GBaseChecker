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
package net.automatalib.exception;

import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;

/**
 * An exception to be thrown when functionality revolving around {@link GrowingAlphabet#addSymbol(Object)} is accessed
 * (e.g. by trying to add an alphabet symbol to an automaton) but the given data structure was not properly set up (e.g.
 * the automaton was initialized with an immutable alphabet).
 *
 * @author frohme
 */
public class GrowingAlphabetNotSupportedException extends UnsupportedOperationException {

    public <I> GrowingAlphabetNotSupportedException(Alphabet<I> alphabet) {
        super("The alphabet '" + alphabet + "' does not support growing");
    }

}