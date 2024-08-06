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
package net.automatalib.util.automata.transducers;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.predicates.TransitionPredicates;
import net.automatalib.words.Alphabet;

/**
 * Various utility methods to filter Mealy machines.
 *
 * @author Malte Isberner
 */
public final class MealyFilter {

    private MealyFilter() {
        // prevent instantiation
    }

    /**
     * Returns a Mealy machine with all transitions removed that have one of the specified output values. The resulting
     * Mealy machine will not contain any unreachable states.
     * <p>
     * This is a convenience varargs overload of {@link #pruneTransitionsWithOutput(MealyMachine, Alphabet,
     * Collection)}.
     *
     * @param in
     *         the input Mealy machine
     * @param inputs
     *         the input alphabet
     * @param outputs
     *         the outputs to remove
     *
     * @return a Mealy machine with all transitions removed that have one of the specified outputs.
     */
    @SafeVarargs
    public static <I, O> CompactMealy<I, O> pruneTransitionsWithOutput(MealyMachine<?, I, ?, O> in,
                                                                       Alphabet<I> inputs,
                                                                       O... outputs) {
        return pruneTransitionsWithOutput(in, inputs, Arrays.asList(outputs));
    }

    /**
     * Returns a Mealy machine with all transitions removed that have one of the specified output values. The resulting
     * Mealy machine will not contain any unreachable states.
     *
     * @param in
     *         the input Mealy machine
     * @param inputs
     *         the input alphabet
     * @param outputs
     *         the outputs to remove
     *
     * @return a Mealy machine with all transitions removed that have one of the specified outputs.
     */
    public static <I, O> CompactMealy<I, O> pruneTransitionsWithOutput(MealyMachine<?, I, ?, O> in,
                                                                       Alphabet<I> inputs,
                                                                       Collection<? super O> outputs) {
        return filterByOutput(in, inputs, o -> !outputs.contains(o));
    }

    public static <I, O> CompactMealy<I, O> filterByOutput(MealyMachine<?, I, ?, O> in,
                                                           Alphabet<I> inputs,
                                                           Predicate<? super O> outputPred) {
        CompactMealy<I, O> out = new CompactMealy<>(inputs);
        filterByOutput(in, inputs, out, outputPred);
        return out;
    }

    public static <S1, T1, S2, I, O> Mapping<S1, S2> filterByOutput(MealyMachine<S1, I, T1, O> in,
                                                                    Collection<? extends I> inputs,
                                                                    MutableMealyMachine<S2, I, ?, O> out,
                                                                    Predicate<? super O> outputPred) {
        TransitionPredicate<S1, I, T1> transPred = TransitionPredicates.outputSatisfies(in, outputPred);

        return AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, in, inputs, out, s -> true, transPred);
    }

    /**
     * Returns a Mealy machine with all transitions removed that have an output not among the specified values. The
     * resulting Mealy machine will not contain any unreachable states.
     * <p>
     * This is a convenience varargs overload of {@link #retainTransitionsWithOutput(MealyMachine, Alphabet,
     * Collection)}.
     *
     * @param in
     *         the input Mealy machine
     * @param inputs
     *         the input alphabet
     * @param outputs
     *         the outputs to retain
     *
     * @return a Mealy machine with all transitions retained that have one of the specified outputs.
     */
    @SafeVarargs
    public static <I, O> CompactMealy<I, O> retainTransitionsWithOutput(MealyMachine<?, I, ?, O> in,
                                                                        Alphabet<I> inputs,
                                                                        O... outputs) {
        return retainTransitionsWithOutput(in, inputs, Arrays.asList(outputs));
    }

    /**
     * Returns a Mealy machine with all transitions removed that have an output not among the specified values. The
     * resulting Mealy machine will not contain any unreachable states.
     *
     * @param in
     *         the input Mealy machine
     * @param inputs
     *         the input alphabet
     * @param outputs
     *         the outputs to retain
     *
     * @return a Mealy machine with all transitions retained that have one of the specified outputs.
     */
    public static <I, O> CompactMealy<I, O> retainTransitionsWithOutput(MealyMachine<?, I, ?, O> in,
                                                                        Alphabet<I> inputs,
                                                                        Collection<? super O> outputs) {
        return filterByOutput(in, inputs, outputs::contains);
    }
}
