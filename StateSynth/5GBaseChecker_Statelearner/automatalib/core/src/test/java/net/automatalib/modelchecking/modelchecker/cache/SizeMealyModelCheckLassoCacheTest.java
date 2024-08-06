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
package net.automatalib.modelchecking.modelchecker.cache;

import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.ModelCheckerLassoCache.MealyModelCheckerLassoCache;
import net.automatalib.modelchecking.modelchecker.cache.ModelCheckerMock.MealyModelCheckerMock;
import net.automatalib.words.impl.Alphabets;
import org.mockito.Mockito;

/**
 * @author frohme
 */
public class SizeMealyModelCheckLassoCacheTest<I, O>
        extends AbstractSizeModelCheckLassoCacheTest<I, MealyLasso<I, O>, MutableMealyMachine<?, I, ?, O>, MealyModelCheckerMock<I, O>, MealyModelCheckerLassoCache<I, O, Object>> {

    @Override
    protected MealyModelCheckerMock<I, O> getModelChecker(MutableMealyMachine<?, I, ?, O> automaton,
                                                          Object property,
                                                          MealyLasso<I, O> counterexample) {
        return new MealyModelCheckerMock<>(automaton, property, counterexample);
    }

    @Override
    protected MealyModelCheckerLassoCache<I, O, Object> getCache(MealyModelCheckerMock<I, O> mockup) {
        return new SizeMealyModelCheckerLassoCache<>(mockup);
    }

    @Override
    protected MutableMealyMachine<?, I, ?, O> getAutomaton() {
        return new CompactMealy<>(Alphabets.fromArray());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected MealyLasso<I, O> getCounterexample() {
        return (MealyLasso<I, O>) Mockito.mock(MealyLasso.class);
    }
}

