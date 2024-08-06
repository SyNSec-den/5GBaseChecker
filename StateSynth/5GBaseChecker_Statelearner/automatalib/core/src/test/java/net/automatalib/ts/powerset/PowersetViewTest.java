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
package net.automatalib.ts.powerset;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Function;

import net.automatalib.automata.fsa.MutableNFA;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.commons.util.nid.NumericID;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class PowersetViewTest {

    @Test
    public void testFastPowerset() {
        final ConstructedSystem<FastNFA<Character>, FastNFAState> system = constructSystem(FastNFA::new);
        final FastPowersetDTS<FastNFAState, Character, ?> powersetDTS = new FastPowersetDTS<>(system.automaton);

        checkConstructedSystem(powersetDTS, system, PowersetViewTest::toFastPowersetState);
    }

    @Test
    public void testDirectPowerset() {
        final ConstructedSystem<CompactNFA<Character>, Integer> system = constructSystem(CompactNFA::new);
        final DirectPowersetDTS<Integer, Character, ?> powersetDTS = new DirectPowersetDTS<>(system.automaton);

        checkConstructedSystem(powersetDTS, system, HashSet::new);
    }

    private static <S, A extends MutableNFA<S, Character>> ConstructedSystem<A, S> constructSystem(Function<Alphabet<Character>, A> constructor) {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final A nfa = constructor.apply(alphabet);

        final S q1 = nfa.addInitialState(false);
        final S q2 = nfa.addState(false);
        final S q3 = nfa.addState(false);

        nfa.setTransitions(q1, 'a', Arrays.asList(q1, q2, q3));
        nfa.setTransitions(q2, 'b', Arrays.asList(q2, q3));
        nfa.setTransitions(q3, 'c', Collections.singleton(q3));

        return new ConstructedSystem<>(nfa, q1, q2, q3);
    }

    private static <S, OS> void checkConstructedSystem(PowersetViewTS<S, Character, ?, OS, ?> view,
                                                       ConstructedSystem<?, OS> system,
                                                       Function<Collection<OS>, S> stateConstructor) {
        final OS q1 = system.q1;
        final OS q2 = system.q2;
        final OS q3 = system.q3;

        final Collection<OS> firstStateSet = Collections.singleton(q1);
        final Collection<OS> secondStateSet = Arrays.asList(q1, q2, q3);
        final Collection<OS> thirdStateSet = Arrays.asList(q2, q3);
        final Collection<OS> fourthStateSet = Collections.singleton(q3);

        final S firstPSState = stateConstructor.apply(firstStateSet);
        final S secondPSState = stateConstructor.apply(secondStateSet);
        final S thirdPSState = stateConstructor.apply(thirdStateSet);
        final S fourthPSState = stateConstructor.apply(fourthStateSet);

        final Word<Character> firstTrace = Word.epsilon();
        final Word<Character> secondTrace = Word.fromLetter('a');
        final Word<Character> thirdTrace = Word.fromSymbols('a', 'b');
        final Word<Character> fourthTrace = Word.fromSymbols('a', 'b', 'c');

        checkPowersetTrace(view, firstPSState, firstTrace);
        checkPowersetTrace(view, secondPSState, secondTrace);
        checkPowersetTrace(view, thirdPSState, thirdTrace);
        checkPowersetTrace(view, fourthPSState, fourthTrace);

        Assert.assertEquals(firstStateSet, view.getOriginalStates(firstPSState));
        Assert.assertEquals(secondStateSet, view.getOriginalStates(secondPSState));
        Assert.assertEquals(thirdStateSet, view.getOriginalStates(thirdPSState));
        Assert.assertEquals(fourthStateSet, view.getOriginalStates(fourthPSState));
    }

    private static <S, I, T> void checkPowersetTrace(PowersetViewTS<S, I, T, ?, ?> view,
                                                     S expectedFinalState,
                                                     Word<I> traceFromInit) {
        S iter = view.getInitialState();

        for (final I i : traceFromInit) {
            final T trans = view.getTransition(iter, i);
            final S tSucc = view.getSuccessor(trans);

            Assert.assertEquals(view.getSuccessor(iter, i), tSucc);

            iter = tSucc;
        }

        Assert.assertEquals(expectedFinalState, iter);
    }

    private static <S extends NumericID> FastPowersetState<S> toFastPowersetState(Collection<S> from) {
        final FastPowersetState<S> result = new FastPowersetState<>();

        for (final S s : from) {
            result.add(s, s.getId());
        }

        return result;
    }

    private static class ConstructedSystem<A, OS> {

        private final A automaton;
        private final OS q1, q2, q3;

        ConstructedSystem(A automaton, OS q1, OS q2, OS q3) {
            this.automaton = automaton;
            this.q1 = q1;
            this.q2 = q2;
            this.q3 = q3;
        }
    }

}
