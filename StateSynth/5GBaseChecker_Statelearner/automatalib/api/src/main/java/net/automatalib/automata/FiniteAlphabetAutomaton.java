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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;

public interface FiniteAlphabetAutomaton<S, I, T> extends Automaton<S, I, T>, InputAlphabetHolder<I>, GraphViewable {

    /**
     * Convenience method for accessing all (outgoing) transitions of a given state. Uses the {@link
     * #getInputAlphabet() input alphabet} of {@code this} {@link FiniteAlphabetAutomaton}.
     *
     * @param state
     *         the state for which the outgoing transitions should be fetched.
     *
     * @return all outgoing transitions of {@code state}.
     */
    default Collection<T> getTransitions(S state) {
        final List<T> result = new ArrayList<>();

        for (I i : getInputAlphabet()) {
            result.addAll(getTransitions(state, i));
        }

        return result;
    }

    @Override
    default Graph<?, ?> graphView() {
        return transitionGraphView();
    }

    default Graph<S, TransitionEdge<I, T>> transitionGraphView() {
        return transitionGraphView(getInputAlphabet());
    }

}
