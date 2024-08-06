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
package net.automatalib.util.partitionrefinement;

import java.util.function.IntFunction;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton.FullIntAbstraction;
import net.automatalib.words.Alphabet;

/**
 * This enum allows to conveniently specify how the states of a deterministic automaton are initially partitioned when
 * initializing the partition refinement data structure.
 *
 * @author Malte Isberner
 * @author frohme
 */
public enum AutomatonInitialPartitioning {
    /**
     * States are initially partitioned by their state property, i.e., states with the same state property are initially
     * placed in the same partition class.
     */
    BY_STATE_PROPERTY {
        @Override
        public IntFunction<?> initialClassifier(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton) {
            return automaton::getStateProperty;
        }
    },

    /**
     * States are initially partitioned by all of their transition properties, i.e., states with the same input
     * symbol/transition property combinations are initially placed in the same partition class. Note that if
     * transitions are missing, a {@code null} property is assumed.
     * <p>
     * For constructing custom sink classifications required by the partial {@link PaigeTarjanInitializers}, please
     * refer to the {@link StateSignature} methods.
     *
     * @see StateSignature#byTransitionProperties(Object[])
     * @see StateSignature#byTransitionProperties(FullIntAbstraction, int)
     * @see StateSignature#byTransitionProperties(UniversalDeterministicAutomaton, Alphabet, Object)
     */
    BY_TRANSITION_PROPERTIES {
        @Override
        public IntFunction<?> initialClassifier(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton) {
            return (s) -> StateSignature.byTransitionProperties(automaton, s);
        }
    },

    /**
     * States are initially partitioned by both their state properties and their transition properties. This mode can be
     * regarded as a combination of {@link #BY_STATE_PROPERTY} and {@link #BY_TRANSITION_PROPERTIES}, resulting in the
     * coarsest initial partition that refines both partitions obtained using the other modes.
     * <p>
     * For constructing custom sink classifications required by the partial {@link PaigeTarjanInitializers}, please
     * refer to the {@link StateSignature} methods.
     *
     * @see StateSignature#byFullSignature(Object, Object[])
     * @see StateSignature#byFullSignature(FullIntAbstraction, int)
     * @see StateSignature#byFullSignature(UniversalDeterministicAutomaton, Alphabet, Object)
     */
    BY_FULL_SIGNATURE {
        @Override
        public IntFunction<?> initialClassifier(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton) {
            return (s) -> StateSignature.byFullSignature(automaton, s);
        }
    };

    public abstract IntFunction<?> initialClassifier(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton);

}
