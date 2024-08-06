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
package net.automatalib.util.ts.transducers;

import net.automatalib.commons.util.Pair;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.util.ts.comp.DTSComposition;

@SuppressWarnings("nullness") //nullness of composed states and transitions depends on partiality
final class DetMealyComposition<S1, S2, I, T1, T2, O1, O2, A1 extends MealyTransitionSystem<S1, I, T1, O1>, A2 extends MealyTransitionSystem<S2, I, T2, O2>>
        extends DTSComposition<S1, S2, I, T1, T2, A1, A2>
        implements MealyTransitionSystem<Pair<S1, S2>, I, Pair<T1, T2>, Pair<O1, O2>> {

    DetMealyComposition(A1 ts1, A2 ts2) {
        this(ts1, ts2, true);
    }

    DetMealyComposition(A1 ts1, A2 ts2, boolean partial) {
        super(ts1, ts2, partial);
    }

    @Override
    public Pair<O1, O2> getTransitionOutput(Pair<T1, T2> transition) {
        final T1 t1 = transition.getFirst();
        final T2 t2 = transition.getSecond();

        final O1 firstOutput = t1 == null ? null : ts1.getTransitionOutput(t1);
        final O2 secondOutput = t2 == null ? null : ts2.getTransitionOutput(t2);
        return Pair.of(firstOutput, secondOutput);
    }
}
