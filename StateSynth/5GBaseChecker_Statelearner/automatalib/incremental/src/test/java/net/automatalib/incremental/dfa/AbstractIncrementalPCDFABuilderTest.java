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
package net.automatalib.incremental.dfa;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.system.JVMUtil;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.dfa.IncrementalDFABuilder.TransitionSystemView;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.GrowingMapAlphabet;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public abstract class AbstractIncrementalPCDFABuilderTest {

    private static final Alphabet<Character> TEST_ALPHABET = Alphabets.characters('a', 'c');

    private static final Word<Character> W_1 = Word.fromString("abc");
    private static final Word<Character> W_2 = Word.fromString("acb");
    private static final Word<Character> W_3 = Word.fromString("ac");

    private IncrementalDFABuilder<Character> incPcDfa;

    @BeforeClass
    public void setUp() {
        this.incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);
    }

    protected abstract <I> IncrementalDFABuilder<I> createIncrementalPCDFABuilder(Alphabet<I> alphabet);

    @Test
    public void testConfluenceBug() {
        Word<Character> wB1 = Word.fromString("aaa");
        Word<Character> wB2 = Word.fromString("bba");
        Word<Character> wB3 = Word.fromString("aabaa");

        incPcDfa.insert(wB1, true);
        incPcDfa.insert(wB2, true);
        incPcDfa.insert(wB3, true);

        Assert.assertEquals(incPcDfa.lookup(Word.fromString("aababaa")), Acceptance.DONT_KNOW);
        this.incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);
    }

    @Test(dependsOnMethods = "testConfluenceBug")
    public void testLookup() {
        Assert.assertEquals(incPcDfa.lookup(W_1), Acceptance.DONT_KNOW);
        Assert.assertEquals(incPcDfa.lookup(W_2), Acceptance.DONT_KNOW);
        Assert.assertEquals(incPcDfa.lookup(W_3), Acceptance.DONT_KNOW);

        incPcDfa.insert(Word.epsilon(), true);
        Assert.assertEquals(incPcDfa.lookup(W_1), Acceptance.DONT_KNOW);
        Assert.assertEquals(incPcDfa.lookup(W_2), Acceptance.DONT_KNOW);
        Assert.assertEquals(incPcDfa.lookup(W_3), Acceptance.DONT_KNOW);

        incPcDfa.insert(W_1, true);
        Assert.assertEquals(incPcDfa.lookup(W_1), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(2)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(1)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(0)), Acceptance.TRUE);

        Assert.assertEquals(incPcDfa.lookup(W_2), Acceptance.DONT_KNOW);
        Assert.assertEquals(incPcDfa.lookup(W_3), Acceptance.DONT_KNOW);

        incPcDfa.insert(W_2, false);
        Assert.assertEquals(incPcDfa.lookup(W_1), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(W_2), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(W_2.append('a')), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(W_3), Acceptance.DONT_KNOW);

        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(1)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(W_2.prefix(1)), Acceptance.TRUE);

        incPcDfa.insert(W_3, true);
        Assert.assertEquals(incPcDfa.lookup(W_1), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(W_2), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(W_3), Acceptance.TRUE);

        Assert.assertEquals(incPcDfa.lookup(W_1.prefix(2)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(W_2.prefix(1)), Acceptance.TRUE);
        Assert.assertEquals(incPcDfa.lookup(W_3.append('a')), Acceptance.DONT_KNOW);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testInsertSame() {
        int oldSize = incPcDfa.asGraph().size();
        incPcDfa.insert(W_1, true);
        Assert.assertEquals(incPcDfa.asGraph().size(), oldSize);
    }

    @Test(expectedExceptions = ConflictException.class, dependsOnMethods = "testLookup")
    public void testConflict() {
        incPcDfa.insert(W_1, false);
    }

    @Test(dependsOnMethods = "testLookup")
    public void testFindSeparatingWord() {
        CompactDFA<Character> testDfa = new CompactDFA<>(TEST_ALPHABET);
        int s0 = testDfa.addInitialState(true);
        int s1 = testDfa.addState(true);
        int s2 = testDfa.addState(true);
        int s3 = testDfa.addState(true);

        testDfa.addTransition(s0, 'a', s1);
        testDfa.addTransition(s1, 'b', s2);
        testDfa.addTransition(s2, 'c', s3);

        Word<Character> sepWord;
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("ac"));

        int s4 = testDfa.addState(true);
        testDfa.addTransition(s1, 'c', s4);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        int s5 = testDfa.addState(false);
        testDfa.addTransition(s4, 'b', s5);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        int s6 = testDfa.addState(false);
        testDfa.addTransition(s3, 'a', s6);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertNull(sepWord);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertNull(sepWord);

        int s7 = testDfa.addState(true);
        testDfa.addTransition(s5, 'a', s7);
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, true);
        Assert.assertEquals(sepWord, Word.fromString("acba"));
        sepWord = incPcDfa.findSeparatingWord(testDfa, TEST_ALPHABET, false);
        Assert.assertEquals(sepWord, Word.fromString("acba"));
    }

    @Test(dependsOnMethods = "testLookup")
    public void testVisualization() throws InvocationTargetException, InterruptedException {
        final int canonicalSpecVersion = JVMUtil.getCanonicalSpecVersion();
        if (!(canonicalSpecVersion <= 8 || canonicalSpecVersion == 11)) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <=8");
        }

        // invokeAndWait so that TestNG doesn't kill our GUI thread that we want to check.
        SwingUtilities.invokeAndWait(() -> Visualization.visualize(incPcDfa.asGraph(), false));
    }

    @Test(dependsOnMethods = "testLookup")
    public void testTSView() {
        testTSViewInternal(incPcDfa.asTransitionSystem());
    }

    private static <S> void testTSViewInternal(TransitionSystemView<S, Character, ?> view) {
        final S s1 = view.getState(W_1);
        final S s2 = view.getState(W_2);
        final S s3 = view.getState(W_3);

        Assert.assertTrue(view.getAcceptance(s1).toBoolean());
        Assert.assertFalse(view.getAcceptance(s2).toBoolean());
        Assert.assertTrue(view.getAcceptance(s3).toBoolean());
    }

    @Test(dependsOnMethods = "testLookup")
    public void testNewInputSymbol() {
        final GrowingAlphabet<Character> alphabet = new GrowingMapAlphabet<>(TEST_ALPHABET);
        final IncrementalDFABuilder<Character> growableBuilder = createIncrementalPCDFABuilder(alphabet);

        growableBuilder.addAlphabetSymbol('d');
        growableBuilder.addAlphabetSymbol('d');

        final Word<Character> input1 = Word.fromCharSequence("dcba");

        growableBuilder.insert(input1, true);

        Assert.assertTrue(growableBuilder.hasDefinitiveInformation(input1));
        Assert.assertEquals(growableBuilder.lookup(input1), Acceptance.TRUE);
        Assert.assertEquals(growableBuilder.lookup(input1.prefix(2)), Acceptance.TRUE);

        final Word<Character> input2 = Word.fromCharSequence("dddd");

        Assert.assertFalse(growableBuilder.hasDefinitiveInformation(input2));
        Assert.assertEquals(growableBuilder.lookup(input2), Acceptance.DONT_KNOW);

        growableBuilder.insert(input2, false);
        Assert.assertEquals(growableBuilder.lookup(input2), Acceptance.FALSE);
        Assert.assertEquals(growableBuilder.lookup(input2.append('d')), Acceptance.FALSE);
    }

    @Test
    public void testCounterexampleOfLengthOne() {
        final IncrementalDFABuilder<Character> incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);
        incPcDfa.insert(Word.fromCharSequence("a"), true);

        final CompactDFA<Character> dfa = new CompactDFA<>(TEST_ALPHABET);
        final Integer q0 = dfa.addInitialState(true);
        final Integer q1 = dfa.addState(false);

        dfa.addTransition(q0, 'a', q1);

        final Word<Character> ce = incPcDfa.findSeparatingWord(dfa, TEST_ALPHABET, false);
        Assert.assertNotNull(ce);
    }

    @Test
    public void testRejectAll() {
        final IncrementalDFABuilder<Character> incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);

        incPcDfa.insert(Word.epsilon(), false);

        Assert.assertEquals(incPcDfa.lookup(W_1), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(W_2), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(W_3), Acceptance.FALSE);

        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(W_1, true));
        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(W_2, true));
        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(W_3, true));
    }

    @Test
    public void testLateSink() {
        final IncrementalDFABuilder<Character> incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);

        Word<Character> w1 = Word.fromString("abc");
        Word<Character> w2 = Word.fromString("bca");
        Word<Character> w3 = Word.fromString("cb");

        incPcDfa.insert(w1, false);
        incPcDfa.insert(w2, false);

        Assert.assertEquals(incPcDfa.asGraph().size(), 6);

        incPcDfa.insert(Word.epsilon(), false);

        Assert.assertEquals(incPcDfa.asGraph().size(), 1);

        Assert.assertEquals(incPcDfa.lookup(w1), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(w2), Acceptance.FALSE);
        Assert.assertEquals(incPcDfa.lookup(w3), Acceptance.FALSE);

        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(w3, true));
    }

    @Test
    public void testInvalidSink() {
        final IncrementalDFABuilder<Character> incPcDfa = createIncrementalPCDFABuilder(TEST_ALPHABET);

        incPcDfa.insert(Word.fromString("abc"), false);
        incPcDfa.insert(Word.fromString("bca"), true);

        Assert.assertEquals(incPcDfa.asGraph().size(), 7);
        Assert.assertThrows(ConflictException.class, () -> incPcDfa.insert(Word.epsilon(), false));
    }
}
