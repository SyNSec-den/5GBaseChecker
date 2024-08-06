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
package net.automatalib.util.automata.minimizer.paigetarjan;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton.FullIntAbstraction;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.minimizer.hopcroft.HopcroftMinimization;
import net.automatalib.util.partitionrefinement.AutomatonInitialPartitioning;
import net.automatalib.util.partitionrefinement.PaigeTarjan;
import net.automatalib.util.partitionrefinement.PaigeTarjanExtractors;
import net.automatalib.util.partitionrefinement.PaigeTarjanInitializers;
import net.automatalib.util.partitionrefinement.StateSignature;
import net.automatalib.words.Alphabet;

/**
 * A utility class that offers short-hand methods for minimizing automata using the partition refinement approach of
 * {@link PaigeTarjan}.
 * <p>
 * This implementation is specifically tailored towards partial automata by allowing to provide a custom {@code
 * sinkClassification}, which will be used as the on-demand "successor" of undefined transitions. When extracting
 * information from the {@link PaigeTarjan} datastructure via {@link PaigeTarjanExtractors}, the original automaton is
 * used to determine the state-transitions from one partition block to another. If the {@code sinkClassification} did
 * not match the signature of any existing state, no transition will enter the artificial partition block of the sink.
 * Consequently, this class will always prune unreachable states, because otherwise we might not return a minimal
 * automaton.
 * <p>
 * For minimizing complete automata, use {@link HopcroftMinimization}.
 *
 * @author frohme
 * @see PaigeTarjan
 * @see HopcroftMinimization
 */
public final class PaigeTarjanMinimization {

    private PaigeTarjanMinimization() {}

    /**
     * Minimizes the given DFA. The result is returned in the form of a {@link CompactDFA}, using the input alphabet
     * obtained via <code>dfa.{@link InputAlphabetHolder#getInputAlphabet() getInputAlphabet()}</code>.
     *
     * @param dfa
     *         the DFA to minimize
     *
     * @return a minimized version of the specified DFA
     */
    public static <S, I, A extends DFA<S, I> & InputAlphabetHolder<I>> CompactDFA<I> minimizeDFA(A dfa) {
        return minimizeDFA(dfa, dfa.getInputAlphabet());
    }

    /**
     * Minimizes the given DFA. The result is returned in the form of a {@link CompactDFA}.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     *
     * @return a minimized version of the specified DFA
     */
    public static <I> CompactDFA<I> minimizeDFA(DFA<?, I> dfa, Alphabet<I> alphabet) {
        return minimizeDFA(dfa, alphabet, new CompactDFA.Creator<>());
    }

    /**
     * Minimizes the given DFA. The result is returned in the form of a {@link MutableDFA}, constructed by the given
     * {@code creator}.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     * @param creator
     *         the creator for constructing the automata instance to return
     *
     * @return a minimized version of the specified DFA
     */
    public static <A extends MutableDFA<?, I>, I> A minimizeDFA(DFA<?, I> dfa,
                                                                Alphabet<I> alphabet,
                                                                AutomatonCreator<A, I> creator) {
        return minimizeUniversal(dfa, alphabet, creator, AutomatonInitialPartitioning.BY_STATE_PROPERTY, Boolean.FALSE);
    }

    /**
     * Minimizes the given Mealy machine. The result is returned in the form of a {@link CompactMealy}, using the
     * alphabet obtained via <code>mealy.{@link InputAlphabetHolder#getInputAlphabet() getInputAlphabet()}</code>.
     *
     * @param mealy
     *         the Mealy machine to minimize
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <S, I, T, O, A extends MealyMachine<S, I, T, O> & InputAlphabetHolder<I>> CompactMealy<I, O> minimizeMealy(
            A mealy) {
        return minimizeMealy(mealy, mealy.getInputAlphabet());
    }

    /**
     * Minimizes the given Mealy machine. The result is returned in the form of a {@link CompactMealy}.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O> CompactMealy<I, O> minimizeMealy(MealyMachine<?, I, ?, O> mealy, Alphabet<I> alphabet) {
        return minimizeMealy(mealy, alphabet, new CompactMealy.Creator<>());
    }

    /**
     * Minimizes the given Mealy machine. The result is returned in the form of a {@link MutableMealyMachine},
     * constructed by the given {@code creator}.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param creator
     *         the creator for constructing the automata instance to return
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <A extends MutableMealyMachine<?, I, ?, O>, I, O> A minimizeMealy(MealyMachine<?, I, ?, O> mealy,
                                                                                    Alphabet<I> alphabet,
                                                                                    AutomatonCreator<A, I> creator) {
        return minimizeUniversal(mealy,
                                 alphabet,
                                 creator,
                                 AutomatonInitialPartitioning.BY_TRANSITION_PROPERTIES,
                                 StateSignature.byTransitionProperties(new Object[alphabet.size()]));
    }

    /**
     * Minimizes the given automaton depending on the given partitioning function. The {@code sinkClassification} is
     * used to describe the signature of the sink state ("successor" of undefined transitions) and may introduce a new,
     * on-thy-fly equivalence class if it doesn't match a signature of any existing state. See the {@link
     * StateSignature} class for creating signatures for existing states.
     *
     * @param automaton
     *         the automaton to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param creator
     *         the creator for constructing the automata instance to return
     * @param ap
     *         the initial partitioning function, determining how states will be distinguished
     * @param sinkClassification
     *         the classification used when an undefined transition is encountered
     *
     * @return the minimized automaton, initially constructed from the given {@code creator}.
     *
     * @see AutomatonInitialPartitioning
     * @see StateSignature
     */
    public static <I, T, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A minimizeUniversal(
            UniversalDeterministicAutomaton<?, I, T, SP, TP> automaton,
            Alphabet<I> alphabet,
            AutomatonCreator<A, I> creator,
            AutomatonInitialPartitioning ap,
            Object sinkClassification) {

        final PaigeTarjan pt = new PaigeTarjan();
        final FullIntAbstraction<T, SP, TP> abs = automaton.fullIntAbstraction(alphabet);

        PaigeTarjanInitializers.initDeterministic(pt, abs, ap.initialClassifier(abs), sinkClassification);

        pt.initWorklist(false);
        pt.computeCoarsestStablePartition();

        return PaigeTarjanExtractors.toDeterministic(pt,
                                                     creator,
                                                     alphabet,
                                                     abs,
                                                     abs::getStateProperty,
                                                     abs::getTransitionProperty,
                                                     true);
    }

}
