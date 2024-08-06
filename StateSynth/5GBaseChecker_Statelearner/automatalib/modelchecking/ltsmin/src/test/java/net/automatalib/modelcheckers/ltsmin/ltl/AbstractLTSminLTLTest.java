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
package net.automatalib.modelcheckers.ltsmin.ltl;

import net.automatalib.modelcheckers.ltsmin.AbstractLTSminTest;
import net.automatalib.modelcheckers.ltsmin.LTSminVersion;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractLTSminLTLTest<A, L extends Lasso<String, ?>> extends AbstractLTSminTest<A, L> {

    @Override
    protected Word<String> getInput() {
        return new WordBuilder<String>().repeatAppend(4, "a").toWord();
    }

    @Override
    public abstract AbstractLTSminLTL<String, A, L> getModelChecker();

    @Override
    protected LTSminVersion getRequiredVersion() {
        return AbstractLTSminLTL.REQUIRED_VERSION;
    }

    @Test
    public void testComputeUnfolds() {
        Assert.assertEquals(getModelChecker().computeUnfolds(1), 3);
        getModelChecker().setMultiplier(2.0);
        Assert.assertEquals(getModelChecker().computeUnfolds(2), 4);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testComputeUnfoldsExcept() {
        getModelChecker().computeUnfolds(0);
    }

    @Test
    public void testSetMinimumUnfolds() {
        getModelChecker().setMinimumUnfolds(1337);
        Assert.assertEquals(getModelChecker().getMinimumUnfolds(), 1337);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetMinimumUnfoldsExcept() {
        getModelChecker().setMinimumUnfolds(0);
    }

    @Test
    public void testSetMultiplier() {
        getModelChecker().setMultiplier(1337.0);
        Assert.assertEquals(getModelChecker().getMultiplier(), 1337.0, 0.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSetMultiplierExcept() {
        getModelChecker().setMultiplier(-1.0);
    }
}
