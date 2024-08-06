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
package net.automatalib.util.automata.minimizer;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;

public class InvasiveMinimizationTest extends AbstractMinimizationTest {

    @Override
    protected <I> DFA<?, I> minimizeDFA(MutableDFA<?, I> dfa, Alphabet<I> alphabet) {
        return Automata.invasiveMinimize(dfa, alphabet);
    }

    @Override
    protected <I, O> MealyMachine<?, I, ?, O> minimizeMealy(MutableMealyMachine<?, I, ?, O> mealy,
                                                            Alphabet<I> alphabet) {
        return Automata.invasiveMinimize(mealy, alphabet);
    }

    @Override
    protected boolean isPruned() {
        return true;
    }

    @Override
    protected boolean supportsPartial() {
        return true;
    }
}
