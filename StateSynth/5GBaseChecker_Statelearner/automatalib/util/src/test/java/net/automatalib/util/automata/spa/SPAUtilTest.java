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
package net.automatalib.util.automata.spa;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.google.common.collect.ImmutableMap;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.spa.EmptySPA;
import net.automatalib.automata.spa.SPA;
import net.automatalib.automata.spa.StackSPA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class SPAUtilTest {

    private final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
    private final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
    private final char returnSymbol = 'R';

    private final SPAAlphabet<Character> alphabet =
            new DefaultSPAAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);
    final SPAAlphabet<Character> emptyAlphabet =
            new DefaultSPAAlphabet<>(Alphabets.fromArray(), Alphabets.fromArray(), returnSymbol);

    @Test
    public void testIncompleteATRSequences() {
        // construct a simple (pseudo) palindrome system which we will gradually alter to model different cases
        final CompactDFA<Character> s = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final FastDFA<Character> t = new FastDFA<>(alphabet.getProceduralAlphabet());

        final int s0 = s.addInitialState(false);
        final int s1 = s.addState(false);
        final int s2 = s.addState(false);
        final int s3 = s.addState(false);
        final int s4 = s.addState(false);
        final int s5 = s.addState(false);

        s.addTransition(s0, 'T', s5);
        s.addTransition(s0, 'a', s1);
        s.addTransition(s0, 'b', s2);
        s.addTransition(s1, 'S', s3);
        s.addTransition(s2, 'S', s4);
        s.addTransition(s3, 'a', s5);
        s.addTransition(s4, 'b', s5);

        final FastDFAState t0 = t.addInitialState(false);
        final FastDFAState t1 = t.addState(false);
        final FastDFAState t2 = t.addState(false);
        final FastDFAState t3 = t.addState(false);

        t.addTransition(t0, 'c', t1);
        t.addTransition(t1, 'T', t2);
        t.addTransition(t2, 'c', t3);

        final SimpleEntry<Character, Word<Character>> sAsEntry = new SimpleEntry<>('S', Word.fromLetter('S'));
        final SimpleEntry<Character, Word<Character>> sTsEntry = new SimpleEntry<>('S', Word.epsilon());
        final SimpleEntry<Character, Word<Character>> sRsEntry = new SimpleEntry<>('S', Word.fromLetter('R'));
        final SimpleEntry<Character, Word<Character>> tAsEntry = new SimpleEntry<>('T', Word.fromCharSequence("ST"));
        final SimpleEntry<Character, Word<Character>> tTsEntry = new SimpleEntry<>('T', Word.fromCharSequence("c"));
        final SimpleEntry<Character, Word<Character>> tRsEntry = new SimpleEntry<>('T', Word.fromCharSequence("RR"));

        final DefaultSPAAlphabet<Character> halfAlphabet =
                new DefaultSPAAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);

        // With no accepting states, there exist no a/t/r sequences.
        final SPA<?, Character> spa = new StackSPA<>(alphabet, 'S', ImmutableMap.of('S', s, 'T', t));
        ATRSequences<Character> atrSequences = SPAUtil.computeATRSequences(spa, alphabet);

        Assertions.assertThat(atrSequences.accessSequences).containsOnly(sAsEntry);
        Assertions.assertThat(atrSequences.terminatingSequences).isEmpty();
        Assertions.assertThat(atrSequences.returnSequences).containsOnly(sRsEntry);
        Assertions.assertThat(SPAUtil.isRedundancyFree(spa)).isFalse();
        Assertions.assertThat(SPAUtil.isRedundancyFree(emptyAlphabet, atrSequences)).isTrue();

        // Now we make the initial state of S accepting
        // This should give us a terminating sequence for S but still no sequences for T.
        s.setAccepting(s0, true);

        atrSequences = SPAUtil.computeATRSequences(spa, alphabet);

        Assertions.assertThat(atrSequences.accessSequences).containsOnly(sAsEntry);
        Assertions.assertThat(atrSequences.terminatingSequences).containsOnly(sTsEntry);
        Assertions.assertThat(atrSequences.returnSequences).containsOnly(sRsEntry);
        Assertions.assertThat(SPAUtil.isRedundancyFree(spa)).isFalse();
        Assertions.assertThat(SPAUtil.isRedundancyFree(halfAlphabet, atrSequences)).isTrue();
        Assertions.assertThat(SPAUtil.isRedundancyFree(emptyAlphabet, atrSequences)).isTrue();

        // Now we make s5 of S accepting.
        // This gives us a terminating sequence that traverses T and therefore allows us to extract access and terminating sequences for T.
        s.setAccepting(s5, true);

        atrSequences = SPAUtil.computeATRSequences(spa, alphabet);

        Assertions.assertThat(atrSequences.accessSequences).containsOnly(sAsEntry, tAsEntry);
        Assertions.assertThat(atrSequences.terminatingSequences).containsOnly(sTsEntry);
        Assertions.assertThat(atrSequences.returnSequences).containsOnly(sRsEntry, tRsEntry);
        Assertions.assertThat(SPAUtil.isRedundancyFree(spa)).isFalse();
        Assertions.assertThat(SPAUtil.isRedundancyFree(halfAlphabet, atrSequences)).isTrue();
        Assertions.assertThat(SPAUtil.isRedundancyFree(emptyAlphabet, atrSequences)).isTrue();

        // Now make t3 of T accepting.
        // The only path to an accepting state contains a recursive call to T, so we still cannot extract a valid terminating sequence.
        t.setAccepting(t3, true);

        atrSequences = SPAUtil.computeATRSequences(spa, alphabet);

        Assertions.assertThat(atrSequences.accessSequences).containsOnly(sAsEntry, tAsEntry);
        Assertions.assertThat(atrSequences.terminatingSequences).containsOnly(sTsEntry);
        Assertions.assertThat(atrSequences.returnSequences).containsOnly(sRsEntry, tRsEntry);
        Assertions.assertThat(SPAUtil.isRedundancyFree(spa)).isFalse();
        Assertions.assertThat(SPAUtil.isRedundancyFree(halfAlphabet, atrSequences)).isTrue();
        Assertions.assertThat(SPAUtil.isRedundancyFree(emptyAlphabet, atrSequences)).isTrue();

        // Now make t1 of T accepting.
        // This allows us to construct a valid terminating sequence for T and therefore make the global ATRSequences valid.
        t.setAccepting(t1, true);

        atrSequences = SPAUtil.computeATRSequences(spa, alphabet);

        Assertions.assertThat(atrSequences.accessSequences).containsOnly(sAsEntry, tAsEntry);
        Assertions.assertThat(atrSequences.terminatingSequences).containsOnly(sTsEntry, tTsEntry);
        Assertions.assertThat(atrSequences.returnSequences).containsOnly(sRsEntry, tRsEntry);
        Assertions.assertThat(SPAUtil.isRedundancyFree(spa)).isTrue();
    }

    @Test
    public void testPartialATRSequences() {
        final Random random = new Random(42);
        final SPA<?, Character> spa = RandomAutomata.randomSPA(random, alphabet, 10);
        final DefaultSPAAlphabet<Character> halfAlphabet =
                new DefaultSPAAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);

        final ATRSequences<Character> atrSequences = SPAUtil.computeATRSequences(spa, halfAlphabet);

        Assertions.assertThat(atrSequences.accessSequences).containsOnlyKeys('S');
        Assertions.assertThat(atrSequences.terminatingSequences).containsOnlyKeys('S');
        Assertions.assertThat(atrSequences.returnSequences).containsOnlyKeys('S');

        Assertions.assertThat(SPAUtil.isRedundancyFree(spa, halfAlphabet)).isTrue();
        Assertions.assertThat(SPAUtil.isRedundancyFree(alphabet, atrSequences)).isFalse();
    }

    @Test
    public void testEmptyPartialATRSequences() {
        final SPA<?, Character> spa = new EmptySPA<>(alphabet);
        final DefaultSPAAlphabet<Character> halfAlphabet =
                new DefaultSPAAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);

        final ATRSequences<Character> atrSequences = SPAUtil.computeATRSequences(spa, halfAlphabet);

        Assert.assertTrue(atrSequences.accessSequences.isEmpty());
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
        Assert.assertTrue(atrSequences.returnSequences.isEmpty());

        Assert.assertFalse(SPAUtil.isRedundancyFree(spa, halfAlphabet));
        Assert.assertFalse(SPAUtil.isRedundancyFree(alphabet, atrSequences));
    }

    @Test
    public void testCompleteATRSequences() {
        final Random random = new Random(42);
        final SPA<?, Character> spa = RandomAutomata.randomSPA(random, alphabet, 10);
        final ATRSequences<Character> atrSequences = SPAUtil.computeATRSequences(spa);

        Assert.assertTrue(atrSequences.accessSequences.keySet().containsAll(alphabet.getCallAlphabet()));
        Assert.assertTrue(atrSequences.terminatingSequences.keySet().containsAll(alphabet.getCallAlphabet()));
        Assert.assertTrue(atrSequences.returnSequences.keySet().containsAll(alphabet.getCallAlphabet()));
        Assert.assertTrue(SPAUtil.isRedundancyFree(spa));
    }

    @Test
    public void testEmptyCompleteATRSequences() {
        final SPA<?, Character> spa = new EmptySPA<>(alphabet);
        final ATRSequences<Character> atrSequences = SPAUtil.computeATRSequences(spa);

        Assert.assertTrue(atrSequences.accessSequences.isEmpty());
        Assert.assertTrue(atrSequences.terminatingSequences.isEmpty());
        Assert.assertTrue(atrSequences.returnSequences.isEmpty());
        Assert.assertFalse(SPAUtil.isRedundancyFree(spa));
    }

    @Test
    public void testMissingAccessReturnSequenceDueToMissingTerminatingSequence() {
        final CompactDFA<Character> s = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final FastDFA<Character> t = new FastDFA<>(alphabet.getProceduralAlphabet());

        final int s0 = s.addInitialState(false);
        final int s1 = s.addState(false);
        final int s2 = s.addState(false);
        final int s3 = s.addState(true);

        s.addTransition(s0, 'T', s1);
        s.addTransition(s1, 'c', s2);
        s.addTransition(s2, 'T', s3);

        t.addInitialState(false);

        final SPA<?, Character> spa = new StackSPA<>(alphabet, 'S', ImmutableMap.of('S', s, 'T', t));

        final ATRSequences<Character> atrSequences = SPAUtil.computeATRSequences(spa);
        Assertions.assertThat(atrSequences.accessSequences).containsOnlyKeys('S');
        Assertions.assertThat(atrSequences.terminatingSequences).isEmpty();
        Assertions.assertThat(atrSequences.returnSequences).containsOnlyKeys('S');
    }

    @Test
    public void testDefaultSeparatingWord() {
        final Random random = new Random(42);
        final int size = 10;

        final SPA<?, Character> spa1 = RandomAutomata.randomSPA(random, alphabet, size);
        final SPA<?, Character> spa2 = RandomAutomata.randomSPA(random, alphabet, size);

        Assert.assertNull(Automata.findSeparatingWord(spa1, spa1, alphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa2, spa2, alphabet));

        final Word<Character> sepWord1 = Automata.findSeparatingWord(spa1, spa2, alphabet);
        final Word<Character> sepWord2 = Automata.findSeparatingWord(spa2, spa1, alphabet);
        Assert.assertNotNull(sepWord1);
        Assert.assertNotNull(sepWord2);
        Assert.assertNotEquals(spa1.computeOutput(sepWord1), spa2.computeOutput(sepWord1));
        Assert.assertNotEquals(spa1.computeOutput(sepWord2), spa2.computeOutput(sepWord2));

        Assert.assertNull(Automata.findSeparatingWord(spa1, spa1, emptyAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa2, spa2, emptyAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa1, spa2, emptyAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa2, spa1, emptyAlphabet));
    }

    @Test
    public void testIntricateSeparatingWord() {
        // construct a simple (pseudo) palindrome system which we will gradually alter to model different cases
        final CompactDFA<Character> s1 = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final FastDFA<Character> t1 = new FastDFA<>(alphabet.getProceduralAlphabet());

        final int s1s0 = s1.addInitialState(false);
        final int s1s1 = s1.addState(false);
        final int s1s2 = s1.addState(false);
        final int s1s3 = s1.addState(false);
        final int s1s4 = s1.addState(false);
        final int s1s5 = s1.addState(false);

        s1.addTransition(s1s0, 'T', s1s5);
        s1.addTransition(s1s0, 'a', s1s1);
        s1.addTransition(s1s0, 'b', s1s2);
        s1.addTransition(s1s1, 'S', s1s3);
        s1.addTransition(s1s2, 'S', s1s4);
        s1.addTransition(s1s3, 'a', s1s5);
        s1.addTransition(s1s4, 'b', s1s5);

        final FastDFAState t1t0 = t1.addInitialState(false);
        final FastDFAState t1t1 = t1.addState(false);
        final FastDFAState t1t2 = t1.addState(false);
        final FastDFAState t1t3 = t1.addState(false);

        t1.addTransition(t1t0, 'c', t1t1);
        t1.addTransition(t1t1, 'T', t1t2);
        t1.addTransition(t1t2, 'c', t1t3);

        final SPA<?, Character> spa1 = new StackSPA<>(alphabet, 'S', ImmutableMap.of('S', s1, 'T', t1));

        final CompactDFA<Character> s2 = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final FastDFA<Character> t2 = new FastDFA<>(alphabet.getProceduralAlphabet());

        final int s2s0 = s2.addInitialState(false);
        final int s2s1 = s2.addState(false);
        final int s2s2 = s2.addState(false);
        final int s2s3 = s2.addState(false);
        final int s2s4 = s2.addState(false);
        final int s2s5 = s2.addState(false);

        s2.addTransition(s2s0, 'T', s2s5);
        s2.addTransition(s2s0, 'a', s2s1);
        s2.addTransition(s2s0, 'b', s2s2);
        s2.addTransition(s2s1, 'S', s2s3);
        s2.addTransition(s2s2, 'S', s2s4);
        s2.addTransition(s2s3, 'a', s2s5);
        s2.addTransition(s2s4, 'b', s2s5);

        final FastDFAState t2t0 = t2.addInitialState(false);
        final FastDFAState t2t1 = t2.addState(false);
        final FastDFAState t2t2 = t2.addState(false);
        final FastDFAState t2t3 = t2.addState(false);

        t2.addTransition(t2t0, 'c', t2t1);
        t2.addTransition(t2t1, 'T', t2t2);
        t2.addTransition(t2t2, 'c', t2t3);

        final SPA<?, Character> spa2 = new StackSPA<>(alphabet, 'S', ImmutableMap.of('S', s2, 'T', t2));

        final SPA<?, Character> emptySPA = new EmptySPA<>(alphabet);

        // no accessible procedures, no separating word should exists. Even with the empty SPAs
        Assert.assertNull(Automata.findSeparatingWord(spa1, spa2, alphabet));
        Assert.assertNull(Automata.findSeparatingWord(emptySPA, spa2, alphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa1, emptySPA, alphabet));

        // make SPA1's 'S' procedure accept 'a'. Now there should exist a separating word
        s1.setAccepting(s1s1, true);
        verifySepWord(spa1, spa2, alphabet);
        verifySepWord(spa2, spa1, alphabet);
        verifySepWord(spa1, emptySPA, alphabet);
        verifySepWord(emptySPA, spa1, alphabet);

        // however, not if we restrict the alphabet to 'b','c'
        final SPAAlphabet<Character> bcAlphabet =
                new DefaultSPAAlphabet<>(Alphabets.characters('b', 'c'), callAlphabet, returnSymbol);
        Assert.assertNull(Automata.findSeparatingWord(spa1, spa2, bcAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa2, spa1, bcAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa1, emptySPA, bcAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(emptySPA, spa1, bcAlphabet));

        // update SPA2 according to SPA1. There should no longer exist a separating word
        s2.setAccepting(s2s1, true);
        Assert.assertNull(Automata.findSeparatingWord(spa1, spa2, alphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa2, spa1, alphabet));

        // make SPA1's s5 accept so that we introduce procedure 'T'. This also adds a new separating word (two 'a's)
        s1.setAccepting(s1s5, true);
        verifySepWord(spa1, spa2, alphabet);
        verifySepWord(spa2, spa1, alphabet);

        // update SPA2 accordingly
        s2.setAccepting(s2s5, true);
        Assert.assertNull(Automata.findSeparatingWord(spa1, spa2, alphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa2, spa1, alphabet));

        // make SPA1's procedure 'T' accept 'c' so that we can find another separating word
        t1.setAccepting(t1t1, true);
        verifySepWord(spa1, spa2, alphabet);
        verifySepWord(spa2, spa1, alphabet);

        // this should also work for partial SPAs
        final SPA<?, Character> partial1 = new StackSPA<>(alphabet, 'S', ImmutableMap.of('S', s1));
        verifySepWord(spa1, partial1, alphabet);
        verifySepWord(partial1, spa1, alphabet);

        // If we restrict ourselves to only 'S' call symbols, a separating word should no longer exist
        final SPAAlphabet<Character> sAlphabet =
                new DefaultSPAAlphabet<>(internalAlphabet, Alphabets.singleton('S'), returnSymbol);
        Assert.assertNull(Automata.findSeparatingWord(spa1, spa2, sAlphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa2, spa1, sAlphabet));

        // update SPA2 accordingly
        t2.setAccepting(t2t1, true);
        Assert.assertNull(Automata.findSeparatingWord(spa1, spa2, alphabet));
        Assert.assertNull(Automata.findSeparatingWord(spa2, spa1, alphabet));

        // make SPA1's 'T' procedure also accept two c's.
        // This should yield a separating word even if we restrict ourselves to only 'c' as internal symbol
        t1.setAccepting(t1t3, true);
        final SPAAlphabet<Character> cAlphabet =
                new DefaultSPAAlphabet<>(Alphabets.singleton('c'), callAlphabet, returnSymbol);
        verifySepWord(spa1, spa2, alphabet);
        verifySepWord(spa2, spa1, alphabet);
        verifySepWord(spa1, spa2, cAlphabet);
        verifySepWord(spa2, spa1, cAlphabet);
    }

    private static <I> void verifySepWord(SPA<?, I> spa1, SPA<?, I> spa2, SPAAlphabet<I> alphabet) {
        final Word<I> sepWord = Automata.findSeparatingWord(spa1, spa2, alphabet);
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(spa1.accepts(sepWord), spa2.accepts(sepWord));
    }

    /**
     * Tests a (snapshot of a) randomly generated SPA that has uncovered a bug in a previous implementation.
     */
    @Test
    public void testRandomBenchmarkSystem1() {
        final SPAAlphabet<Integer> alphabet =
                new DefaultSPAAlphabet<>(Alphabets.integers(0, 9), Alphabets.integers(10, 12), -1);

        // @formatter:off
        CompactDFA<Integer> p10 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).loop()
                                                   .from("s0").on(0).to("s1")
                                                   .from("s1").on(0, 1, 3, 4, 10, 11, 12).to("s0")
                                                   .from("s1").on(2, 5, 7).loop()
                                                   .withAccepting("s1")
                                                   .create();

        CompactDFA<Integer> p11 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).loop()
                                                   .from("s0").on(12).to("s1")
                                                   .from("s1").on(0).loop()
                                                   .from("s1").on(1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).to("s0")
                                                   .withAccepting("s1")
                                                   .create();

        CompactDFA<Integer> p12 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0).to("s1")
                                                   .from("s0").on(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).to("s2")
                                                   .from("s1").on(6, 12).to("s0")
                                                   .from("s1").on(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11).to("s2")
                                                   .from("s2").on(8).to("s0")
                                                   .from("s2").on(5).to("s1")
                                                   .from("s2").on(0, 1, 2, 3, 4, 6, 7, 9, 10, 11, 12).loop()
                                                   .withAccepting("s0")
                                                   .create();
        // @formatter:on

        final Map<Integer, DFA<?, Integer>> procedures = new HashMap<>();
        procedures.put(10, p10);
        procedures.put(11, p11);
        procedures.put(12, p12);

        final SPA<?, Integer> spa = new StackSPA<>(alphabet, 11, procedures);

        final ATRSequences<Integer> atr = SPAUtil.computeATRSequences(spa);

        verifyATR(spa, alphabet, atr);
    }

    /**
     * Tests a (snapshot of a) randomly generated SPA that has uncovered a bug in a previous implementation.
     */
    @Test
    public void testRandomBenchmarkSystem2() {
        final SPAAlphabet<Integer> alphabet =
                new DefaultSPAAlphabet<>(Alphabets.integers(0, 9), Alphabets.integers(10, 11), -1);

        // @formatter:off
        CompactDFA<Integer> p10 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0, 6, 7, 10, 11).loop()
                                                   .from("s0").on(1, 2, 3, 4, 5, 8, 9).to("s1")
                                                   .from("s1").on(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).loop()
                                                   .withAccepting("s0")
                                                   .create();

        CompactDFA<Integer> p11 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0, 2, 3, 4, 5, 7, 8, 10, 11).to("s1")
                                                   .from("s0").on(1, 6, 9).to("s2")
                                                   .from("s1").on(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).loop()
                                                   .from("s2").on(8, 11).to("s0")
                                                   .from("s2").on(0, 1, 2, 9).to("s1")
                                                   .from("s2").on(4, 5, 6, 7).loop()
                                                   .from("s2").on(3, 10).to("s3")
                                                   .from("s3").on(1, 6, 9, 11).to("s2")
                                                   .from("s3").on(0, 2, 3, 4, 5, 7, 8, 10).to("s1")
                                                   .withAccepting("s0")
                                                   .create();
        // @formatter:on

        final Map<Integer, DFA<?, Integer>> procedures = new HashMap<>();
        procedures.put(10, p10);
        procedures.put(11, p11);

        final SPA<?, Integer> spa = new StackSPA<>(alphabet, 11, procedures);

        final ATRSequences<Integer> atr = SPAUtil.computeATRSequences(spa);

        verifyATR(spa, alphabet, atr);
    }

    /**
     * Tests a (snapshot of a) randomly generated SPA that has uncovered a bug in a previous implementation.
     */
    @Test
    public void testRandomBenchmarkSystem3() {
        final SPAAlphabet<Integer> alphabet =
                new DefaultSPAAlphabet<>(Alphabets.singleton(0), Alphabets.fromArray(1, 2), -1);

        // @formatter:off
        CompactDFA<Integer> p1 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0).to("s1")
                                                   .from("s1").on(0).to("s2")
                                                   .from("s2").on(0).to("s3")
                                                   .from("s3").on(2).to("s4")
                                                   .from("s4").on(1).to("s3")
                                                   .from("s3").on(1).to("s2")
                                                   .from("s2").on(1).to("s1")
                                                   .from("s1").on(1).to("s0")
                                                   .withAccepting("s0")
                                                   .create();

        CompactDFA<Integer> p2 = AutomatonBuilders.newDFA(alphabet.getProceduralAlphabet())
                                                   .withInitial("s0")
                                                   .from("s0").on(0, 1, 2).loop()
                                                   .withAccepting("s0")
                                                   .create();
        // @formatter:on

        final Map<Integer, DFA<?, Integer>> procedures = new HashMap<>();
        procedures.put(1, p1);
        procedures.put(2, p2);

        final SPA<?, Integer> spa = new StackSPA<>(alphabet, 1, procedures);

        final ATRSequences<Integer> atr = SPAUtil.computeATRSequences(spa);

        verifyATR(spa, alphabet, atr);
    }

    @Test
    public void testEquivalence() {
        final Random random = new Random(42);
        final int size = 10;

        final SPA<?, Character> spa1 = RandomAutomata.randomSPA(random, alphabet, size);
        final SPA<?, Character> spa2 = RandomAutomata.randomSPA(random, alphabet, size);

        Assert.assertTrue(Automata.testEquivalence(spa1, spa1, alphabet));
        Assert.assertTrue(Automata.testEquivalence(spa2, spa2, alphabet));

        Assert.assertFalse(Automata.testEquivalence(spa1, spa2, alphabet));
        Assert.assertFalse(Automata.testEquivalence(spa2, spa1, alphabet));
    }

    private static <I> void verifyATR(SPA<?, I> spa, SPAAlphabet<I> alphabet, ATRSequences<I> atr) {

        for (I i : alphabet.getCallAlphabet()) {
            final Word<I> as = atr.accessSequences.get(i);
            final Word<I> ts = atr.terminatingSequences.get(i);
            final Word<I> rs = atr.returnSequences.get(i);

            Assert.assertNotNull(as, Objects.toString(i));
            Assert.assertNotNull(ts, Objects.toString(i));
            Assert.assertNotNull(rs, Objects.toString(i));
            Assert.assertTrue(spa.accepts(Word.fromWords(as, ts, rs)), Objects.toString(i));
        }
    }
}
