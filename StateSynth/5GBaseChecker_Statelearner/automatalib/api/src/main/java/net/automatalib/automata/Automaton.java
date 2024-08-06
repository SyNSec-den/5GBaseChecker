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

import net.automatalib.automata.graphs.AutomatonGraphView;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.graphs.Graph;
import net.automatalib.ts.TransitionSystem;

/**
 * Basic interface for an automaton. An automaton is a {@link TransitionSystem} with a finite number of states.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 *
 * @author Malte Isberner
 */
public interface Automaton<S, I, T> extends TransitionSystem<S, I, T>, SimpleAutomaton<S, I> {

    /**
     * Obtains a {@link Graph graph} view of the transition graph of this automaton, taking into account the specified
     * input symbols. The transitions are represented as {@link TransitionEdge}s in the graph.
     *
     * @param inputs
     *         the input symbols to consider
     *
     * @return a graph view of the transition graph of this automaton for the given input symbols
     */
    default Graph<S, TransitionEdge<I, T>> transitionGraphView(Collection<? extends I> inputs) {
        return AutomatonGraphView.create(this, inputs);
    }
}
