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
package net.automatalib.automata;

import java.util.Collection;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.graphs.UniversalAutomatonGraphView;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.ts.UniversalTransitionSystem;

/**
 * A universal automaton is a generalized representation of automata, with a unified access to the properties of states
 * and transitions. See {@link UniversalTransitionSystem} for a further explanation of this concept.
 *
 * @param <S>
 *         state class
 * @param <I>
 *         input symbol class
 * @param <T>
 *         transition class
 * @param <SP>
 *         state property class
 * @param <TP>
 *         transition property class
 *
 * @author Malte Isberner
 */
public interface UniversalAutomaton<S, I, T, SP, TP>
        extends Automaton<S, I, T>, UniversalTransitionSystem<S, I, T, SP, TP> {

    @Override
    default UniversalGraph<S, TransitionEdge<I, T>, SP, TransitionEdge.Property<I, TP>> transitionGraphView(Collection<? extends I> inputs) {
        return UniversalAutomatonGraphView.create(this, inputs);
    }

}
