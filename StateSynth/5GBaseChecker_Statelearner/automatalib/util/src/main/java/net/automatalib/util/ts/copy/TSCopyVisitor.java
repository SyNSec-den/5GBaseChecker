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
package net.automatalib.util.ts.copy;

import java.util.function.Function;
import java.util.function.Predicate;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.Holder;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.ts.traversal.TSTraversalAction;
import net.automatalib.util.ts.traversal.TSTraversalVisitor;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TSCopyVisitor<S1, I1, T1, S2, I2, T2, SP2, TP2> implements TSTraversalVisitor<S1, I1, T1, S2> {

    private final MutableMapping<S1, @Nullable S2> stateMapping;
    private final MutableAutomaton<S2, I2, T2, ? super SP2, @Nullable ? super TP2> out;

    private final Function<? super I1, ? extends I2> inputMapping;
    private final Function<? super S1, ? extends SP2> spMapping;
    private final Function<? super T1, ? extends TP2> tpMapping;

    private final Predicate<? super S1> stateFilter;
    private final TransitionPredicate<? super S1, ? super I1, ? super T1> transFilter;

    public TSCopyVisitor(TransitionSystem<S1, ? super I1, T1> in,
                         MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
                         Function<? super I1, ? extends I2> inputMapping,
                         Function<? super S1, ? extends SP2> spMapping,
                         Function<? super T1, ? extends TP2> tpMapping,
                         Predicate<? super S1> stateFilter,
                         TransitionPredicate<? super S1, ? super I1, ? super T1> transFilter) {
        this.stateMapping = in.createStaticStateMapping();
        this.out = out;
        this.inputMapping = inputMapping;
        this.spMapping = spMapping;
        this.tpMapping = tpMapping;
        this.stateFilter = stateFilter;
        this.transFilter = transFilter;
    }

    @Override
    public TSTraversalAction processInitial(S1 state, Holder<S2> outData) {
        @Nullable S2 s2 = stateMapping.get(state);
        if (s2 != null) {
            out.setInitial(s2, true);
            return TSTraversalAction.IGNORE;
        } else if (!stateFilter.test(state)) {
            return TSTraversalAction.IGNORE;
        }

        SP2 sp = spMapping.apply(state);
        s2 = out.addInitialState(sp);

        stateMapping.put(state, s2);

        outData.value = s2;
        return TSTraversalAction.EXPLORE;
    }

    @Override
    public boolean startExploration(S1 state, S2 data) {
        return true;
    }

    @Override
    public TSTraversalAction processTransition(S1 source,
                                               S2 source2,
                                               I1 input,
                                               T1 transition,
                                               S1 succ,
                                               Holder<S2> outData) {
        if (!transFilter.apply(source, input, transition)) {
            return TSTraversalAction.IGNORE;
        }

        boolean ignore = false;

        @Nullable S2 succ2 = stateMapping.get(succ);
        if (succ2 == null) {
            if (!stateFilter.test(succ)) {
                return TSTraversalAction.IGNORE;
            }
            SP2 sp = spMapping.apply(succ);
            succ2 = out.addState(sp);
            stateMapping.put(succ, succ2);
        } else {
            ignore = true;
        }

        I2 input2 = inputMapping.apply(input);
        TP2 tp = tpMapping.apply(transition);

        out.addTransition(source2, input2, succ2, tp);
        outData.value = succ2;

        return ignore ? TSTraversalAction.IGNORE : TSTraversalAction.EXPLORE;
    }

    public Mapping<S1, @Nullable S2> getStateMapping() {
        return stateMapping;
    }
}
