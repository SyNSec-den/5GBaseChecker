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
package net.automatalib.serialization.saf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelSerializationProvider;
import net.automatalib.words.Alphabet;

public final class SAFSerializationDFA
        implements InputModelSerializationProvider<Integer, DFA<?, Integer>, DFA<Integer, Integer>> {

    private static final SAFSerializationDFA INSTANCE = new SAFSerializationDFA();

    private SAFSerializationDFA() {}

    public static SAFSerializationDFA getInstance() {
        return INSTANCE;
    }

    @Override
    public InputModelData<Integer, DFA<Integer, Integer>> readModel(InputStream is) throws IOException {
        try (InputStream stream = IOUtil.asUncompressedBufferedNonClosingInputStream(is)) {
            SAFInput in = new SAFInput(stream);
            final CompactDFA<Integer> automaton = in.readNativeDFA();
            return new InputModelData<>(automaton, automaton.getInputAlphabet());
        }
    }

    @Override
    public void writeModel(OutputStream os, DFA<?, Integer> model, Alphabet<Integer> alphabet) throws IOException {
        SAFOutput out = new SAFOutput(os);
        out.writeDFA(model, alphabet);
    }
}
