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
package net.automatalib.util.partitionrefinement;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.automata.equivalence.NearLinearEquivalenceTest;
import net.automatalib.util.automata.minimizer.paigetarjan.PaigeTarjanMinimization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class PaigeTarjanTest {

    private static final String SINK_OUTPUT = "sink";

    /**
     * Builds binary tree (partial due to 4 input symbols), whose leaves end in a sink and add two unreachable states.
     * s3 and s5 are equivalent, s8, s9 equivalent and unreachable.
     */
    public static CompactMealy<Integer, String> getMealy() {
        final Alphabet<Integer> alphabet = Alphabets.integers(1, 4);

        // @formatter:off
        return AutomatonBuilders.<Integer, String>newMealy(alphabet)
                                .withInitial(0)
                                .from(0)
                                    .on(2).withOutput("2,4").to(1)
                                    .on(1).withOutput("1,4").to(2)
                                .from(1)
                                    .on(1).withOutput("1,3").to(3)
                                    .on(4).withOutput("2,3").to(4)
                                .from(2)
                                    .on(2).withOutput("2,3").to(5)
                                    .on(4).withOutput("1,3").to(6)
                                .from(3).on(3, 4).withOutput("").to(7)
                                .from(4).on(1, 3).withOutput("").to(7)
                                .from(5).on(3, 4).withOutput("").to(7)
                                .from(6).on(2, 3).withOutput("").to(7)
                                .from(8)
                                    .on(1).withOutput(SINK_OUTPUT).loop()
                                    .on(2).withOutput(SINK_OUTPUT).to(9)
                                .from(9).on(1, 2).withOutput(SINK_OUTPUT).loop()
                                .create();
        // @formatter:on
    }

    public static CompactDFA<Character> getDFA() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');

        // @formatter:off
        return AutomatonBuilders.newDFA(alphabet)
                                .withInitial(0)
                                .from(0)
                                    .on('a').to(1)
                                    .on('b').to(2)
                                    .on('c').to(3)
                                .from(3).on('a', 'b', 'c').loop()
                                .withAccepting(0)
                                .create();
        // @formatter:on
    }

    @Test
    public void testDFAMinimization() {
        final CompactDFA<Character> dfa = getDFA();
        final CompactDFA<Character> stateMinimized = PaigeTarjanMinimization.minimizeDFA(dfa);

        // here, states 1, 2, 3 should fall together because they are all rejecting
        Assert.assertEquals(stateMinimized.size(), 2);
        // equivalence checks full state signature
        Assert.assertFalse(Automata.testEquivalence(dfa, stateMinimized, dfa.getInputAlphabet()));
        // when ignoring undefined transitions, they should be equivalent
        Assert.assertNull(NearLinearEquivalenceTest.findSeparatingWord(dfa,
                                                                       stateMinimized,
                                                                       dfa.getInputAlphabet(),
                                                                       true));

        final CompactDFA<Character> fullMinimized = PaigeTarjanMinimization.minimizeUniversal(dfa,
                                                                                              dfa.getInputAlphabet(),
                                                                                              new CompactDFA.Creator<>(),
                                                                                              AutomatonInitialPartitioning.BY_FULL_SIGNATURE,
                                                                                              null);

        Assert.assertEquals(fullMinimized.size(), 3);
        // BY_FULL_SIGNATURE should yield full-signature-equivalent model
        Assert.assertTrue(Automata.testEquivalence(dfa, fullMinimized, dfa.getInputAlphabet()));
    }

    @Test
    public void testMealyMinimizationByStateProperties() {
        testMealyConfiguration(AutomatonInitialPartitioning.BY_STATE_PROPERTY, null, 1, false);

        // when using false (a non-used state property) we want essentially no collapsing of states, because all states
        // reach an undefined transition at a different point in time
        testMealyConfiguration(AutomatonInitialPartitioning.BY_STATE_PROPERTY, false, 7, true);
    }

    @Test
    public void testMealyMinimizationByTransitionProperties() {
        final CompactMealy<Integer, String> mealy = getMealy();

        testMealyConfiguration(AutomatonInitialPartitioning.BY_TRANSITION_PROPERTIES,
                               StateSignature.byTransitionProperties(mealy, mealy.getInputAlphabet(), 6),
                               7,
                               true);
        testMealyConfiguration(AutomatonInitialPartitioning.BY_TRANSITION_PROPERTIES,
                               StateSignature.byTransitionProperties(SINK_OUTPUT,
                                                                     SINK_OUTPUT,
                                                                     SINK_OUTPUT,
                                                                     SINK_OUTPUT),
                               7,
                               true);
        testMealyConfiguration(AutomatonInitialPartitioning.BY_TRANSITION_PROPERTIES, null, 7, true);
    }

    @Test
    public void testMealyMinimizationByFullProperties() {
        final CompactMealy<Integer, String> mealy = getMealy();

        testMealyConfiguration(AutomatonInitialPartitioning.BY_FULL_SIGNATURE,
                               StateSignature.byFullSignature(mealy, mealy.getInputAlphabet(), 6),
                               7,
                               true);
        testMealyConfiguration(AutomatonInitialPartitioning.BY_FULL_SIGNATURE,
                               StateSignature.byFullSignature(null, SINK_OUTPUT, SINK_OUTPUT, SINK_OUTPUT, SINK_OUTPUT),
                               7,
                               true);
        testMealyConfiguration(AutomatonInitialPartitioning.BY_FULL_SIGNATURE, null, 7, true);
    }

    private void testMealyConfiguration(AutomatonInitialPartitioning ap,
                                        Object sinkClassification,
                                        int expectedSize,
                                        boolean equivalent) {
        final CompactMealy<Integer, String> mealy = getMealy();

        final CompactMealy<Integer, String> minimized = PaigeTarjanMinimization.minimizeUniversal(mealy,
                                                                                                  mealy.getInputAlphabet(),
                                                                                                  new CompactMealy.Creator<>(),
                                                                                                  ap,
                                                                                                  sinkClassification);

        Assert.assertEquals(minimized.size(), expectedSize);
        Assert.assertEquals(Automata.testEquivalence(mealy, minimized, mealy.getInputAlphabet()), equivalent);
    }
}