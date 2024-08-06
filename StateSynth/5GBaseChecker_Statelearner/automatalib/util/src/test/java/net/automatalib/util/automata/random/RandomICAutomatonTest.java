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
package net.automatalib.util.automata.random;

import java.util.Random;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class RandomICAutomatonTest {

    @Test
    public void testRandomICAutomaton() {

        final Random random = new Random(42);
        final int size = 20;
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');

        final CompactDFA<Character> automaton = RandomAutomata.randomICDFA(random, size, alphabet, false);

        for (final Integer s : automaton) {
            for (final Character i : alphabet) {
                Assert.assertNotNull(automaton.getSuccessor(s, i));
            }
        }
    }
}
