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

/**
 * This package contains the basic interfaces for automata.
 * <p>
 * An automaton (in the AutomataLib context) is a finite-state {@link net.automatalib.ts.TransitionSystem transition
 * system}. Like transition systems, automata are in general nondeterministic, but can be specialized to be {@link
 * net.automatalib.automata.DeterministicAutomaton deterministic}.
 * <p>
 * The {@link net.automatalib.automata.Automaton Automaton} interface has no inherent semantics. Special types of
 * automata, such as {@link net.automatalib.automata.fsa.DFA DFAs} or {@link net.automatalib.automata.transducers.MealyMachine
 * Mealy machines} can be found in the respective subpackages.
 *
 * @author Malte Isberner
 * @see net.automatalib.ts
 */
package net.automatalib.automata;