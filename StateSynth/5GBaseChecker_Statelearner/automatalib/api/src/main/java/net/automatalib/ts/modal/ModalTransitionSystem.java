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
package net.automatalib.ts.modal;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.graphs.TransitionEdge.Property;
import net.automatalib.automata.graphs.UniversalAutomatonGraphView;
import net.automatalib.automata.visualization.MTSVisualizationHelper;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.visualization.VisualizationHelper;

/**
 * A modal transition system is a {@link UniversalAutomaton} in which transitions can be marked as {@link
 * net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType#MAY may} or {@link
 * net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType#MUST must} edges. Since most MTS-based algorithms
 * require a finite transition domain, this includes a finite set input symbols. This is achieved by sub-typing the
 * {@link UniversalFiniteAlphabetAutomaton} interface, which additionally inherits several convenience methods for
 * working with MTSs.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <TP>
 *         (specific) transition property type
 *
 * @author msc
 */
public interface ModalTransitionSystem<S, I, T, TP extends ModalEdgeProperty>
        extends UniversalFiniteAlphabetAutomaton<S, I, T, Void, TP> {

    @Override
    default Iterator<S> iterator() {
        return UniversalFiniteAlphabetAutomaton.super.iterator();
    }

    @Override
    default int size() {
        return UniversalFiniteAlphabetAutomaton.super.size();
    }

    @Override
    default UniversalGraph<S, TransitionEdge<I, T>, Void, Property<I, TP>> transitionGraphView(Collection<? extends I> inputs) {
        return new MTSGraphView<>(this, inputs);
    }

    class MTSGraphView<S, I, T, TP extends ModalEdgeProperty, M extends ModalTransitionSystem<S, I, T, TP>>
            extends UniversalAutomatonGraphView<S, I, T, Void, TP, M> {

        public MTSGraphView(M mts, Collection<? extends I> inputs) {
            super(mts, inputs);
        }

        @Override
        public VisualizationHelper<S, TransitionEdge<I, T>> getVisualizationHelper() {
            return new MTSVisualizationHelper<>(automaton);
        }
    }
}
