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
package net.automatalib.brics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.DFABuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class BricsDFATest {

    private Automaton bricsAutomaton;
    private BricsDFA dfa;

    @BeforeClass
    public void setUp() {
        // manually construct automaton accepting the regular expression "a(b*|bb+)c?d"
        // we do this manually to ensure non-determinism to check our determinization
        final Automaton a = BasicAutomata.makeChar('a');
        final Automaton b = BasicAutomata.makeChar('b');
        final Automaton c = BasicAutomata.makeChar('c');
        final Automaton d = BasicAutomata.makeChar('d');

        this.bricsAutomaton = BasicOperations.concatenate(Arrays.asList(a,
                                                                        BasicOperations.union(BasicOperations.repeat(b),
                                                                                              BasicOperations.repeat(b,
                                                                                                                     2)),
                                                                        BasicOperations.optional(c),
                                                                        d));
        assert !this.bricsAutomaton.isDeterministic();
        this.dfa = new BricsDFA(bricsAutomaton);
    }

    @Test
    public void testDeterminism() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'd');

        for (State s : this.dfa) {
            for (Character i : alphabet) {
                Assert.assertTrue(this.dfa.getTransitions(s, i).size() <= 1);
            }
        }
    }

    @Test
    public void testWordAcceptance() {
        List<String> strings = Arrays.asList("ad", "acd", "abbe", "abbce", "acce", "abcd");

        for (String s : strings) {
            Assert.assertEquals(dfa.accepts(Word.fromString(s)), bricsAutomaton.run(s));
        }
    }

    @Test
    public void testStructuralEquality() {
        AbstractBricsAutomaton.GraphView graphView = dfa.graphView();

        Assert.assertEquals(dfa.getInitialState(), bricsAutomaton.getInitialState());

        Set<State> states1 = new HashSet<>(bricsAutomaton.getStates());
        Set<State> states2 = new HashSet<>(dfa.getStates());

        Assert.assertEquals(states1, states2);

        for (State s : dfa) {
            Assert.assertEquals(dfa.isAccepting(s), s.isAccept());

            Set<Transition> trans1 = new HashSet<>(graphView.getOutgoingEdges(s));
            Set<Transition> trans2 = new HashSet<>(s.getTransitions());

            Assert.assertEquals(trans1, trans2);
        }
    }

    @Test
    public void testEquivalence() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final DFA<?, Character> target = new DFABuilder<>(new CompactDFA<>(alphabet)).withInitial("s0")
                                                                                     .from("s0")
                                                                                     .on('a', 'b', 'c')
                                                                                     .loop()
                                                                                     .create();

        final Automaton automaton1 = new RegExp("[a-b]{2}").toAutomaton();
        final Automaton automaton2 = new RegExp("[a-b]{2}").toAutomaton();

        final BricsDFA partialBrics = new BricsDFA(automaton1);
        final BricsDFA totalBrics = new BricsDFA(automaton2, true);

        final Word<Character> partialSeqWord = Automata.findShortestSeparatingWord(target, partialBrics, alphabet);
        final Word<Character> totalSeqWord = Automata.findShortestSeparatingWord(target, totalBrics, alphabet);

        Assert.assertEquals(partialSeqWord, Word.fromLetter('c'));
        Assert.assertEquals(totalSeqWord, Word.fromSymbols('a', 'a'));
    }
}
