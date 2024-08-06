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
package net.automatalib.modelchecking;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;

/**
 * A model checker that caches calls to {@link #findCounterExample(Object, Collection, Object)}.
 *
 * @param <I>
 *         the input type
 * @param <A>
 *         the automaton type
 * @param <P>
 *         the property type
 * @param <R>
 *         the type of a counterexample
 *
 * @author Jeroen Meijer
 */
public interface ModelCheckerCache<I, A, P, R> extends ModelChecker<I, A, P, R> {

    /**
     * Clears the cache.
     */
    void clear();

    interface DFAModelCheckerCache<I, P, R> extends ModelCheckerCache<I, DFA<?, I>, P, R>, DFAModelChecker<I, P, R> {}

    interface MealyModelCheckerCache<I, O, P, R>
            extends ModelCheckerCache<I, MealyMachine<?, I, ?, O>, P, R>, MealyModelChecker<I, O, P, R> {}
}
