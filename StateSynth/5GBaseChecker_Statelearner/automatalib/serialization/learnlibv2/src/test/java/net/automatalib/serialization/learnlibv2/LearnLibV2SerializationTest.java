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
package net.automatalib.serialization.learnlibv2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.io.UnclosableInputStream;
import net.automatalib.commons.util.io.UnclosableOutputStream;
import net.automatalib.serialization.InputModelData;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class LearnLibV2SerializationTest {

    private CompactDFA<Integer> automaton;

    @BeforeClass
    public void setUp() {
        this.automaton = RandomAutomata.randomDFA(new Random(0), 20, Alphabets.integers(0, 2));
    }

    @Test
    public void outputEqualsInputTest() throws IOException {
        final Alphabet<Integer> alphabet = this.automaton.getInputAlphabet();
        final InputModelData<Integer, DFA<Integer, Integer>> deserializedData = writeAndRead(this.automaton, alphabet);

        final DFA<Integer, Integer> deserializedAutomaton = deserializedData.model;
        final Alphabet<Integer> deserializedAlphabet = deserializedData.alphabet;

        Assert.assertEquals(alphabet, deserializedAlphabet);
        Assert.assertTrue(Automata.testEquivalence(automaton, deserializedAutomaton, alphabet));
    }

    @Test
    public void noInitialTest() {
        final CompactDFA<Integer> automaton = new CompactDFA<>(this.automaton);
        final Alphabet<Integer> alphabet = automaton.getInputAlphabet();

        for (Integer s : automaton) {
            automaton.setInitial(s, false);
        }

        Assert.assertThrows(IllegalArgumentException.class, () -> writeAndRead(automaton, alphabet));
    }

    @Test
    public void doNotCloseInputOutputStreamTest() throws IOException {
        final Alphabet<Integer> alphabet = this.automaton.getInputAlphabet();
        LearnLibV2Serialization serializer = LearnLibV2Serialization.getInstance();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.writeModel(new UnclosableOutputStream(baos), automaton, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        serializer.readModel(new UnclosableInputStream(is));
    }

    private InputModelData<Integer, DFA<Integer, Integer>> writeAndRead(DFA<Integer, Integer> automaton,
                                                                        Alphabet<Integer> alphabet) throws IOException {
        LearnLibV2Serialization serializer = LearnLibV2Serialization.getInstance();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.writeModel(baos, automaton, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return serializer.readModel(is);
    }
}
