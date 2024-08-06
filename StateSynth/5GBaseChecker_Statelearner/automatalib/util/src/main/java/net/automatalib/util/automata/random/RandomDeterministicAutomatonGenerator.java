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
package net.automatalib.util.automata.random;

import java.util.Collection;
import java.util.Random;

import net.automatalib.automata.MutableDeterministic;

public class RandomDeterministicAutomatonGenerator<S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>>
        extends RandomAutomatonGenerator<S, I, T, SP, TP, A> {

    public RandomDeterministicAutomatonGenerator(Random random,
                                                 Collection<? extends I> inputs,
                                                 Collection<? extends SP> stateProps,
                                                 Collection<? extends TP> transProps,
                                                 A automaton) {
        super(random, inputs, stateProps, transProps, automaton);
    }

    @SuppressWarnings("nullness") // nullness depends on the emptiness of the passed lists
    public void addTransitions() {
        for (S s : states) {
            for (I in : inputs) {
                S succ = randomState();
                TP prop = randomTransProperty();
                automaton.addTransition(s, in, succ, prop);
            }
        }
    }

}
