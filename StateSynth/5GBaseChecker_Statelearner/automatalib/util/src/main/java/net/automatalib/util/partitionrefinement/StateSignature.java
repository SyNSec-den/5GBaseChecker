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

import java.util.Arrays;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class for constructing state signatures used for classifying states during {@link PaigeTarjanInitializers}.
 */
public final class StateSignature {

    private final @Nullable Object[] properties;

    private StateSignature(@Nullable Object[] properties) {
        this.properties = properties;
    }

    @SafeVarargs
    public static <SP, TP> StateSignature byFullSignature(SP stateProperty, TP... transitionProperties) {
        @Nullable Object[] properties = new Object[transitionProperties.length + 1];
        System.arraycopy(transitionProperties, 0, properties, 0, transitionProperties.length);
        properties[transitionProperties.length] = stateProperty;
        return new StateSignature(properties);
    }

    public static StateSignature byFullSignature(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton,
                                                 int state) {
        int numInputs = automaton.numInputs();
        @Nullable Object[] properties = new Object[numInputs + 1];
        fillTransitionProperties(automaton, state, properties);
        properties[numInputs] = automaton.getStateProperty(state);
        return new StateSignature(properties);
    }

    public static <S, I> StateSignature byFullSignature(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                        Alphabet<I> alphabet,
                                                        S state) {
        int numInputs = alphabet.size();
        @Nullable Object[] properties = new Object[numInputs + 1];
        fillTransitionProperties(automaton, alphabet, state, properties);
        properties[numInputs] = automaton.getStateProperty(state);
        return new StateSignature(properties);
    }

    @SafeVarargs
    public static <TP> StateSignature byTransitionProperties(TP... properties) {
        return new StateSignature(properties);
    }

    public static StateSignature byTransitionProperties(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton,
                                                        int state) {
        int numInputs = automaton.numInputs();
        @Nullable Object[] properties = new Object[numInputs];
        fillTransitionProperties(automaton, state, properties);
        return new StateSignature(properties);
    }

    public static <S, I> StateSignature byTransitionProperties(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                               Alphabet<I> alphabet,
                                                               S state) {
        int numInputs = alphabet.size();
        @Nullable Object[] properties = new Object[numInputs];
        for (int i = 0; i < numInputs; i++) {
            I sym = alphabet.getSymbol(i);
            properties[i] = automaton.getTransitionProperty(state, sym);
        }
        return new StateSignature(properties);
    }

    private static void fillTransitionProperties(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton,
                                                 int state,
                                                 @Nullable Object[] properties) {
        int numInputs = automaton.numInputs();
        for (int i = 0; i < numInputs; i++) {
            properties[i] = automaton.getTransitionProperty(state, i);
        }
    }

    private static <S, I> void fillTransitionProperties(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                        Alphabet<I> alphabet,
                                                        S state,
                                                        @Nullable Object[] properties) {
        int numInputs = alphabet.size();
        for (int i = 0; i < numInputs; i++) {
            I sym = alphabet.getSymbol(i);
            properties[i] = automaton.getTransitionProperty(state, sym);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StateSignature)) {
            return false;
        }

        final StateSignature that = (StateSignature) o;
        return Arrays.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(properties);
    }
}
