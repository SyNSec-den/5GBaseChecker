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
package net.automatalib.serialization.etf.writer;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.io.UnclosableOutputStream;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for DFA2ETFWriter.
 *
 * @author Jeroen Meijer
 */
public class DFA2ETFWriterTest {

    @Test
    public void testWrite() throws Exception {
        try (Reader r = IOUtil.asBufferedUTF8Reader(DFA2ETFWriterTest.class.getResourceAsStream("/DFA-testWrite.etf"));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');

            final Random random = new Random(0);
            final DFA<?, Character> automaton = RandomAutomata.randomDFA(random, 20, alphabet);

            DFA2ETFWriter.<Character>getInstance().writeModel(baos, automaton, alphabet);

            final String expected = CharStreams.toString(r);

            Assert.assertEquals(baos.toString(StandardCharsets.UTF_8.toString()), expected);
        }
    }

    @Test
    public void testEmptyLanguage() throws Exception {
        try (Reader r = IOUtil.asBufferedUTF8Reader(DFA2ETFWriterTest.class.getResourceAsStream("/DFA-testEmptyLanguage.etf"));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            final Alphabet<Character> alphabet = Alphabets.characters('a', 'a');
            final DFA<?, Character> emptyLanguage =
                    AutomatonBuilders.newDFA(alphabet).withInitial("q0").from("q0").on('a').loop().create();

            DFA2ETFWriter.<Character>getInstance().writeModel(baos, emptyLanguage, alphabet);

            final String expected = CharStreams.toString(r);

            Assert.assertEquals(baos.toString(StandardCharsets.UTF_8.toString()), expected);
        }
    }

    @Test
    public void doNotCloseOutputStreamTest() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final DFA<?, Character> automaton = RandomAutomata.randomDFA(new Random(0), 10, alphabet);

        DFA2ETFWriter.<Character>getInstance().writeModel(new UnclosableOutputStream(ByteStreams.nullOutputStream()),
                                                          automaton,
                                                          alphabet);
    }
}
