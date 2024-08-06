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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.TransitionSystem;

public class TSComposition<S1, S2, I, T1, T2, TS1 extends TransitionSystem<S1, I, T1>, TS2 extends TransitionSystem<S2, I, T2>>
        implements TransitionSystem<Pair<S1, S2>, I, Pair<T1, T2>> {

    protected final TS1 ts1;
    protected final TS2 ts2;

    /**
     * Constructor.
     *
     * @param ts1
     *         first transition system
     * @param ts2
     *         second transition system
     */
    public TSComposition(TS1 ts1, TS2 ts2) {
        this.ts1 = ts1;
        this.ts2 = ts2;
    }

    @Override
    public Set<Pair<S1, S2>> getInitialStates() {
        Collection<S1> init1 = ts1.getInitialStates();
        Collection<S2> init2 = ts2.getInitialStates();

        Set<Pair<S1, S2>> result = Sets.newHashSetWithExpectedSize(init1.size() * init2.size());

        for (S1 s1 : init1) {
            for (S2 s2 : init2) {
                result.add(Pair.of(s1, s2));
            }
        }

        return result;
    }

    @Override
    public Collection<Pair<T1, T2>> getTransitions(Pair<S1, S2> state, I input) {
        S1 s1 = state.getFirst();
        S2 s2 = state.getSecond();
        Collection<T1> trans1 = ts1.getTransitions(s1, input);
        Collection<T2> trans2 = ts2.getTransitions(s2, input);

        if (trans1.isEmpty() || trans2.isEmpty()) {
            return Collections.emptySet();
        }

        List<Pair<T1, T2>> result = new ArrayList<>(trans1.size() * trans2.size());

        for (T1 t1 : trans1) {
            for (T2 t2 : trans2) {
                result.add(Pair.of(t1, t2));
            }
        }

        return result;
    }

    @Override
    public Pair<S1, S2> getSuccessor(Pair<T1, T2> transition) {
        T1 t1 = transition.getFirst();
        T2 t2 = transition.getSecond();
        return Pair.of(ts1.getSuccessor(t1), ts2.getSuccessor(t2));
    }

}
