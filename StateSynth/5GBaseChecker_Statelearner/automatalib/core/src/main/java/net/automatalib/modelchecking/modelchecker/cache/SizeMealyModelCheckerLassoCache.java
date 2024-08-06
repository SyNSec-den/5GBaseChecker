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

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.ModelCheckerLasso.MealyModelCheckerLasso;
import net.automatalib.modelchecking.ModelCheckerLassoCache.MealyModelCheckerLassoCache;
import net.automatalib.modelchecking.modelchecker.cache.InternalModelCheckerDelegator.MealyModelCheckerLassoDelegator;

/**
 * @see SizeDFAModelCheckerCache
 */
public class SizeMealyModelCheckerLassoCache<I, O, P>
        extends SizeModelCheckerCache<I, MealyMachine<?, I, ?, O>, P, MealyLasso<I, O>> implements
                                                                                        MealyModelCheckerLassoCache<I, O, P>,
                                                                                        MealyModelCheckerLassoDelegator<MealyModelCheckerLasso<I, O, P>, I, O, P> {

    private final MealyModelCheckerLasso<I, O, P> modelChecker;

    public SizeMealyModelCheckerLassoCache(MealyModelCheckerLasso<I, O, P> modelChecker) {
        super(modelChecker);
        this.modelChecker = modelChecker;
    }

    @Override
    public MealyModelCheckerLasso<I, O, P> getModelChecker() {
        return modelChecker;
    }
}
