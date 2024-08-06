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

import java.util.HashSet;

import com.google.common.collect.Sets;
import net.automatalib.automata.concepts.StateLocalInput;
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
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class StateLocalInputTest {

    private static final Alphabet<Integer> ALPHABET = Alphabets.integers(1, 4);

    @Test
    public void testCompactDFA() {
        this.testAutomaton(new CompactDFA<>(ALPHABET));
    }

    @Test
    public void testCompactNFA() {
        this.testAutomaton(new CompactNFA<>(ALPHABET));
    }

    @Test
    public void testFastDFA() {
        this.testAutomaton(new FastDFA<>(ALPHABET));
    }

    @Test
    public void testFastNFA() {
        this.testAutomaton(new FastNFA<>(ALPHABET));
    }

    @Test
    public void testCompactMealy() {
        this.testAutomaton(new CompactMealy<>(ALPHABET));
    }

    @Test
    public void testFastMealy() {
        this.testAutomaton(new FastMealy<>(ALPHABET));
    }

    @Test
    public void testFastProbMealy() {
        this.testAutomaton(new FastProbMealy<>(ALPHABET));
    }

    @Test
    public void testCompactMoore() {
        this.testAutomaton(new CompactMoore<>(ALPHABET));
    }

    @Test
    public void testFastMoore() {
        this.testAutomaton(new FastMoore<>(ALPHABET));
    }

    @Test
    public void testCompactSST() {
        this.testAutomaton(new CompactSST<>(ALPHABET));
    }

    private <M extends MutableAutomaton<S, Integer, T, SP, TP> & StateLocalInput<S, Integer>, S, T, SP, TP> void testAutomaton(
            final M automaton) {

        // construct cyclic automaton: symbols increase clock-wise and decrease counter-clock-wise
        final S s1 = automaton.addInitialState();
        final S s2 = automaton.addState();
        final S s3 = automaton.addState();
        final S s4 = automaton.addState();

        automaton.addTransition(s1, 1, s2, null);
        automaton.addTransition(s2, 2, s3, null);
        automaton.addTransition(s3, 3, s4, null);
        automaton.addTransition(s4, 4, s1, null);

        automaton.addTransition(s1, 4, s4, null);
        automaton.addTransition(s4, 3, s3, null);
        automaton.addTransition(s3, 2, s2, null);
        automaton.addTransition(s2, 1, s1, null);

        // check defined inputs
        Assert.assertEquals(new HashSet<>(automaton.getLocalInputs(s1)), Sets.newHashSet(1, 4));
        Assert.assertEquals(new HashSet<>(automaton.getLocalInputs(s2)), Sets.newHashSet(1, 2));
        Assert.assertEquals(new HashSet<>(automaton.getLocalInputs(s3)), Sets.newHashSet(2, 3));
        Assert.assertEquals(new HashSet<>(automaton.getLocalInputs(s4)), Sets.newHashSet(3, 4));

    }

}
