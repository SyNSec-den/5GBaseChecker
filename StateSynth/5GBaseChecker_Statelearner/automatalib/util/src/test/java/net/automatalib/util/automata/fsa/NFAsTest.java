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
package net.automatalib.util.automata.fsa;

import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NFAsTest {

    private static final boolean[] VECTOR_1 = {true, true, false, false};
    private static final boolean[] VECTOR_2 = {true, false, true, false};

    // The precomputed results of applying the operations on VECTOR_1 and VECTOR_2
    private static final boolean[] AND_RESULT = {true, false, false, false};
    private static final boolean[] OR_RESULT = {true, true, true, false};
    private static final boolean[] XOR_RESULT = {false, true, true, false};
    private static final boolean[] EQUIV_RESULT = {true, false, false, true};
    private static final boolean[] IMPL_RESULT = {true, false, true, true};

    private final Alphabet<Integer> testAlphabet;
    private final CompactNFA<Integer> testNfa1, testNfa2;

    public NFAsTest() {
        this.testAlphabet = Alphabets.integers(0, 0);
        this.testNfa1 = forVector(VECTOR_1);
        this.testNfa2 = forVector(VECTOR_2);
    }

    private CompactNFA<Integer> forVector(boolean... boolVec) {
        if (boolVec.length == 0) {
            throw new IllegalArgumentException("Length of vector must be non-zero");
        }

        CompactNFA<Integer> result = new CompactNFA<>(testAlphabet, boolVec.length);

        int first = result.addInitialState(boolVec[0]);
        int prev = first;

        for (int i = 1; i < boolVec.length; i++) {
            int next = result.addState(boolVec[i]);
            result.addTransition(prev, 0, next);
            prev = next;
        }

        result.addTransition(prev, 0, first);

        return result;
    }

    @Test
    public void testAnd() {
        NFA<?, Integer> expected = forVector(AND_RESULT);
        NFA<?, Integer> actual = NFAs.and(testNfa1, testNfa2, testAlphabet);

        assertEquivalence(actual, expected, testAlphabet);
    }

    @Test
    public void testOr() {
        NFA<?, Integer> expected = forVector(OR_RESULT);
        NFA<?, Integer> actual = NFAs.or(testNfa1, testNfa2, testAlphabet);

        assertEquivalence(actual, expected, testAlphabet);
    }

    @Test
    public void testXor() {
        NFA<?, Integer> expected = forVector(XOR_RESULT);
        NFA<?, Integer> actual = NFAs.xor(testNfa1, testNfa2, testAlphabet);

        assertEquivalence(actual, expected, testAlphabet);
    }

    @Test
    public void testEquiv() {
        NFA<?, Integer> expected = forVector(EQUIV_RESULT);
        NFA<?, Integer> actual = NFAs.equiv(testNfa1, testNfa2, testAlphabet);

        assertEquivalence(actual, expected, testAlphabet);
    }

    @Test
    public void testImpl() {
        NFA<?, Integer> expected = forVector(IMPL_RESULT);
        NFA<?, Integer> actual = NFAs.impl(testNfa1, testNfa2, testAlphabet);

        assertEquivalence(actual, expected, testAlphabet);
    }

    @Test
    public void testDeterminize() {
        Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

        CompactNFA<Integer> nfa = new CompactNFA<>(alphabet);

        int q0 = nfa.addInitialState(false);
        int q1 = nfa.addState(true);

        nfa.addTransition(q0, 0, q0);
        nfa.addTransition(q0, 1, q0);
        nfa.addTransition(q0, 1, q1);

        Assert.assertTrue(nfa.accepts(Word.fromSymbols(0, 1, 0, 1)));
        Assert.assertFalse(nfa.accepts(Word.fromSymbols(0, 1, 0, 1, 0)));

        CompactDFA<Integer> dfa = NFAs.determinize(nfa);

        Assert.assertEquals(dfa.size(), 2);
    }

    private <I> void assertEquivalence(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputs) {
        Assert.assertTrue(Automata.testEquivalence(NFAs.determinize(nfa1, inputs),
                                                   NFAs.determinize(nfa2, inputs),
                                                   inputs));

    }
}
