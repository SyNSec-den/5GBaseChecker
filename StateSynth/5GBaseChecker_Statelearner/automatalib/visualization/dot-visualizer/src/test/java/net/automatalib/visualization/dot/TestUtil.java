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
package net.automatalib.visualization.dot;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Random;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public final class TestUtil {

    private static final Alphabet<Integer> INPUT_ALPHABET = Alphabets.integers(1, 6);
    private static final int AUTOMATON_SIZE = 10;

    private TestUtil() {
        // prevent instantiation
    }

    static CompactDFA<Integer> generateRandomAutomaton(Random random) {
        return RandomAutomata.randomDFA(random, AUTOMATON_SIZE, INPUT_ALPHABET);
    }

    static String generateRandomAutomatonDot(Random random) {

        final CompactDFA<Integer> automaton = generateRandomAutomaton(random);
        final StringWriter writer = new StringWriter();

        try {
            GraphDOT.write(automaton, INPUT_ALPHABET, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }
}
