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

import java.util.Objects;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MealyMachinesTest {

    private Alphabet<Character> alphabet;
    private CompactMealy<Character, Integer> mealy1;
    private CompactMealy<Character, Integer> mealy2;

    @BeforeClass
    public void setUp() {
        this.alphabet = Alphabets.characters('a', 'c');
        // @formatter:off
        this.mealy1 = AutomatonBuilders.<Character, Integer>newMealy(alphabet).withInitial(0)
                                                                              .from(0).on('a').withOutput(1).to(1)
                                                                              .from(1).on('b').withOutput(2).to(2)
                                                                              .from(2).on('c').withOutput(3).to(0)
                                                                              .create();

        this.mealy2 = AutomatonBuilders.<Character, Integer>newMealy(alphabet).withInitial(0)
                                                                              .from(0).on('a').withOutput(3).to(1)
                                                                              .from(0).on('b').withOutput(0).loop()
                                                                              .from(1).on('b').withOutput(2).to(2)
                                                                              .from(2).on('c').withOutput(1).to(0)
                                                                              .create();
        // @formatter:on
    }

    @Test
    public void testProductAutomaton() {
        final CompactMealy<Character, Pair<Integer, Integer>> product = MealyMachines.combine(mealy1, mealy2, alphabet);

        final Word<Character> completeInput = Word.fromCharSequence("abcabcabc");
        final Word<Character> partialInput = Word.fromCharSequence("abcbbb");
        final Word<Character> undefinedInput = Word.fromCharSequence("abbbbb");

        final Word<Pair<Integer, Integer>> completeOutput = product.computeOutput(completeInput);
        final Word<Pair<Integer, Integer>> partialOutput = product.computeOutput(partialInput);
        final Word<Pair<Integer, Integer>> undefinedOutput = product.computeOutput(undefinedInput);

        Assert.assertEquals(completeOutput.size(), completeInput.size());
        Assert.assertTrue(completeOutput.stream().allMatch(out -> out.getFirst() + out.getSecond() == 4));

        Assert.assertEquals(partialOutput.size(), partialInput.size());
        Assert.assertTrue(partialOutput.stream().map(Pair::getSecond).allMatch(Objects::nonNull));
        Assert.assertTrue(partialOutput.stream().map(Pair::getFirst).anyMatch(Objects::isNull));

        Assert.assertEquals(undefinedOutput.size(), 2);
    }

    @Test
    public void testCompletion() {
        final CompactMealy<Character, Integer> completed = MealyMachines.complete(mealy1, alphabet, -1);

        final Word<Character> originalInput = Word.fromCharSequence("abcabcabc");
        final Word<Character> partialInput = Word.fromCharSequence("abcbbb");
        final Word<Character> undefinedInput = Word.fromCharSequence("bbbbbb");

        final Word<Integer> originalOutput = completed.computeOutput(originalInput);
        final Word<Integer> partialOutput = completed.computeOutput(partialInput);
        final Word<Integer> undefinedOutput = completed.computeOutput(undefinedInput);

        Assert.assertEquals(completed.size(), mealy1.size() + 1);
        Assert.assertEquals(originalOutput, Word.fromSymbols(1, 2, 3, 1, 2, 3, 1, 2, 3));
        Assert.assertEquals(partialOutput, Word.fromSymbols(1, 2, 3, -1, -1, -1));
        Assert.assertEquals(undefinedOutput, Word.fromSymbols(-1, -1, -1, -1, -1, -1));
    }
}
