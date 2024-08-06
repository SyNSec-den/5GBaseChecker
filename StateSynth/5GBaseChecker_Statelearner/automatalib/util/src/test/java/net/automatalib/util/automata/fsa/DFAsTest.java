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

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class DFAsTest {

    private static final boolean[] VECTOR_1 = {true, true, false, false};
    private static final boolean[] VECTOR_1_NEG = {false, false, true, true};
    private static final boolean[] VECTOR_2 = {true, false, true, false};

    // The precomputed results of applying the operations on VECTOR_1 and VECTOR_2
    private static final boolean[] AND_RESULT = {true, false, false, false};
    private static final boolean[] OR_RESULT = {true, true, true, false};
    private static final boolean[] XOR_RESULT = {false, true, true, false};
    private static final boolean[] EQUIV_RESULT = {true, false, false, true};
    private static final boolean[] IMPL_RESULT = {true, false, true, true};

    private final Alphabet<Integer> testAlphabet;
    private final CompactDFA<Integer> testDfa1, testDfa2;

    public DFAsTest() {
        this.testAlphabet = Alphabets.integers(0, 0);
        this.testDfa1 = forVector(VECTOR_1);
        this.testDfa2 = forVector(VECTOR_2);
    }

    private CompactDFA<Integer> forVector(boolean... boolVec) {
        if (boolVec.length == 0) {
            throw new IllegalArgumentException("Length of vector must be non-zero");
        }

        CompactDFA<Integer> result = new CompactDFA<>(testAlphabet, boolVec.length);

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
        DFA<?, Integer> expected = forVector(AND_RESULT);
        DFA<?, Integer> actual = DFAs.and(testDfa1, testDfa2, testAlphabet);

        Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
    }

    @Test
    public void testOr() {
        DFA<?, Integer> expected = forVector(OR_RESULT);
        DFA<?, Integer> actual = DFAs.or(testDfa1, testDfa2, testAlphabet);

        Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
    }

    @Test
    public void testXor() {
        DFA<?, Integer> expected = forVector(XOR_RESULT);
        DFA<?, Integer> actual = DFAs.xor(testDfa1, testDfa2, testAlphabet);

        Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
    }

    @Test
    public void testEquiv() {
        DFA<?, Integer> expected = forVector(EQUIV_RESULT);
        DFA<?, Integer> actual = DFAs.equiv(testDfa1, testDfa2, testAlphabet);

        Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
    }

    @Test
    public void testImpl() {
        DFA<?, Integer> expected = forVector(IMPL_RESULT);
        DFA<?, Integer> actual = DFAs.impl(testDfa1, testDfa2, testAlphabet);

        Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
    }

    @Test
    public void testComplement() {
        DFA<?, Integer> expected = forVector(VECTOR_1_NEG);
        DFA<?, Integer> actual = DFAs.complement(testDfa1, testAlphabet);

        Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
    }

    @Test
    public void testIsPrefixClosed() {
        Assert.assertFalse(DFAs.isPrefixClosed(forVector(VECTOR_1), testAlphabet));

        Assert.assertFalse(DFAs.isPrefixClosed(forVector(VECTOR_1_NEG), testAlphabet));

        Assert.assertFalse(DFAs.isPrefixClosed(forVector(VECTOR_2), testAlphabet));

        Assert.assertTrue(DFAs.isPrefixClosed(forVector(true, true, true, true), testAlphabet));

        Assert.assertTrue(DFAs.isPrefixClosed(forVector(false, false, false, false), testAlphabet));
    }

    @Test
    public void testAcceptsEmptyLanguage() {
        Assert.assertTrue(DFAs.acceptsEmptyLanguage(forVector(false)));

        Assert.assertFalse(DFAs.acceptsEmptyLanguage(forVector(true)));
    }
}
