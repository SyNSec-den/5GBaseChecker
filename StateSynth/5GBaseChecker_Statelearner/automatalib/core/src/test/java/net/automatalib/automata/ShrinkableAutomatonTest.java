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

import java.util.Collections;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.impl.FastNFA;
import net.automatalib.automata.fsa.impl.FastNFAState;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.automata.transducers.impl.FastMealyState;
import net.automatalib.automata.util.TestUtil;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class ShrinkableAutomatonTest {

    @Test
    public void testDeterministic() {

        final FastMealy<Symbol<Character>, String> mealy = TestUtil.constructMealy();
        final StateIDs<FastMealyState<String>> stateIds = mealy.stateIDs();

        final FastMealyState<String> s0 = stateIds.getState(0);
        final FastMealyState<String> s1 = stateIds.getState(1);
        final FastMealyState<String> s2 = stateIds.getState(2);

        mealy.removeState(s2);

        final Word<Symbol<Character>> input1 = Word.fromSymbols(TestUtil.IN_B, TestUtil.IN_A, TestUtil.IN_A);
        final Word<Symbol<Character>> input2 =
                Word.fromSymbols(TestUtil.IN_B, TestUtil.IN_A, TestUtil.IN_B, TestUtil.IN_B);

        final Word<String> expectedOutput1 = Word.fromSymbols(TestUtil.OUT_ERROR, TestUtil.OUT_OK);
        final Word<String> expectedOutput2 =
                Word.fromSymbols(TestUtil.OUT_ERROR, TestUtil.OUT_OK, TestUtil.OUT_OK, TestUtil.OUT_ERROR);

        Assert.assertEquals(mealy.computeOutput(input1), expectedOutput1);
        Assert.assertEquals(mealy.computeOutput(input2), expectedOutput2);

        final FastMealyState<String> s3 = mealy.addState();
        mealy.setTransition(s3, TestUtil.IN_A, s1, TestUtil.OUT_OK);
        mealy.setTransition(s3, TestUtil.IN_B, s3, TestUtil.OUT_ERROR);

        mealy.removeState(s0, s3);

        Assert.assertEquals(mealy.getInitialState(), s3);
        Assert.assertEquals(mealy.computeOutput(input1), expectedOutput1);
        Assert.assertEquals(mealy.computeOutput(input2), expectedOutput2);
    }

    @Test
    public void testNonDeterministic() {

        final FastNFA<Symbol<Character>> nfa = TestUtil.constructNFA();
        final StateIDs<FastNFAState> stateIds = nfa.stateIDs();

        final FastNFAState s0 = stateIds.getState(0);
        final FastNFAState s1 = stateIds.getState(1);
        final FastNFAState s2 = stateIds.getState(2);

        nfa.removeState(s2);

        final Word<Symbol<Character>> input1 = Word.fromSymbols(TestUtil.IN_A, TestUtil.IN_A, TestUtil.IN_B);
        final Word<Symbol<Character>> input2 = Word.fromSymbols(TestUtil.IN_A, TestUtil.IN_B, TestUtil.IN_A);

        Assert.assertFalse(nfa.accepts(input1));
        Assert.assertTrue(nfa.accepts(input2));

        final FastNFAState s3 = nfa.addState(false);
        nfa.setTransitions(s3, TestUtil.IN_A, Collections.singleton(s1));
        nfa.setTransitions(s3, TestUtil.IN_B, Collections.singleton(s3));
        nfa.removeState(s0, s3);

        Assert.assertEquals(nfa.getInitialStates(), Collections.singleton(s3));
        Assert.assertFalse(nfa.accepts(input1));
        Assert.assertTrue(nfa.accepts(input2));

    }
}
