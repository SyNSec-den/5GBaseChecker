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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.random.RandomUtil;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A random generator for initially connected (IC) deterministic automata.
 * <p>
 * The object state of instances of this class only determines how state and transition properties are assigned. These
 * can be set conveniently using the {@code with...} methods in a fluent interface-like manner.
 * <p>
 * For conveniently generating initially connected deterministic automata of certain types, consider using the static
 * methods defined in class {@link RandomAutomata}, such as {@link RandomAutomata#randomICDFA(Random, int, Alphabet,
 * boolean)}.
 *
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 *
 * @author Malte Isberner
 */
@SuppressWarnings("nullness") // for now the nullness of SP/TP depends on the provided suppliers
public class RandomICAutomatonGenerator<SP, TP> {

    private Function<? super Random, ? extends @Nullable SP> spSupplier = (r) -> null;
    private Function<? super Random, ? extends @Nullable TP> tpSupplier = (r) -> null;

    /**
     * Creates a random IC automaton generator instance for generating DFAs. States in generated automata will be
     * accepting or rejecting with equal probability.
     *
     * @return a random IC automaton generator instance for generating DFAs
     */
    public static RandomICAutomatonGenerator<Boolean, Void> forDFA() {
        return new RandomICAutomatonGenerator<Boolean, Void>().withStateProperties(Random::nextBoolean);
    }

    /**
     * Creates a random IC automaton generator instance for generating DFAs. The {@code acceptingRatio} parameter
     * controls the probability of a state in a generated automaton being an accepting state.
     *
     * @param acceptingRatio
     *         the (approximate) ratio of accepting states in generated automata
     *
     * @return a random IC automaton generator instance for generating DFAs
     */
    public static RandomICAutomatonGenerator<Boolean, Void> forDFA(double acceptingRatio) {
        return new RandomICAutomatonGenerator<Boolean, Void>().withStateProperties(r -> r.nextDouble() <
                                                                                        acceptingRatio);
    }

    /**
     * Sets the function for supplying state properties, and returns {@code this}.
     *
     * @param spFunc
     *         the function that supplies state properties, using a {@link Random} object as a source for randomness
     *
     * @return {@code this}
     */
    public RandomICAutomatonGenerator<SP, TP> withStateProperties(Function<? super Random, ? extends SP> spFunc) {
        this.spSupplier = Objects.requireNonNull(spFunc);
        return this;
    }

    /**
     * Sets the supplier for state properties, and returns {@code this}.
     * <p>
     * Using this function is discouraged, as it ignores the {@link Random} instance passed to the generation functions.
     * If possible, use {@link #withStateProperties(Function)}.
     *
     * @param spSupplier
     *         the supplier for state properties
     *
     * @return {@code this}
     */
    public RandomICAutomatonGenerator<SP, TP> withStateProperties(Supplier<? extends SP> spSupplier) {
        return withStateProperties(r -> spSupplier.get());
    }

    /**
     * Sets the possible state properties, and returns {@code this}. The collection is internally converted into a list,
     * from which state properties are selected using {@link RandomUtil#choose(List, Random)}. If the collection is
     * empty, {@code null} will always be chosen as the state property.
     * <p>
     * Note that if the collection contains elements several times, the probability of these elements being selected is
     * proportionally higher.
     *
     * @param possibleSps
     *         the collection of possible state properties
     *
     * @return {@code this}
     */
    public RandomICAutomatonGenerator<SP, TP> withStateProperties(Collection<? extends SP> possibleSps) {
        if (possibleSps.isEmpty()) {
            return withStateProperties(r -> null);
        }
        List<SP> spList = new ArrayList<>(possibleSps);
        return withStateProperties((r) -> RandomUtil.choose(spList, r));
    }

    /**
     * Sets the possible state properties, and returns {@code this}. State properties are selected from this array using
     * {@link RandomUtil#choose(Object[], Random)}. If the array is empty, {@code null} will always be chosen as the
     * state property.
     *
     * @param possibleSps
     *         the possible state properties
     *
     * @return {@code this}
     */
    @SafeVarargs
    public final RandomICAutomatonGenerator<SP, TP> withStateProperties(SP... possibleSps) {
        if (possibleSps.length == 0) {
            return withStateProperties(r -> null);
        }
        return withStateProperties(r -> RandomUtil.choose(possibleSps, r));
    }

    /**
     * Sets the supplier for transition properties, and returns {@code this}.
     * <p>
     * Using this function is discouraged, as it ignores the {@link Random} instance passed to the generation functions.
     * If possible, use {@link #withTransitionProperties(Function)}.
     *
     * @param tpSupplier
     *         the supplier for transition properties
     *
     * @return {@code this}
     */
    public RandomICAutomatonGenerator<SP, TP> withTransitionProperties(Supplier<? extends TP> tpSupplier) {
        return withTransitionProperties(r -> tpSupplier.get());
    }

