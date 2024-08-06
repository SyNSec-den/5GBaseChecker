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
package net.automatalib.serialization.aut;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.serialization.InputModelData;

/**
 * A parser for automata specified in the AUT format (see http://cadp.inria.fr/man/aut.html for further information).
 *
 * @author frohme
 */
public final class AUTParser {

    private AUTParser() {
        // prevent instantiation
    }

    public static InputModelData<String, SimpleAutomaton<Integer, String>> readAutomaton(InputStream is)
            throws IOException {
        return readAutomaton(is, Function.identity());
    }

    public static <I> InputModelData<I, SimpleAutomaton<Integer, I>> readAutomaton(InputStream is,
                                                                                   Function<String, I> inputTransformer)
            throws IOException {
        return new InternalAUTParser(is).parse(inputTransformer);
    }

}
