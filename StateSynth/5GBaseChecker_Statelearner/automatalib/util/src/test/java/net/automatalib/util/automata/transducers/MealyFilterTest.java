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
package net.automatalib.util.automata.transducers;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class MealyFilterTest {

    private final Alphabet<Integer> testAlphabet;
    private CompactMealy<Integer, String> testMealy;

    public MealyFilterTest() {
        this.testAlphabet = Alphabets.integers(0, 1);
    }

    @BeforeClass
    public void setUp() {
        this.testMealy = fromSequence("a", "b", "c");
    }

    private CompactMealy<Integer, String> fromSequence(String... outputs) {
        CompactMealy<Integer, String> mealy = new CompactMealy<>(testAlphabet);

        int prev = -1; //mealy.addInitialState();

        int first = -1;

        for (String out : outputs) {
            int next = mealy.addState();
            if (prev < 0) {
                first = next;
            } else {
                mealy.addTransition(prev, 1, next, out);
            }
            prev = next;
        }

        int init = mealy.addInitialState();
        mealy.addTransition(init, 1, first, outputs[0]);

        return mealy;
    }

    @Test
    public void testPruneTransitionWithOutput() {
        Word<Integer> testWord = Word.fromSymbols(1, 1, 1);
        WordBuilder<String> testOutput = new WordBuilder<>(3);

        Assert.assertTrue(testMealy.trace(testWord, testOutput));
        Assert.assertEquals(testOutput.toWord(), Word.fromSymbols("a", "b", "c"));
        testOutput.clear();

        MealyMachine<?, Integer, ?, String> mealy1 =
                MealyFilter.pruneTransitionsWithOutput(testMealy, testAlphabet, "c");
        Assert.assertEquals(mealy1.size(), 3);

        Assert.assertFalse(mealy1.trace(testWord, testOutput));
        Assert.assertEquals(testOutput.toWord(), Word.fromSymbols("a", "b"));
        testOutput.clear();

        MealyMachine<?, Integer, ?, String> mealy2 =
                MealyFilter.pruneTransitionsWithOutput(testMealy, testAlphabet, "b", "c");
        Assert.assertEquals(mealy2.size(), 2);

        Assert.assertFalse(mealy2.trace(testWord, testOutput));
        Assert.assertEquals(testOutput.toWord(), Word.fromSymbols("a"));
        testOutput.clear();

        MealyMachine<?, Integer, ?, String> mealy3 =
                MealyFilter.pruneTransitionsWithOutput(testMealy, testAlphabet, "a");
        Assert.assertEquals(mealy3.size(), 1);

        Assert.assertFalse(mealy3.trace(testWord, testOutput));
        Assert.assertEquals(testOutput.toWord(), Word.epsilon());
    }
}