    /**
     * Sets the function for supplying transition properties, and returns {@code this}.
     *
     * @param tpFunc
     *         the function that supplies transition properties, using a {@link Random} object as a source for
     *         randomness
     *
     * @return {@code this}
     */
    public RandomICAutomatonGenerator<SP, TP> withTransitionProperties(Function<? super Random, ? extends TP> tpFunc) {
        this.tpSupplier = Objects.requireNonNull(tpFunc);
        return this;
    }

    /**
     * Sets the possible transition properties, and returns {@code this}. Transition properties are selected from this
     * array using {@link RandomUtil#choose(Object[], Random)}. If the array is empty, {@code null} will always be
     * chosen as the state property.
     *
     * @param possibleTps
     *         the possible transition properties
     *
     * @return {@code this}
     */
    @SafeVarargs
    public final RandomICAutomatonGenerator<SP, TP> withTransitionProperties(TP... possibleTps) {
        if (possibleTps.length == 0) {
            return withTransitionProperties(r -> null);
        }
        return withTransitionProperties(Arrays.asList(possibleTps));
    }

    /**
     * Sets the possible transition properties, and returns {@code this}. The collection is internally converted into a
     * list, from which transition properties are selected using {@link RandomUtil#choose(List, Random)}. If the
     * collection is empty, {@code null} will always be chosen as the transition property.
     * <p>
     * Note that if the collection contains elements several times, the probability of these elements being selected is
     * proportionally higher.
     *
     * @param possibleTps
     *         the collection of possible transition properties
     *
     * @return {@code this}
     */
    public RandomICAutomatonGenerator<SP, TP> withTransitionProperties(Collection<? extends TP> possibleTps) {
        if (possibleTps.isEmpty()) {
            return withTransitionProperties(r -> null);
        }
        List<TP> tpList = new ArrayList<>(possibleTps);
        return withTransitionProperties((r) -> RandomUtil.choose(tpList, r));
    }

    /**
     * Generates an initially-connected (IC) deterministic automaton with the given parameters. The resulting automaton
     * is instantiated using the given {@code creator}. Note that the resulting automaton will <b>not</b> be minimized.
     *
     * @param numStates
     *         the number of states of the resulting automaton
     * @param alphabet
     *         the input alphabet of the resulting automaton
     * @param creator
     *         an {@link AutomatonCreator} for instantiating the result automaton
     * @param r
     *         the randomness source
     *
     * @return a randomly-generated IC deterministic automaton
     */
    public <I, A extends MutableDeterministic<?, I, ?, ? super SP, ? super TP>> A generateICDeterministicAutomaton(int numStates,
                                                                                                                   Alphabet<I> alphabet,
                                                                                                                   AutomatonCreator<? extends A, I> creator,
                                                                                                                   Random r) {
        A result = creator.createAutomaton(alphabet, numStates);
        return generateICDeterministicAutomaton(numStates, alphabet, result, r);
    }

    /**
     * Generates an initially connected (IC) deterministic automaton with the given parameters. Note that the automaton
     * will <b>not</b> be minized.
     *
     * @param numStates
     *         the number of states of the resulting automaton
     * @param inputs
     *         the input symbols to consider during generation
     * @param result
     *         the result automaton (should be empty)
     * @param r
     *         the randomness source
     *
     * @return the result automaton
     */
    public <I, A extends MutableDeterministic<?, I, ?, ? super SP, ? super TP>> A generateICDeterministicAutomaton(int numStates,
                                                                                                                   Collection<? extends I> inputs,
                                                                                                                   A result,
                                                                                                                   Random r) {
        MutableDeterministic.StateIntAbstraction<I, ?, ? super SP, ? super TP> resultAbs = result.stateIntAbstraction();

        List<? extends I> inputsList = CollectionsUtil.randomAccessList(inputs);

        resultAbs.addIntInitialState(spSupplier.apply(r));
        for (int i = 1; i < numStates; i++) {
            int src, succ;
            I input;
            do {
                src = r.nextInt(i);
                input = RandomUtil.choose(inputsList, r);
                succ = resultAbs.getSuccessor(src, input);
            } while (succ >= 0);
            int next = resultAbs.addIntState(spSupplier.apply(r));
            resultAbs.setTransition(src, input, next, tpSupplier.apply(r));
        }

        for (int i = 0; i < numStates; i++) {
            for (I input : inputs) {
                if (resultAbs.getSuccessor(i, input) >= 0) {
                    continue;
                }
                int succ = r.nextInt(numStates);
                resultAbs.setTransition(i, input, succ, tpSupplier.apply(r));
            }
        }

        return result;
    }

}
