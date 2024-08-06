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

import java.util.Arrays;
import java.util.List;

import net.automatalib.automata.util.TestUtil;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public abstract class AbstractMutableMooreTest {

    protected abstract MutableMooreMachine<?, Symbol<Character>, ?, String> getMoore();

    @Test
    public void testTrace() {
        final MutableMooreMachine<?, Symbol<Character>, ?, String> fm = getMoore();
        final List<Symbol<Character>> input = Arrays.asList(TestUtil.IN_A, TestUtil.IN_B, TestUtil.IN_A, TestUtil.IN_B);

        final Word<String> output = fm.computeOutput(input);

        Assert.assertEquals(output,
                            Word.fromSymbols(TestUtil.OUT_OK,
                                             TestUtil.OUT_OK,
                                             TestUtil.OUT_ERROR,
                                             TestUtil.OUT_ERROR,
                                             TestUtil.OUT_OK));
    }

    @Test
    public void testPartialTrace() {
        testPartialTraceInternal(getMoore());
    }

    private <S, T> void testPartialTraceInternal(MutableMooreMachine<S, Symbol<Character>, T, String> fm) {

        final S aSucc = fm.getSuccessor(fm.getInitialState(), TestUtil.IN_A);
        final T transToRemove = fm.getTransition(aSucc, TestUtil.IN_A);
        fm.removeTransition(aSucc, TestUtil.IN_A, transToRemove);

        final Word<Symbol<Character>> input =
                Word.fromSymbols(TestUtil.IN_A, TestUtil.IN_B, TestUtil.IN_B, TestUtil.IN_A, TestUtil.IN_A);
        final Word<String> output = fm.computeOutput(input);

        Assert.assertEquals(output,
                            Word.fromSymbols(TestUtil.OUT_OK,
                                             TestUtil.OUT_OK,
                                             TestUtil.OUT_ERROR,
                                             TestUtil.OUT_OK,
                                             TestUtil.OUT_OK));
    }
}
