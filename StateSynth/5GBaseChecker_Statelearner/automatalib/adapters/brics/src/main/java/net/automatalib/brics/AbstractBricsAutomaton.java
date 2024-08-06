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
package net.automatalib.brics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import net.automatalib.automata.fsa.FiniteStateAcceptor;
import net.automatalib.automata.graphs.AbstractAutomatonGraphView;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.visualization.VisualizationHelper;

/**
 * Base class for Brics automata adapters.
 *
 * @author Malte Isberner
 */
public abstract class AbstractBricsAutomaton implements FiniteStateAcceptor<State, Character>, GraphViewable {

    protected final Automaton automaton;

    /**
     * Constructor.
     *
     * @param automaton
     *         the Brics automaton to wrap.
     * @param totalize
     *         flag, indicating whether the automaton should have a total transition function.
     *
     * @see Automaton#totalize()
     */
    public AbstractBricsAutomaton(Automaton automaton, boolean totalize) {
        this.automaton = automaton;

        if (totalize) {
            State s = new State();
            s.addTransition(new Transition(Character.MIN_VALUE, Character.MAX_VALUE, s));
            for (State p : automaton.getStates()) {
                int maxi = Character.MIN_VALUE;
                for (Transition t : p.getSortedTransitions(false)) {
                    if (t.getMin() > maxi) {
                        p.addTransition(new Transition((char) maxi, (char) (t.getMin() - 1), s));
                    }
                    if (t.getMin() + 1 > maxi) {
                        maxi = t.getMax() + 1;
                    }
                }
                if (maxi <= Character.MAX_VALUE) {
                    p.addTransition(new Transition((char) maxi, Character.MAX_VALUE, s));
                }
            }
        }
    }

    /**
     * Retrieves the Brics automaton object.
     *
     * @return the brics automaton object
     */
    public Automaton getBricsAutomaton() {
        return automaton;
    }

    @Override
    public boolean isAccepting(State state) {
        return state.isAccept();
    }

    @Override
    public Collection<State> getTransitions(State state, Character input) {
        Collection<Transition> transitions = state.getSortedTransitions(false);

        Set<State> result = new HashSet<>();

        for (Transition t : transitions) {
            char min = t.getMin();
            if (input < min) {
                break;
            }
            char max = t.getMax();
            if (input > max) {
                continue;
            }
            result.add(t.getDest());
        }
        return result;
    }

    @Override
    public Set<State> getInitialStates() {
        return Collections.singleton(automaton.getInitialState());
    }

    @Override
    public Collection<State> getStates() {
        return automaton.getStates();
    }

    @Override
    public GraphView graphView() {
        return new GraphView();
    }

    public class GraphView extends AbstractAutomatonGraphView<State, AbstractBricsAutomaton, Transition>
            implements UniversalGraph<State, Transition, Boolean, BricsTransitionProperty> {

        public GraphView() {
            super(AbstractBricsAutomaton.this);
        }

        @Override
        public Collection<Transition> getOutgoingEdges(State node) {
            return node.getTransitions();
        }

        @Override
        public State getTarget(Transition edge) {
            return edge.getDest();
        }

        @Override
        public VisualizationHelper<State, Transition> getVisualizationHelper() {
            return new BricsVisualizationHelper(AbstractBricsAutomaton.this);
        }

        @Override
        public Boolean getNodeProperty(State node) {
            return node.isAccept();
        }

        @Override
        public BricsTransitionProperty getEdgeProperty(Transition edge) {
            return new BricsTransitionProperty(edge);
        }
    }

}
