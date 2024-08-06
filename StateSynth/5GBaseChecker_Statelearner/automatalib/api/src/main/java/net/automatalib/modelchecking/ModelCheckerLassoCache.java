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
 * @see ModelCheckerCache
 *
 * @author Jeroen Meijer
 */
public interface ModelCheckerLassoCache<I, A, P, R extends Lasso<I, ?>>
        extends ModelCheckerCache<I, A, P, R>, ModelCheckerLasso<I, A, P, R> {

    interface DFAModelCheckerLassoCache<I, P> extends ModelCheckerLassoCache<I, DFA<?, I>, P, DFALasso<I>>,
                                                      DFAModelCheckerCache<I, P, DFALasso<I>>,
                                                      DFAModelCheckerLasso<I, P> {}

    interface MealyModelCheckerLassoCache<I, O, P>
            extends ModelCheckerLassoCache<I, MealyMachine<?, I, ?, O>, P, MealyLasso<I, O>>,
                    MealyModelCheckerCache<I, O, P, MealyLasso<I, O>>,
                    MealyModelCheckerLasso<I, O, P> {}
}
