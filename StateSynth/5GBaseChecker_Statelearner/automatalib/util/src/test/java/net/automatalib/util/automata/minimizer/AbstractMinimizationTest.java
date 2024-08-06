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
package net.automatalib.util.automata.minimizer;

import java.util.Collection;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.partitionrefinement.PaigeTarjanTest;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public abstract class AbstractMinimizationTest {

    @Test
    public void createTestDFA1() {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        // @formatter:off
        CompactDFA<Integer> dfa = AutomatonBuilders.newDFA(alphabet)
                .withInitial("A")
                .from("A")
                    .on(0).to("H")
                    .on(1).to("B")
                .from("B")
                    .on(0).to("H")
                    .on(1).to("A")
                .from("C")
                    .on(0).to("E")
                    .on(1).to("F")
                .from("D")
                    .on(0).to("E")
                    .on(1).to("F")
                .from("E")
                    .on(0).to("F")
                    .on(1).to("G")
                .from("F")
                    .on(0, 1).loop()
                .from("G")
                    .on(0).loop()
                    .on(1).to("F")
                .from("H")
                    .on(0, 1).to("C")
                .withAccepting("F", "G")
                .create();
        // @formatter:on

        testMinimizeDFA(new TestConfig<>(alphabet, dfa, 5, 5));
    }

    @Test
    public void createTestDFA2() {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        // @formatter:off
        CompactDFA<Integer> dfa = AutomatonBuilders.newDFA(alphabet)
                .withInitial("A")
                .from("A")
                    .on(0).to("B")
                    .on(1).to("C")
                .from("B")
                    .on(0, 1).to("D")
                .from("C")
                    .on(0, 1).to("D")
                .from("D")
                    .on(0, 1).to("E")
                .from("E")
                    .on(0, 1).loop()
                .withAccepting("E")
                .create();
        // @formatter:on

        testMinimizeDFA(new TestConfig<>(alphabet, dfa, 4, 4));
    }

    @Test
    public void createTestDFA3() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        final char input1 = 'a';
        final char input2 = 'b';

        // @formatter:off
        final CompactDFA<Character> dfa = AutomatonBuilders.newDFA(alphabet)
                .withInitial("s0")
                .from("s0")
                   .on(input1).to("s2")
                   .on(input2).to("s1")
                .from("s1")
                   .on(input1).loop()
                   .on(input2).loop()
                .from("s2")
                   .on(input1).to("s1")
                   .on(input2).to("s3")
                .from("s3")
                   .on(input1).to("s4")
                   .on(input2).to("s1")
                .from("s4")
                   .on(input1).to("s5")
                   .on(input2).to("s6")
                .from("s5")
                   .on(input1, input2).loop()
                .from("s6")
                   .on(input1).to("s4")
                   .on(input2).to("s5")
                .withAccepting("s0", "s1", "s2", "s3")
                .create();
        // @formatter:on

        testMinimizeDFA(new TestConfig<>(alphabet, dfa, 5, 5));
    }

    @Test
    public void createTestMealy1() {
        final CompactMealy<Integer, String> mealy = PaigeTarjanTest.getMealy();
        TestConfig<Integer, CompactMealy<Integer, String>> config =
                new TestConfig<>(mealy.getInputAlphabet(), mealy, 7, 8);

        if (supportsPartial()) {
            testMinimizeMealy(config);
        } else {
            Assert.assertThrows(IllegalArgumentException.class, () -> testMinimizeMealy(config));
        }
    }

    private <I, A extends MutableDFA<?, I>> void testMinimizeDFA(TestConfig<I, A> test) {

        final DFA<?, I> result = minimizeDFA(test.automaton, test.alphabet);

        if (isPruned()) {
            Assert.assertEquals(result.size(), test.prunedSize);
            Assert.assertTrue(Automata.testEquivalence(test.automaton, result, test.alphabet));
        } else {
            Assert.assertEquals(result.size(), test.unprunedSize);
            assertAllInequivalent(result, test.alphabet);
        }
    }

    private <I, O, A extends MutableMealyMachine<?, I, ?, O>> void testMinimizeMealy(TestConfig<I, A> test) {

        final MealyMachine<?, I, ?, O> result = minimizeMealy(test.automaton, test.alphabet);

        if (isPruned()) {
            Assert.assertEquals(result.size(), test.prunedSize);
            Assert.assertTrue(Automata.testEquivalence(test.automaton, result, test.alphabet));
        } else {
            Assert.assertEquals(result.size(), test.unprunedSize);
            assertAllInequivalent(result, test.alphabet);
        }
    }

    protected abstract <I> DFA<?, I> minimizeDFA(MutableDFA<?, I> dfa, Alphabet<I> alphabet);

    protected abstract <I, O> MealyMachine<?, I, ?, O> minimizeMealy(MutableMealyMachine<?, I, ?, O> mealy,
                                                                     Alphabet<I> alphabet);

    protected abstract boolean isPruned();

    protected abstract boolean supportsPartial();

    private static <S, I> void assertAllInequivalent(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                     Collection<? extends I> inputs) {
        StateIDs<S> ids = automaton.stateIDs();
        int size = automaton.size();
        for (int i = 0; i < size - 1; i++) {
            S s1 = ids.getState(i);
            for (int j = i + 1; j < size; j++) {
                S s2 = ids.getState(j);
                Assert.assertNotNull(Automata.findSeparatingWord(automaton, s1, s2, inputs));
            }
        }
    }

    private static class TestConfig<I, A> {

        final Alphabet<I> alphabet;
        final A automaton;
        final int prunedSize;
        final int unprunedSize;

        TestConfig(Alphabet<I> alphabet, A automaton, int prunedSize, int unprunedSize) {
            this.alphabet = alphabet;
            this.automaton = automaton;
            this.prunedSize = prunedSize;
            this.unprunedSize = unprunedSize;
        }
    }

}
