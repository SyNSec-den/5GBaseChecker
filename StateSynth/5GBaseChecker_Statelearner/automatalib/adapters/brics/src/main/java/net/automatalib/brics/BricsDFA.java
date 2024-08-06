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

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import net.automatalib.automata.fsa.DFA;

/**
 * Adapter class for wrapping a Brics automaton as a {@link DFA}.
 * <p>
 * This adapter is backed by the Brics automaton, so changes to the {@link Automaton} are reflected. Please note that
 * any changes which result in a loss of determinism will result in incorrect behavior exposed by this class until
 * determinism is restored.
 *
 * @author Malte Isberner
 */
public class BricsDFA extends AbstractBricsAutomaton implements DFA<State, Character> {

    /**
     * Constructor. If the given {@link Automaton} is not deterministic, it will be automatically determinized by
     * invoking {@link Automaton#determinize()}.
     * <p>
     * <b>Note:</b> Brics automata may only be partially defined (especially when created from regular expressions). If
     * you plan to use this wrapper in any structural analysis (e.g. for determining equivalence), consider using
     * {@link #BricsDFA(Automaton, boolean)} instead.
     *
     * @param automaton
     *         the Brics automaton to wrap.
     */
    public BricsDFA(Automaton automaton) {
        this(requireDeterministic(automaton), false);
    }

    /**
     * Constructor. If the given {@link Automaton} is not deterministic, it will be automatically determinized by
     * invoking {@link Automaton#determinize()}.
     * <p>
     * If the parameter {@code totalize} is set to {@code true}, an additional sink state will be added to the automaton
     * and all otherwise undefined transitions will transition the automaton into the sink. <b>Note:</b> this mutates
     * the original {@code automaton}.
     *
     * @param automaton
     *         the Brics automaton to wrap.
     * @param totalize
     *         flag, indicating whether the automaton should have a total transition function.
     */
    public BricsDFA(Automaton automaton, boolean totalize) {
        super(requireDeterministic(automaton), totalize);
    }

    private static Automaton requireDeterministic(Automaton aut) {
        if (!aut.isDeterministic()) {
            aut.determinize();
        }
        return aut;
    }

    @Override
    public State getInitialState() {
        return automaton.getInitialState();
    }

    @Override
    public State getSuccessor(State state, Character input) {
        return state.step(input.charValue());
    }

    @Override
    public State getTransition(State state, Character input) {
        return state.step(input.charValue());
    }

}
