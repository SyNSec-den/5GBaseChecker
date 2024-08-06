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
package net.automatalib.ts;

import net.automatalib.automata.Automaton;
import net.automatalib.graphs.FiniteKripkeStructure;
import net.automatalib.graphs.concepts.KripkeInterpretation;

/**
 * A finite Kripke Transition System combines the properties of a {@link Automaton finite transition system} and a
 * {@link KripkeInterpretation}.
 *
 * @param <S>
 *         state class
 * @param <T>
 *         transition class
 * @param <AP>
 *         atomic proposition class
 * @param <I>
 *         input symbol class
 *
 * @author Malte Isberner
 * @see KripkeInterpretation
 * @see FiniteKripkeStructure
 */
public interface FiniteKTS<S, I, T, AP> extends Automaton<S, I, T>, KripkeInterpretation<S, AP> {}
