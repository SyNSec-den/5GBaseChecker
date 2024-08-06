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

public final class Transducers {

    private Transducers() {}

    public static <S1, S2, I, T1, T2, O1, O2, TS1 extends MealyTransitionSystem<S1, I, T1, O1>, TS2 extends MealyTransitionSystem<S2, I, T2, O2>> MealyTransitionSystem<Pair<S1, S2>, I, Pair<T1, T2>, Pair<O1, O2>> combine(
            TS1 ts1,
            TS2 ts2) {
        return new DetMealyComposition<>(ts1, ts2);
    }
}
