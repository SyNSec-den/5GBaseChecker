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
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("nullness") // nullness of composed states and transitions depends on partiality
public class DTSComposition<S1, S2, I, T1, T2, TS1 extends DeterministicTransitionSystem<S1, I, T1>, TS2 extends DeterministicTransitionSystem<S2, I, T2>>
        implements DeterministicTransitionSystem<Pair<S1, S2>, I, Pair<T1, T2>> {

    protected final TS1 ts1;
    protected final TS2 ts2;
    protected final boolean allowPartial;

    public DTSComposition(TS1 ts1, TS2 ts2) {
        this(ts1, ts2, false);
    }

    public DTSComposition(TS1 ts1, TS2 ts2, boolean allowPartial) {
        this.ts1 = ts1;
        this.ts2 = ts2;
        this.allowPartial = allowPartial;
    }

    @Override
    public Pair<S1, S2> getInitialState() {
        return Pair.of(ts1.getInitialState(), ts2.getInitialState());
    }

    @Override
    public @Nullable Pair<T1, T2> getTransition(Pair<@Nullable S1, @Nullable S2> state, I input) {
        S1 s1 = state.getFirst();

        T1 t1 = (s1 == null) ? null : ts1.getTransition(s1, input);
        if (t1 == null && !allowPartial) {
            return null;
        }

        S2 s2 = state.getSecond();

        T2 t2 = (s2 == null) ? null : ts2.getTransition(s2, input);
        if (t2 == null && !allowPartial) {
            return null;
        }

        return t1 == null && t2 == null ? null : Pair.of(t1, t2);
    }

    @Override
    public Pair<S1, S2> getSuccessor(Pair<@Nullable T1, @Nullable T2> transition) {
        T1 t1 = transition.getFirst();
        T2 t2 = transition.getSecond();
        return Pair.of((t1 == null) ? null : ts1.getSuccessor(t1), (t2 == null) ? null : ts2.getSuccessor(t2));
    }

}
