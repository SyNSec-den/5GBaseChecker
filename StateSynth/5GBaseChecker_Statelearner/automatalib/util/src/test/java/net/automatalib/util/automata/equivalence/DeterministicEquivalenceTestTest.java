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
package net.automatalib.util.automata.equivalence;

import java.util.Collection;
import java.util.Random;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class DeterministicEquivalenceTestTest {

    private static final Random RANDOM = new Random(0);
    private static final int AUTOMATON_SIZE_SMALL = 20;

    // Equivalence check switches implementation when stateSize**2 is > 10000
    private static final int AUTOMATON_SIZE_LARGE = 200;

    @Test
    public void testEquivalenceDFA() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final DFA<?, Integer> a1 = RandomAutomata.randomDFA(RANDOM, AUTOMATON_SIZE_SMALL, alphabet);
        final DFA<?, Integer> a2 = RandomAutomata.randomDFA(RANDOM, AUTOMATON_SIZE_SMALL, alphabet);

        testEquivalenceInternal(a1, a1, alphabet, true);
        testEquivalenceInternal(a1, a2, alphabet, false);
    }

    @Test
    public void testEquivalenceDFALarge() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final DFA<?, Integer> a1 = RandomAutomata.randomDFA(RANDOM, AUTOMATON_SIZE_LARGE, alphabet, false);
        final DFA<?, Integer> a2 = RandomAutomata.randomDFA(RANDOM, AUTOMATON_SIZE_LARGE, alphabet, false);

        testEquivalenceInternal(a1, a1, alphabet, true);
        testEquivalenceInternal(a1, a2, alphabet, false);
    }

    @Test
    public void testEquivalenceMealy() {
        final Alphabet<Integer> inputAlphabet = Alphabets.integers(0, 5);
        final Alphabet<Character> outputAlphabet = Alphabets.characters('a', 'f');
        final MealyMachine<?, Integer, ?, Character> a1 =
                RandomAutomata.randomMealy(RANDOM, AUTOMATON_SIZE_SMALL, inputAlphabet, outputAlphabet);
        final MealyMachine<?, Integer, ?, Character> a2 =
                RandomAutomata.randomMealy(RANDOM, AUTOMATON_SIZE_SMALL, inputAlphabet, outputAlphabet);

        testEquivalenceInternal(a1, a1, inputAlphabet, true);
        testEquivalenceInternal(a1, a2, inputAlphabet, false);
    }

    @Test
    public void testEmptyDFAs() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final CompactDFA<Integer> uninit = new CompactDFA<>(alphabet, 0);
        final CompactDFA<Integer> empty = new CompactDFA<>(alphabet, 1);
        empty.addInitialState(false);

        testForEmptySepWord(uninit, empty, alphabet);
    }

    @Test
    public void testEmptyMealies() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final CompactMealy<Integer, ?> uninit = new CompactMealy<>(alphabet, 0);
        final CompactMealy<Integer, ?> empty = new CompactMealy<>(alphabet, 1);
        empty.addInitialState();

        testForEmptySepWord(uninit, empty, alphabet);
    }

    private static <I> void testForEmptySepWord(UniversalDeterministicAutomaton<?, I, ?, ?, ?> a1,
                                                UniversalDeterministicAutomaton<?, I, ?, ?, ?> a2,
                                                Collection<? extends I> inputs) {

        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(a1, a1, inputs));
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWord(a2, a2, inputs));

        final Word<I> sepWord1 = DeterministicEquivalenceTest.findSeparatingWord(a1, a2, inputs);
        Assert.assertEquals(sepWord1, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord1), a2.getState(sepWord1));

        final Word<I> sepWord2 = DeterministicEquivalenceTest.findSeparatingWord(a2, a1, inputs);
        Assert.assertEquals(sepWord2, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord2), a2.getState(sepWord2));

        // Large version
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWordLarge(a1, a1, inputs));
        Assert.assertNull(DeterministicEquivalenceTest.findSeparatingWordLarge(a2, a2, inputs));

        final Word<I> sepWord3 = DeterministicEquivalenceTest.findSeparatingWordLarge(a1, a2, inputs);
        Assert.assertEquals(sepWord3, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord3), a2.getState(sepWord3));

        final Word<I> sepWord4 = DeterministicEquivalenceTest.findSeparatingWordLarge(a2, a1, inputs);
        Assert.assertEquals(sepWord4, Word.epsilon());
        Assert.assertNotEquals(a1.getState(sepWord4), a2.getState(sepWord4));
    }

    private <I, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, ?>> void testEquivalenceInternal(M a1,
                                                                                                                      M a2,
                                                                                                                      Alphabet<I> alphabet,
                                                                                                                      boolean equivalent) {
        final UniversalDeterministicAutomaton<?, I, ?, ?, ?> m1 = a1;
        final UniversalDeterministicAutomaton<?, I, ?, ?, ?> m2 = a2;

        final Word<I> separatingWord = DeterministicEquivalenceTest.findSeparatingWord(m1, m2, alphabet);

        Assert.assertEquals(equivalent, separatingWord == null);

        if (!equivalent) {
            Assert.assertNotEquals(a1.computeOutput(separatingWord), a2.computeOutput(separatingWord));
        }
    }
}
