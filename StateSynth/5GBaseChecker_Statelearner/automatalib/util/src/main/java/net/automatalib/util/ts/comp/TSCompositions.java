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
package net.automatalib.util.ts.comp;

import net.automatalib.commons.util.Pair;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.TransitionSystem;

public final class TSCompositions {

    private TSCompositions() {}

    public static <S1, S2, I, T1, T2, TS1 extends TransitionSystem<S1, I, T1>, TS2 extends TransitionSystem<S2, I, T2>> TransitionSystem<Pair<S1, S2>, I, Pair<T1, T2>> compose(
            TS1 ts1,
            TS2 ts2) {
        return new TSComposition<>(ts1, ts2);
    }

    public static <S1, S2, I, T1, T2, TS1 extends DeterministicTransitionSystem<S1, I, T1>, TS2 extends DeterministicTransitionSystem<S2, I, T2>> DeterministicTransitionSystem<Pair<S1, S2>, I, Pair<T1, T2>> compose(
            TS1 ts1,
            TS2 ts2) {
        return new DTSComposition<>(ts1, ts2);
    }
}
