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
import net.automatalib.automata.fsa.NFA;

/**
 * Adapter class for wrapping a Brics automaton as an {@link NFA}.
 * <p>
 * This adapter is backed by the Brics automaton, so changes to the {@link Automaton} are reflected.
 * <p>
 * As a DFA can be regarded as a special case of an NFA, using this class on a Brics {@link Automaton} will always work.
 * However, determining successor states for input characters might be much less efficient than when using a {@link
 * BricsDFA}.
 *
 * @author Malte Isberner
 */
public class BricsNFA extends AbstractBricsAutomaton implements NFA<State, Character> {

    /**
     * Constructor.
     * <p>
     * <b>Note:</b> Brics automata may only be partially defined (especially when created from regular expressions). If
     * you plan to use this wrapper in any structural analysis (e.g. for determining equivalence), consider using
     * {@link #BricsNFA(Automaton, boolean)} instead.
     *
     * @param automaton
     *         the Brics automaton to wrap.
     */
    public BricsNFA(Automaton automaton) {
        this(automaton, false);
    }

    /**
     * Constructor.
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
    public BricsNFA(Automaton automaton, boolean totalize) {
        super(automaton, totalize);
    }
}
