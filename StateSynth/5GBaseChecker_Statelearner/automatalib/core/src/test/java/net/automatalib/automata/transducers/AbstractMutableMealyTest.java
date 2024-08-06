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
package net.automatalib.automata.transducers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.automatalib.automata.util.TestUtil;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Maik Merten
 * @author frohme
 */
public abstract class AbstractMutableMealyTest {

    protected abstract MutableMealyMachine<?, Symbol<Character>, ?, String> getMealy();

    @Test
    public void testTrace() {
        MutableMealyMachine<?, Symbol<Character>, ?, String> fm = getMealy();

        final List<Symbol<Character>> trace = Arrays.asList(TestUtil.IN_A, TestUtil.IN_A, TestUtil.IN_A);
        final Word<String> output = fm.computeOutput(trace);

        Assert.assertTrue(fm.trace(trace, new ArrayList<>(trace.size())));

        Assert.assertEquals(output.size(), 3);

        Assert.assertEquals(output.getSymbol(0), TestUtil.OUT_OK);
        Assert.assertEquals(output.getSymbol(1), TestUtil.OUT_OK);
        Assert.assertEquals(output.getSymbol(2), TestUtil.OUT_ERROR);
    }

    @Test
    public void testRemoveTransition() {
        testRemoveTransitionInternal(getMealy());
    }

    private <S, T> void testRemoveTransitionInternal(MutableMealyMachine<S, Symbol<Character>, T, String> fm) {
        List<Symbol<Character>> trace = new ArrayList<>();
        trace.add(TestUtil.IN_A);
        trace.add(TestUtil.IN_A);

        S state = fm.getSuccessor(fm.getInitialState(), trace);
        T trans = fm.getTransition(state, TestUtil.IN_A);

        fm.removeTransition(state, TestUtil.IN_A, trans);

        trace.add(TestUtil.IN_A);

        Assert.assertFalse(fm.trace(trace, new ArrayList<>()));
        Assert.assertNull(fm.getSuccessor(fm.getInitialState(), trace));
    }

    @Test
    public void testRemoveAllTransitions() {
        testRemoveAllTransitions(getMealy());
    }

    private <S, T> void testRemoveAllTransitions(MutableMealyMachine<S, Symbol<Character>, T, String> fm) {
        List<Symbol<Character>> trace = new ArrayList<>();
        trace.add(TestUtil.IN_A);
        trace.add(TestUtil.IN_A);

        S state = fm.getSuccessor(fm.getInitialState(), trace);

        fm.removeAllTransitions(state, TestUtil.IN_A);

        trace.add(TestUtil.IN_A);

        Assert.assertNull(fm.getSuccessor(fm.getInitialState(), trace));
    }

    @Test
    public void testRedefineTransition() {
        testRedefineTransition(getMealy());
    }

    private <S, T> void testRedefineTransition(MutableMealyMachine<S, Symbol<Character>, T, String> fm) {
        List<Symbol<Character>> trace = new ArrayList<>();
        trace.add(TestUtil.IN_A);
        trace.add(TestUtil.IN_A);

        // find last state in 3-state automaton
        S laststate = fm.getSuccessor(fm.getInitialState(), trace);

        // prepare and execute trace
        trace.add(TestUtil.IN_A);
        List<String> output = new ArrayList<>();
        fm.trace(trace, output);

        // compare trace output with expectations
        Assert.assertEquals(output.size(), 3);
        Assert.assertEquals(output.get(2), TestUtil.OUT_ERROR);

        // redefine transition with diverging output
        fm.removeAllTransitions(laststate, TestUtil.IN_A);
        fm.addTransition(laststate, TestUtil.IN_A, fm.getInitialState(), TestUtil.OUT_OK);

        // retrace
        output.clear();
        fm.trace(trace, output);

        // compare output
        Assert.assertEquals(output.size(), 3);
        Assert.assertEquals(output.get(2), TestUtil.OUT_OK);

    }

}
