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

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.Holder;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.traversal.TraversalOrder;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.util.ts.traversal.TSTraversalAction;
import net.automatalib.util.ts.traversal.TSTraversalVisitor;

final class TraversalAutomatonCopy<S1, I1, T1, S2, I2, T2, SP2, TP2>
        extends AbstractLowLevelAutomatonCopier<S1, I1, T1, S2, I2, T2, SP2, TP2, TransitionSystem<S1, ? super I1, T1>>
        implements TSTraversalVisitor<S1, I1, T1, S2> {

    private final TraversalOrder traversalOrder;
    private final int limit;

    TraversalAutomatonCopy(TraversalOrder traversalOrder,
                                  int limit,
                                  TransitionSystem<S1, ? super I1, T1> in,
                                  Collection<? extends I1> inputs,
                                  MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
                                  Function<? super I1, ? extends I2> inputsMapping,
                                  Function<? super S1, ? extends SP2> spMapping,
                                  Function<? super T1, ? extends TP2> tpMapping,
                                  Predicate<? super S1> stateFilter,
                                  TransitionPredicate<? super S1, ? super I1, ? super T1> transFilter) {
        super(in, inputs, out, inputsMapping, spMapping, tpMapping, stateFilter, transFilter);
        this.traversalOrder = traversalOrder;
        this.limit = limit;
    }

    @Override
    public void doCopy() {
        TSTraversal.traverse(traversalOrder, in, limit, inputs, this);
    }

    @Override
    public TSTraversalAction processInitial(S1 state, Holder<S2> outData) {
        if (stateFilter.test(state)) {
            outData.value = copyInitialState(state);
            return TSTraversalAction.EXPLORE;
        }
        return TSTraversalAction.IGNORE;
    }

    @Override
    public boolean startExploration(S1 state, S2 data) {
        return true;
    }

    @Override
    public TSTraversalAction processTransition(S1 source,
                                               S2 srcData,
                                               I1 input,
                                               T1 transition,
                                               S1 succ,
                                               Holder<S2> outData) {
        if (transFilter.apply(source, input, transition) && stateFilter.test(succ)) {
            S2 succ2 = copyTransitionChecked(srcData, inputsMapping.apply(input), transition, succ);
            if (succ2 == null) {
                return TSTraversalAction.IGNORE;
            }
            outData.value = succ2;
            return TSTraversalAction.EXPLORE;
        }
        return TSTraversalAction.IGNORE;
    }

    static final class CopyMethod implements AutomatonCopyMethod {

        private final TraversalOrder traversalOrder;

        CopyMethod(TraversalOrder traversalOrder) {
            this.traversalOrder = traversalOrder;
        }

        @Override
        public <S1, I1, T1, S2, I2, T2, SP2, TP2> LowLevelAutomatonCopier<S1, S2> createLowLevelCopier(
                Automaton<S1, ? super I1, T1> in,
                Collection<? extends I1> inputs,
                MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
                Function<? super I1, ? extends I2> inputsMapping,
                Function<? super S1, ? extends SP2> spMapping,
                Function<? super T1, ? extends TP2> tpMapping,
                Predicate<? super S1> stateFilter,
                TransitionPredicate<? super S1, ? super I1, ? super T1> transitionFilter) {
            return new TraversalAutomatonCopy<>(traversalOrder,
                                                in.size(),
                                                in,
                                                inputs,
                                                out,
                                                inputsMapping,
                                                spMapping,
                                                tpMapping,
                                                stateFilter,
                                                transitionFilter);
        }

    }

}

