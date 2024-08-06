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

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.Lasso.MealyLasso;

/**
 * A model checker where the counterexample is a lasso.
 *
 * @param <I>
 *         the input type.
 * @param <A>
 *         the automaton type.
 * @param <P>
 *         the property type.
 * @param <R>
 *         the type of a counterexample
 *
 * @author Jeroen Meijer
 */
public interface ModelCheckerLasso<I, A, P, R extends Lasso<I, ?>> extends ModelChecker<I, A, P, R> {

    /**
     * Return the multiplier for the number of times a loop of the lasso must be unrolled, relative to the size of the
     * hypothesis.
     *
     * @return the multiplier
     */
    double getMultiplier();

    /**
     * Set the multiplier for the number of times a loop of the lasso must be unrolled, relative to the size of the
     * hypothesis.
     *
     * @param multiplier
     *         the multiplier
     *
     * @throws IllegalArgumentException
     *         when {@code multiplier < 0.0}.
     */
    void setMultiplier(double multiplier);

    /**
     * Returns the minimum number of times a loop must be unrolled.
     *
     * @return the minimum
     */
    int getMinimumUnfolds();

    /**
     * Set the minimum number of times a loop must be unrolled.
     *
     * @param minimumUnfolds
     *         the minimum
     *
     * @throws IllegalArgumentException
     *         when {@code minimumUnfolds < 1}.
     */
    void setMinimumUnfolds(int minimumUnfolds);

    /**
     * Compute the number of unfolds according to {@code size}.
     *
     * @param size
     *         the number of states in the hypothesis.
     *
     * @return the number of times the loop of a lasso has to be unfolded.
     */
    default int computeUnfolds(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Illegal size: " + size);
        }
        final int relativeUnfolds = (int) Math.ceil(size * getMultiplier());
        return Math.max(getMinimumUnfolds(), relativeUnfolds);
    }

    interface DFAModelCheckerLasso<I, P>
            extends ModelCheckerLasso<I, DFA<?, I>, P, DFALasso<I>>, DFAModelChecker<I, P, DFALasso<I>> {}

    interface MealyModelCheckerLasso<I, O, P>
            extends ModelCheckerLasso<I, MealyMachine<?, I, ?, O>, P, MealyLasso<I, O>>,
                    MealyModelChecker<I, O, P, MealyLasso<I, O>> {}
}
