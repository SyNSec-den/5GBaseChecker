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
package net.automatalib.automata.transducers.probabilistic;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.Probabilistic;
import net.automatalib.automata.concepts.TransitionOutput;
import net.automatalib.ts.UniversalTransitionSystem;

public interface ProbabilisticMealyMachine<S, I, T, O> extends Automaton<S, I, T>,
                                                               TransitionOutput<T, O>,
                                                               UniversalTransitionSystem<S, I, T, Void, ProbabilisticOutput<O>>,
                                                               Probabilistic<T> {

    @Override
    ProbabilisticOutput<O> getTransitionProperty(T transition);
}
