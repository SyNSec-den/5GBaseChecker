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
package net.automatalib.serialization.taf.parser;

import java.util.Set;

import net.automatalib.words.Alphabet;

/**
 * Interface for a builder object that takes care of the actual automaton construction during parsing of a TAF file.
 *
 * @author Malte Isberner
 */
interface TAFBuilder {

    void init(Alphabet<String> alphabet);

    void declareState(String identifer, Set<String> options);

    Object finish();
}
