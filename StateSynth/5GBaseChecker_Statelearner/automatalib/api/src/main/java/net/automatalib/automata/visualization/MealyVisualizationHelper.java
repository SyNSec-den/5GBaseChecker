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
package net.automatalib.automata.visualization;

import java.util.Map;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transducers.TransitionOutputAutomaton;

public class MealyVisualizationHelper<S, I, T, O>
        extends AutomatonVisualizationHelper<S, I, T, TransitionOutputAutomaton<S, I, T, O>> {

    public MealyVisualizationHelper(TransitionOutputAutomaton<S, I, T, O> automaton) {
        super(automaton);
    }

    @Override
    public boolean getEdgeProperties(S src, TransitionEdge<I, T> edge, S tgt, Map<String, String> properties) {
        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
            return false;
        }

        final StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(edge.getInput()).append(" / ");
        O output = automaton.getTransitionOutput(edge.getTransition());
        if (output != null) {
            labelBuilder.append(output);
        }
        properties.put(EdgeAttrs.LABEL, labelBuilder.toString());
        return true;
    }

}
