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

import java.util.Collections;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.ModelCheckerCache;
import net.automatalib.modelchecking.ModelCheckerLasso;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Abstract super class for size based modelchecker caches.
 *
 * @param <I>
 *         input symbol type
 * @param <R>
 *         counterexample type
 * @param <MA>
 *         mutable automaton type
 * @param <MC>
 *         model checker type
 * @param <C>
 *         cache type
 *
 * @author frohme
 */
public abstract class AbstractSizeModelCheckerCacheTest<I, R extends Lasso<I, ?>, MA extends MutableAutomaton<?, I, ?, ?, ?>, MC extends ModelCheckerLasso<I, ? super MA, Object, R> & ModelCheckCounter, C extends ModelCheckerCache<I, ? super MA, Object, R>> {

    private Object property;
    private R counterexample;

    private MC modelCheckerMockUp;
    protected C cache;
    private MA automaton;

    @BeforeClass
    public void setUp() {
        this.property = new Object();
        this.counterexample = getCounterexample();

        this.automaton = getAutomaton();
        this.modelCheckerMockUp = getModelChecker(automaton, property, counterexample);
        this.cache = getCache(modelCheckerMockUp);
    }

    @Test
    public void testCacheMiss() {
        final Lasso<?, ?> ce = this.cache.findCounterExample(this.automaton, Collections.emptyList(), property);
        Assert.assertSame(ce, counterexample);
        Assert.assertEquals(this.modelCheckerMockUp.getChecks(), 1);
    }

    @Test(dependsOnMethods = "testCacheMiss")
    public void testCacheHit() {
        final Lasso<?, ?> ce = this.cache.findCounterExample(this.automaton, Collections.emptyList(), property);
        Assert.assertSame(ce, counterexample);
        Assert.assertEquals(this.modelCheckerMockUp.getChecks(), 1);
    }

    @Test(dependsOnMethods = "testCacheHit")
    public void testSizeIncrease() {
        this.automaton.addState();
        final Lasso<?, ?> ce = this.cache.findCounterExample(this.automaton, Collections.emptyList(), property);
        Assert.assertSame(ce, counterexample);
        Assert.assertEquals(this.modelCheckerMockUp.getChecks(), 2);
    }

    @Test(dependsOnMethods = "testSizeIncrease")
    public void testCacheClear() {
        this.cache.clear();
        final Object ce = this.cache.findCounterExample(this.automaton, Collections.emptyList(), property);
        Assert.assertSame(ce, counterexample);
        Assert.assertEquals(this.modelCheckerMockUp.getChecks(), 3);
    }

    protected abstract MC getModelChecker(MA automaton, Object property, R counterexample);

    protected abstract C getCache(MC mockup);

    protected abstract MA getAutomaton();

    protected abstract R getCounterexample();

}
