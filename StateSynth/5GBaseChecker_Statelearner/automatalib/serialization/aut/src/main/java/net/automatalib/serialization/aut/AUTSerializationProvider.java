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
import java.io.OutputStream;
import java.util.function.Function;

import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.automaton.SimpleAutomatonSerializationProvider;
import net.automatalib.words.Alphabet;

public final class AUTSerializationProvider implements SimpleAutomatonSerializationProvider<Integer, String> {

    private static final AUTSerializationProvider INSTANCE = new AUTSerializationProvider();

    private AUTSerializationProvider() {
        // prevent instantiation
    }

    public static AUTSerializationProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public <I2> void writeModel(OutputStream os,
                                SimpleAutomaton<?, I2> model,
                                Alphabet<I2> alphabet,
                                Function<I2, String> inputTransformer) throws IOException {
        AUTWriter.writeAutomaton(model, alphabet, inputTransformer, os);
    }

    @Override
    public <I2> InputModelData<I2, SimpleAutomaton<Integer, I2>> readModel(InputStream is,
                                                                           Function<String, I2> inputTransformer)
            throws IOException {
        return AUTParser.readAutomaton(is, inputTransformer);
    }
}
