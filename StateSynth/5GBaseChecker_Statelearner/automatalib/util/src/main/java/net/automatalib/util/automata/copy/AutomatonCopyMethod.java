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
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.traversal.TraversalOrder;

public interface AutomatonCopyMethod {

    AutomatonCopyMethod STATE_BY_STATE = PlainAutomatonCopy::new;
    AutomatonCopyMethod DFS = new TraversalAutomatonCopy.CopyMethod(TraversalOrder.DEPTH_FIRST);
    AutomatonCopyMethod BFS = new TraversalAutomatonCopy.CopyMethod(TraversalOrder.BREADTH_FIRST);

    <S1, I1, T1, S2, I2, T2, SP2, TP2> LowLevelAutomatonCopier<S1, S2> createLowLevelCopier(Automaton<S1, ? super I1, T1> in,
                                                                                            Collection<? extends I1> inputs,
                                                                                            MutableAutomaton<S2, I2, T2, ? super SP2, ? super TP2> out,
                                                                                            Function<? super I1, ? extends I2> inputsMapping,
                                                                                            Function<? super S1, ? extends SP2> spMapping,
                                                                                            Function<? super T1, ? extends TP2> tpMapping,
                                                                                            Predicate<? super S1> stateFilter,
                                                                                            TransitionPredicate<? super S1, ? super I1, ? super T1> transitionFilter);

}
