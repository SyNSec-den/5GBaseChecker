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

import java.util.Collection;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.modelchecking.ModelChecker.MealyModelChecker;
import net.automatalib.modelchecking.ModelCheckerLasso;
import net.automatalib.modelchecking.ModelCheckerLasso.MealyModelCheckerLasso;

/**
 * Internal utility interface to provide default implementations for {@link ModelChecker}s based on a given delegator
 * (cf. {@link #getModelChecker()}). Being based in interfaces, this interface and its inner sub-interfaces allow for a
 * kind of multiple inheritance.
 *
 * @param <MC>
 *         model checker type
 * @param <I>
 *         input alphabet type
 * @param <A>
 *         automaton type
 * @param <P>
 *         property type
 * @param <R>
 *         counterexample type
 *
 * @author frohme
 */
interface InternalModelCheckerDelegator<MC extends ModelChecker<I, A, P, R>, I, A, P, R> {

    MC getModelChecker();

    /**
     * Specialization of {@link InternalModelCheckerDelegator}.
     */
    interface ModelCheckerLassoDelegator<MC extends ModelCheckerLasso<I, A, P, R>, I, A, P, R extends Lasso<I, ?>>
            extends InternalModelCheckerDelegator<MC, I, A, P, R>, ModelCheckerLasso<I, A, P, R> {

        @Override
        default double getMultiplier() {
            return getModelChecker().getMultiplier();
        }

        @Override
        default void setMultiplier(double multiplier) {
            getModelChecker().setMultiplier(multiplier);
        }

        @Override
        default int getMinimumUnfolds() {
            return getModelChecker().getMinimumUnfolds();
        }

        @Override
        default void setMinimumUnfolds(int minimumUnfolds) {
            getModelChecker().setMinimumUnfolds(minimumUnfolds);
        }
    }

    /**
     * Specialization of {@link InternalModelCheckerDelegator}.
     */
    interface MealyModelCheckerDelegator<MC extends MealyModelChecker<I, O, P, R>, I, O, P, R> extends
                                                                                               InternalModelCheckerDelegator<MC, I, MealyMachine<?, I, ?, O>, P, R>,
                                                                                               MealyModelChecker<I, O, P, R> {

        @Override
        default Collection<? super O> getSkipOutputs() {
            return getModelChecker().getSkipOutputs();
        }

        @Override
        default void setSkipOutputs(Collection<? super O> skipOutputs) {
            getModelChecker().setSkipOutputs(skipOutputs);
        }
    }

    /**
     * Specialization of {@link InternalModelCheckerDelegator}.
     */
    interface MealyModelCheckerLassoDelegator<MC extends MealyModelCheckerLasso<I, O, P>, I, O, P> extends
                                                                                                   ModelCheckerLassoDelegator<MC, I, MealyMachine<?, I, ?, O>, P, MealyLasso<I, O>>,
                                                                                                   MealyModelCheckerDelegator<MC, I, O, P, MealyLasso<I, O>>,
                                                                                                   MealyModelChecker<I, O, P, MealyLasso<I, O>> {}

}
