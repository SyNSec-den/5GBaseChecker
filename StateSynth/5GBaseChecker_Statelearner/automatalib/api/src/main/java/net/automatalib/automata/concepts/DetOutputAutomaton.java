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
package net.automatalib.automata.concepts;

import net.automatalib.automata.DeterministicAutomaton;

/**
 * An automaton which deterministically produces an output for an input word. Here, output refers to the <i>complete</i>
 * output that is made when an input word is read, not a single symbol.
 *
 * @param <S>
 *         state class
 * @param <I>
 *         input symbol class
 * @param <T>
 *         transition class
 * @param <D>
 *         output domain class
 *
 * @author Malte Isberner
 */
public interface DetOutputAutomaton<S, I, T, D> extends OutputAutomaton<S, I, T, D>, DeterministicAutomaton<S, I, T> {}
