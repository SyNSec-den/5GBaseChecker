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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.commons.util.io.UnclosableInputStream;
import net.automatalib.commons.util.io.UnclosableOutputStream;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class SAFSerializationTest {

    private static final Alphabet<Integer> ALPHABET = Alphabets.integers(0, 4);
    private static final int AUTOMATON_SIZE = 20;

    private CompactDFA<Integer> dfa;
    private CompactNFA<Integer> nfa;

    @BeforeMethod
    public void setUp() {
        final Random random = new Random(0);
        this.dfa = RandomAutomata.randomDFA(random, AUTOMATON_SIZE, ALPHABET);

        // remove some transitions for partiality
        for (int i = 0; i < AUTOMATON_SIZE; i++) {
            this.dfa.removeTransition(random.nextInt(AUTOMATON_SIZE),
                                      random.nextInt(ALPHABET.size()),
                                      random.nextInt(AUTOMATON_SIZE));
        }

        this.nfa = new CompactNFA<>(ALPHABET, AUTOMATON_SIZE);

        for (int i = 0; i < AUTOMATON_SIZE; i++) {
            this.nfa.addState(random.nextBoolean());
            this.nfa.setInitial(i, random.nextBoolean());
        }

        for (int i = 0; i < AUTOMATON_SIZE * AUTOMATON_SIZE; i++) {
            this.nfa.addTransition(random.nextInt(AUTOMATON_SIZE),
                                   random.nextInt(ALPHABET.size()),
                                   random.nextInt(AUTOMATON_SIZE));
        }
    }

    @Test
    public void testDFASerialization() throws Exception {
        final SAFSerializationDFA serializer = SAFSerializationDFA.getInstance();

        final DFA<Integer, Integer> deserializedModel = writeAndReadModel(this.dfa, ALPHABET, serializer, serializer);

        Assert.assertTrue(Automata.testEquivalence(this.dfa, deserializedModel, ALPHABET));
    }

    @Test
    public void testNFASerialization() throws Exception {
        final SAFSerializationNFA serializer = SAFSerializationNFA.getInstance();

        final NFA<Integer, Integer> deserializedModel = writeAndReadModel(this.nfa, ALPHABET, serializer, serializer);

        Assert.assertTrue(Automata.testEquivalence(NFAs.determinize(this.nfa, ALPHABET),
                                                   NFAs.determinize(deserializedModel, ALPHABET),
                                                   ALPHABET));
    }

    @Test
    public void doNotCloseInputOutputStreamDFATest() throws IOException {

        final SAFSerializationDFA serializer = SAFSerializationDFA.getInstance();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.writeModel(new UnclosableOutputStream(baos), this.dfa, ALPHABET);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        serializer.readModel(new UnclosableInputStream(is));
    }

    @Test
    public void doNotCloseInputOutputStreamNFATest() throws IOException {

        final SAFSerializationNFA serializer = SAFSerializationNFA.getInstance();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.writeModel(new UnclosableOutputStream(baos), this.nfa, ALPHABET);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        serializer.readModel(new UnclosableInputStream(is));
    }

    private <I, IN extends UniversalAutomaton<?, I, ?, ?, ?>, OUT extends UniversalAutomaton<?, I, ?, ?, ?>> OUT writeAndReadModel(
            IN source,
            Alphabet<I> alphabet,
            InputModelSerializer<I, IN> serializer,
            InputModelDeserializer<I, OUT> deserializer) throws IOException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        serializer.writeModel(baos, source, alphabet);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return deserializer.readModel(is).model;
    }

}
