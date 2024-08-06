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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.google.common.io.ByteStreams;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.commons.util.io.UnclosableInputStream;
import net.automatalib.commons.util.io.UnclosableOutputStream;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.ArrayAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class AUTSerializationTest {

    @Test
    public void quotationLabelTest() throws Exception {
        try (InputStream is = AUTSerializationTest.class.getResourceAsStream("/quotationTest.aut")) {
            final SimpleAutomaton<Integer, String> automaton = AUTParser.readAutomaton(is).model;

            Assert.assertEquals(4, automaton.size());

            final String input1 = "PUT_\\6";
            final String input2 = "GET !true !7 !CONS (A, CONS (B, NIL))";
            final String input3 = "SEND !\"hello\" !\"world\"";

            final Set<Integer> s0 = automaton.getInitialStates();
            final Set<Integer> s1 = automaton.getSuccessors(s0, Collections.singletonList(input1));
            final Set<Integer> s2 = automaton.getSuccessors(s0, Collections.singletonList(input2));
            final Set<Integer> s3 = automaton.getSuccessors(s0, Arrays.asList(input2, input3));

            Assert.assertEquals(Collections.singleton(0), s0);
            Assert.assertEquals(Collections.singleton(1), s1);
            Assert.assertEquals(Collections.singleton(2), s2);
            Assert.assertEquals(Collections.singleton(3), s3);
        }
    }

    @Test
    public void sinkStateTest() throws Exception {
        try (InputStream is = AUTSerializationTest.class.getResourceAsStream("/sinkStateTest.aut")) {
            final SimpleAutomaton<Integer, String> automaton = AUTParser.readAutomaton(is).model;
            Assert.assertEquals(3, automaton.size());

            final Alphabet<String> alphabet = new ArrayAlphabet<String>("input", "output");

            final Set<Integer> s0 = automaton.getInitialStates();
            final Set<Integer> t1 = automaton.getSuccessors(s0, Collections.singletonList("input"));
            final Set<Integer> t2 = automaton.getSuccessors(s0, Collections.singletonList("output"));
            final Set<Integer> t3 = automaton.getSuccessors(s0, Arrays.asList("input", "output"));
            final Set<Integer> t4 = automaton.getSuccessors(s0, Arrays.asList("input", "output", "output"));

            Assert.assertEquals(Collections.singleton(0), s0);
            Assert.assertEquals(Collections.singleton(1), t1);
            Assert.assertEquals(Collections.singleton(2), t2);
            Assert.assertEquals(Collections.singleton(0), t3);
            Assert.assertEquals(Collections.singleton(2), t4);
        }
    }

    @Test
    public void serializationTest() throws Exception {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 2);
        final Random random = new Random(0);
        final DFA<Integer, Integer> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AUTWriter.writeAutomaton(automaton, alphabet, baos);

        final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final SimpleAutomaton<Integer, Integer> deserialized = AUTParser.readAutomaton(is, Integer::parseInt).model;

        equalityTest(automaton, deserialized, alphabet);
    }

    private <S, I> void equalityTest(SimpleAutomaton<S, I> src, SimpleAutomaton<S, I> target, Alphabet<I> inputs) {
        for (final S s : src.getStates()) {
            for (final I i : inputs) {
                Assert.assertEquals(src.getSuccessors(s, i), target.getSuccessors(s, i));
            }
        }
    }

    @Test
    public void errorTest() throws IOException {
        try (InputStream e1 = AUTSerializationTest.class.getResourceAsStream("/error1.aut");
             InputStream e2 = AUTSerializationTest.class.getResourceAsStream("/error2.aut");
             InputStream e3 = AUTSerializationTest.class.getResourceAsStream("/error3.aut");
             InputStream e4 = AUTSerializationTest.class.getResourceAsStream("/error4.aut");
             InputStream e5 = AUTSerializationTest.class.getResourceAsStream("/error5.aut")) {
            Assert.assertThrows(() -> AUTParser.readAutomaton(e1));
            Assert.assertThrows(() -> AUTParser.readAutomaton(e2));
            Assert.assertThrows(() -> AUTParser.readAutomaton(e3));
            Assert.assertThrows(() -> AUTParser.readAutomaton(e4));
            Assert.assertThrows(() -> AUTParser.readAutomaton(e5));
        }
    }

    @Test
    public void doNotCloseOutputStreamTest() throws IOException {
        final CompactDFA<Integer> automaton = RandomAutomata.randomDFA(new Random(0), 10, Alphabets.integers(0, 2));

        AUTSerializationProvider.getInstance()
                                .writeModel(new UnclosableOutputStream(ByteStreams.nullOutputStream()),
                                            automaton,
                                            automaton.getInputAlphabet(),
                                            Objects::toString);
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException {
        try (InputStream is = AUTSerializationTest.class.getResourceAsStream("/quotationTest.aut")) {
            AUTSerializationProvider.getInstance().readModel(new UnclosableInputStream(is));
        }
    }
}
