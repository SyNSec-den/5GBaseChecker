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

import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.ModelCheckerCache.DFAModelCheckerCache;
import net.automatalib.modelchecking.modelchecker.cache.ModelCheckerMock.DFAModelCheckerMock;
import net.automatalib.words.impl.Alphabets;
import org.mockito.Mockito;

/**
 * @author frohme
 */
public class SizeDFAModelCheckerCacheTest<I>
        extends AbstractSizeModelCheckerCacheTest<I, DFALasso<I>, MutableDFA<?, I>, DFAModelCheckerMock<I>, DFAModelCheckerCache<I, Object, DFALasso<I>>> {

    @Override
    protected DFAModelCheckerMock<I> getModelChecker(MutableDFA<?, I> automaton,
                                                     Object property,
                                                     DFALasso<I> counterexample) {
        return new DFAModelCheckerMock<>(automaton, property, counterexample);
    }

    @Override
    protected DFAModelCheckerCache<I, Object, DFALasso<I>> getCache(DFAModelCheckerMock<I> mockup) {
        return new SizeDFAModelCheckerCache<>(mockup);
    }

    @Override
    protected MutableDFA<?, I> getAutomaton() {
        return new CompactDFA<>(Alphabets.fromArray());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected DFALasso<I> getCounterexample() {
        return (DFALasso<I>) Mockito.mock(DFALasso.class);
    }
}
