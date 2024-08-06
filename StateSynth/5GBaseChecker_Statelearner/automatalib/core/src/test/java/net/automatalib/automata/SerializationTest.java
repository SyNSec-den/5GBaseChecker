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
package net.automatalib.automata;

import java.util.Collection;

import com.thoughtworks.xstream.XStream;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.automata.transducers.impl.FastMoore;
import net.automatalib.automata.transducers.impl.FastProbMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.automata.transducers.impl.compact.CompactSST;
import net.automatalib.automata.util.TestUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A test for checking if the automaton (and alphabet) implementations can be properly serialized and de-serialized.
 * This test mainly exists for the "Resumable" feature of LearnLib.
 *
 * @author frohme
 */
public class SerializationTest {

    private enum InputEnum {
        ENUM_A,
        ENUM_B,
        ENUM_C
    }

    private XStream xstream;

    @BeforeClass
    public void beforeClass() {
        xstream = new XStream();
        xstream.allowTypesByRegExp(new String[] {"net.automatalib.*"});
    }

    @DataProvider(name = "alphabets")
    public static Object[][] alphabetProvider() {
        return new Object[][] {{Alphabets.integers(1, 3)},
                               {Alphabets.characters('a', 'c')},
                               {Alphabets.closedCharStringRange('a', 'c')},
                               {Alphabets.fromEnum(InputEnum.class)},
                               {Alphabets.fromCollection(TestUtil.ALPHABET)},
                               {Alphabets.singleton("singleton")},
                               {new FastAlphabet<>(TestUtil.IN_A, TestUtil.IN_B)},
                               {new GrowingMapAlphabet<>(TestUtil.ALPHABET)},
                               {Alphabets.fromArray('a', 'b', 'c')}};
    }

    @Test(dataProvider = "alphabets")
    public <I> void testCompactDFA(Alphabet<I> alphabet) {
        final CompactDFA<I> automaton = new CompactDFA<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.STATE_PROPS,
                                          MutableAutomatonTest.EMPTY_PROPS);
        testSerialization(automaton, alphabet);
    }

    @Test(dataProvider = "alphabets")
    public <I> void testCompactNFA(Alphabet<I> alphabet) {
        final CompactNFA<I> automaton = new CompactNFA<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.STATE_PROPS,
                                          MutableAutomatonTest.EMPTY_PROPS);
        testSerialization(automaton, alphabet);
    }

    @Test(dataProvider = "alphabets")
    public <I> void testFastDFA(Alphabet<I> alphabet) {
        final FastDFA<I> automaton = new FastDFA<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.STATE_PROPS,
                                          MutableAutomatonTest.EMPTY_PROPS);
        testSerialization(automaton, alphabet);
    }

    @Test(dataProvider = "alphabets")
    public <I> void testFastNFA(Alphabet<I> alphabet) {
        final FastNFA<I> automaton = new FastNFA<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.STATE_PROPS,
                                          MutableAutomatonTest.EMPTY_PROPS);
        testSerialization(automaton, alphabet);
    }

    @Test(dataProvider = "alphabets")
    public <I> void testCompactMealy(Alphabet<I> alphabet) {
        final CompactMealy<I, Character> automaton = new CompactMealy<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.EMPTY_PROPS,
                                          MutableAutomatonTest.TRANS_PROPS);
        testSerialization(automaton, alphabet);
    }

    @Test(dataProvider = "alphabets")
    public <I> void testFastMealy(Alphabet<I> alphabet) {
        final FastMealy<I, Character> automaton = new FastMealy<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.EMPTY_PROPS,
                                          MutableAutomatonTest.TRANS_PROPS);
        testSerialization(automaton, alphabet);
    }

    @Test(dataProvider = "alphabets")
    public <I> void testFastProbMealy(Alphabet<I> alphabet) {
        final FastProbMealy<I, Character> automaton = new FastProbMealy<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.EMPTY_PROPS,
                                          MutableAutomatonTest.PROB_TRANS_PROPS);
        testSerialization(automaton, alphabet);
    }

    @Test(dataProvider = "alphabets")
    public <I> void testCompactMoore(Alphabet<I> alphabet) {
        final CompactMoore<I, Boolean> automaton = new CompactMoore<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.STATE_PROPS,
                                          MutableAutomatonTest.EMPTY_PROPS);
        testSerialization(automaton, alphabet);
    }

    @Test(dataProvider = "alphabets")
    public <I> void testFastMoore(Alphabet<I> alphabet) {
        final FastMoore<I, Boolean> automaton = new FastMoore<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.STATE_PROPS,
                                          MutableAutomatonTest.EMPTY_PROPS);
        testSerialization(automaton, alphabet);
    }

    @Test(dataProvider = "alphabets")
    public <I> void testCompactSST(Alphabet<I> alphabet) {
        final CompactSST<I, Character> automaton = new CompactSST<>(alphabet);
        MutableAutomatonTest.fillRandomly(automaton,
                                          alphabet,
                                          MutableAutomatonTest.SST_STATE_PROPS,
                                          MutableAutomatonTest.SST_TRANS_PROPS);
        testSerialization(automaton, alphabet);
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void testSerialization(M original,
                                                                                                  Alphabet<I> alphabet) {
        final String data = xstream.toXML(original);
        @SuppressWarnings("unchecked")
        final M deserialized = (M) xstream.fromXML(data);
        testEquivalence(original, deserialized, alphabet);
    }

    private <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> void testEquivalence(M original,
                                                                                                M deserialized,
                                                                                                Alphabet<I> alphabet) {
        Assert.assertEquals(original.getStates(), deserialized.getStates());

        for (S s : original) {
            final SP origSP = original.getStateProperty(s);
            final SP deserSP = deserialized.getStateProperty(s);

            Assert.assertEquals(origSP, deserSP);

            for (I i : alphabet) {
                final Collection<T> origTrans = original.getTransitions(s, i);
                final Collection<T> deserTrans = deserialized.getTransitions(s, i);

                Assert.assertEquals(origTrans, deserTrans);

                for (T t : origTrans) {
                    final TP origTP = original.getTransitionProperty(t);
                    final TP deserTP = deserialized.getTransitionProperty(t);

                    Assert.assertEquals(origTP, deserTP);
                }
            }
        }
    }
}
