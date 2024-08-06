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
package net.automatalib.util.automata.copy;

import java.util.Random;
import java.util.function.Predicate;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.ShrinkableAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.predicates.TransitionPredicates;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public abstract class AbstractAutomatonCopyTest {

    private static final Alphabet<Character> INPUT_ALPHABET = Alphabets.characters('a', 'c');
    private static final Alphabet<Integer> OUTPUT_ALPHABET = Alphabets.integers(1, 3);

    private static final int SIZE = 10;

    private CompactMealy<Character, Integer> mealy;
    private CompactMealy<Character, Integer> partialMealy;

    private CompactDFA<Character> dfa;
    private CompactDFA<Character> partialDfa;

    @BeforeClass
    public void setUp() {
        mealy = RandomAutomata.randomMealy(new Random(42), SIZE, INPUT_ALPHABET, OUTPUT_ALPHABET, false);
        partialMealy = RandomAutomata.randomMealy(new Random(42), SIZE, INPUT_ALPHABET, OUTPUT_ALPHABET, false);

        dfa = RandomAutomata.randomDFA(new Random(42), SIZE, INPUT_ALPHABET, false);
        partialDfa = RandomAutomata.randomDFA(new Random(42), SIZE, INPUT_ALPHABET, false);

        makePartial(partialMealy,
                    INPUT_ALPHABET,
                    statePredicateForRemoval(partialMealy),
                    TransitionPredicates.inputIs('c'));
        makePartial(partialDfa,
                    INPUT_ALPHABET,
                    statePredicateForRemoval(partialDfa),
                    TransitionPredicates.inputIs('c'));
    }

    /**
     * Check equivalence of a total automaton (with transition properties).
     */
    @Test
    public void testCompactMealy() {
        checkEquivalence(mealy, INPUT_ALPHABET, new CompactMealy.Creator<>());
    }

    /**
     * Check equivalence of a partial automaton (with transition properties).
     */
    @Test
    public void testPartialCompactMealy() {
        checkEquivalence(partialMealy, INPUT_ALPHABET, new CompactMealy.Creator<>());
    }

    /**
     * Check equivalence of a total automaton with filter operations (with transition properties).
     */
    @Test
    public void testFilteredCompactMealy() {
        checkFilteredEquivalence(mealy,
                                 INPUT_ALPHABET,
                                 new CompactMealy.Creator<>(),
                                 statePredicateForRemoval(mealy).negate(),
                                 TransitionPredicates.inputIsNot('c'),
                                 partialMealy);
    }

    /**
     * Check equivalence of a total automaton (with state properties).
     */
    @Test
    public void testCompactDFA() {
        checkEquivalence(dfa, INPUT_ALPHABET, new CompactDFA.Creator<>());
    }

    /**
     * Check equivalence of a partial automaton (with state properties).
     */
    @Test
    public void testPartialCompactDFA() {
        checkEquivalence(partialDfa, INPUT_ALPHABET, new CompactDFA.Creator<>());
    }

    /**
     * Check equivalence of a total automaton with filter operations (with state properties).
     */
    @Test
    public void testFilteredCompactDFA() {
        checkFilteredEquivalence(dfa,
                                 INPUT_ALPHABET,
                                 new CompactDFA.Creator<>(),
                                 statePredicateForRemoval(dfa).negate(),
                                 TransitionPredicates.inputIsNot('c'),
                                 partialDfa);
    }

    protected abstract AutomatonCopyMethod getCopyMethod();

    private <S, I, T> void makePartial(MutableDeterministic<S, I, T, ?, ?> automaton,
                                       Alphabet<I> alphabet,
                                       Predicate<S> stateFilterForRemoval,
                                       TransitionPredicate<S, I, T> transitionFilterForRemoval) {

        for (final S s : automaton) {
            for (final I i : alphabet) {
                for (final T t : automaton.getTransitions(s, i)) {
                    if (transitionFilterForRemoval.apply(s, i, t)) {
                        automaton.removeTransition(s, i, t);
                    }
                }
            }
        }

        for (final S s : automaton) {
            if (stateFilterForRemoval.test(s)) {
                ShrinkableAutomaton.unlinkState(automaton, s, null, alphabet);
            }
        }
    }

    private <A extends MutableDeterministic<S, I, T, SP, TP>, S, I, T, SP, TP> void checkEquivalence(A source,
                                                                                                     Alphabet<I> alphabet,
                                                                                                     AutomatonCreator<A, I> targetCreator) {
        final A target = targetCreator.createAutomaton(alphabet);
        AutomatonLowLevelCopy.copy(getCopyMethod(), source, alphabet, target);

        Assert.assertTrue(Automata.testEquivalence(source, target, alphabet));
    }

    private <A extends MutableDeterministic<S, I, T, SP, TP>, S, I, T, SP, TP> void checkFilteredEquivalence(A source,
                                                                                                             Alphabet<I> alphabet,
                                                                                                             AutomatonCreator<A, I> targetCreator,
                                                                                                             Predicate<S> sPred,
                                                                                                             TransitionPredicate<S, I, T> tPred,
                                                                                                             A expectedTarget) {

        final A target = targetCreator.createAutomaton(alphabet);
        AutomatonLowLevelCopy.copy(getCopyMethod(), source, alphabet, target, sPred, tPred);

        Assert.assertTrue(Automata.testEquivalence(expectedTarget, target, alphabet));
    }

    private static Predicate<Integer> statePredicateForRemoval(UniversalDeterministicAutomaton<Integer, ?, ?, ?, ?> automaton) {
        return s -> s == null || (s.equals(automaton.getInitialState()) && s > SIZE / 2);
    }
}
