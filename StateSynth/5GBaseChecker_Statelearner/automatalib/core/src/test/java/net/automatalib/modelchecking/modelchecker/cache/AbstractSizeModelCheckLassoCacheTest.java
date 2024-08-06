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

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.ModelCheckerLasso;
import net.automatalib.modelchecking.ModelCheckerLassoCache;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public abstract class AbstractSizeModelCheckLassoCacheTest<I, R extends Lasso<I, ?>, MA extends MutableAutomaton<?, I, ?, ?, ?>, MC extends ModelCheckerLasso<I, ? super MA, Object, R> & ModelCheckCounter, C extends ModelCheckerLassoCache<I, ? super MA, Object, R>>
        extends AbstractSizeModelCheckerCacheTest<I, R, MA, MC, C> {

    private static final double NEW_MULTIPLIER = 1.23;
    private static final int NEW_UNFOLDS = 123;

    @Test
    public void testInitialValues() {
        Assert.assertEquals(super.cache.getMultiplier(), ModelCheckerMock.DEFAULT_MULTIPLIER);
        Assert.assertEquals(super.cache.getMinimumUnfolds(), ModelCheckerMock.DEFAULT_UNFOLDS);
    }

    @Test(dependsOnMethods = "testInitialValues")
    public void testUpdatingValues() {
        super.cache.setMinimumUnfolds(NEW_UNFOLDS);
        super.cache.setMultiplier(NEW_MULTIPLIER);

        this.checkUpdatedValues();
    }

    @AfterClass
    public void checkUpdatedValues() {
        Assert.assertEquals(super.cache.getMultiplier(), NEW_MULTIPLIER);
        Assert.assertEquals(super.cache.getMinimumUnfolds(), NEW_UNFOLDS);
    }
}
