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
package net.automatalib.util.automata.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.ts.TransitionPredicate;

final class PlainAutomatonCopy<S1, I1, T1, S2, I2, T2, SP2, TP2>
        extends AbstractLowLevelAutomatonCopier<S1, I1, T1, S2, I2, T2, SP2, TP2, Automaton<S1, ? super I1, T1>> {

    PlainAutomatonCopy(Automaton<S1, ? super I1, T1> in,
                              Collection<? extends I1> inputs,
                              MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
                              Function<? super I1, ? extends I2> inputsMapping,
                              Function<? super S1, ? extends SP2> spMapping,
                              Function<? super T1, ? extends TP2> tpMapping,
                              Predicate<? super S1> stateFilter,
                              TransitionPredicate<? super S1, ? super I1, ? super T1> transFilter) {
        super(in, inputs, out, inputsMapping, spMapping, tpMapping, stateFilter, transFilter);
    }

    @Override
    public void doCopy() {
        List<StateRec<S1, S2>> outStates = new ArrayList<>(in.size());

        for (S1 s1 : in) {
            if (stateFilter.test(s1)) {
                S2 s2 = copyState(s1);
                outStates.add(new StateRec<>(s1, s2));
            }
        }

        for (StateRec<S1, S2> p : outStates) {
            S1 s1 = p.inState;
            S2 s2 = p.outState;

            for (I1 i1 : inputs) {
                I2 i2 = inputsMapping.apply(i1);
                Collection<? extends T1> transitions1 = in.getTransitions(s1, i1);
                copyTransitions(s2, i2, transitions1.stream().filter(t -> transFilter.apply(s1, i1, t)).iterator());
            }
        }

        updateInitials();
    }

    private static class StateRec<S1, S2> {

        private final S1 inState;
        private final S2 outState;

        StateRec(S1 inState, S2 outState) {
            this.inState = inState;
            this.outState = outState;
        }
    }

}