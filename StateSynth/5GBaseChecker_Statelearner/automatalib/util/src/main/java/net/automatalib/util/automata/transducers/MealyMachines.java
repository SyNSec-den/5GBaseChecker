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

import java.util.Collection;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.ts.copy.TSCopy;
import net.automatalib.util.ts.transducers.Transducers;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.util.ts.traversal.TSTraversalMethod;
import net.automatalib.words.Alphabet;

public final class MealyMachines {

    private MealyMachines() {}

    /**
     * Constructs the product automaton for two Mealy Machines.
     *
     * @param mealy1
     *         the first Mealy Machine
     * @param mealy2
     *         the second Mealy Machine
     * @param inputAlphabet
     *         the input alphabet of the returned automaton
     * @param <I>
     *         input symbol type
     * @param <O1>
     *         output symbol type of the first Mealy Machine
     * @param <O2>
     *         output symbol type of the second Mealy Machine
     *
     * @return a new Mealy Machine representing the product automaton of the specified Mealy Machines
     */
    public static <I, O1, O2> CompactMealy<I, Pair<O1, O2>> combine(MealyMachine<?, I, ?, O1> mealy1,
                                                                    MealyMachine<?, I, ?, O2> mealy2,
                                                                    Alphabet<I> inputAlphabet) {
        return combine(mealy1, mealy2, inputAlphabet, new CompactMealy<>(inputAlphabet));
    }

    /**
     * Constructs the product automaton for two Mealy Machines.
     *
     * @param mealy1
     *         the first Mealy Machine
     * @param mealy2
     *         the second Mealy Machine
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         the instance to which the combined automaton should be written
     * @param <I>
     *         input symbol type
     * @param <O1>
     *         output symbol type of the first Mealy Machine
     * @param <O2>
     *         output symbol type of the second Mealy Machine
     *
     * @return {@code out}, for convenience
     */
    public static <I, O1, O2, A extends MutableMealyMachine<?, I, ?, Pair<O1, O2>>> A combine(MealyMachine<?, I, ?, O1> mealy1,
                                                                                              MealyMachine<?, I, ?, O2> mealy2,
                                                                                              Collection<? extends I> inputs,
                                                                                              A out) {
        final MealyTransitionSystem<?, I, ?, Pair<O1, O2>> comb = Transducers.combine(mealy1, mealy2);

        TSCopy.copy(TSTraversalMethod.BREADTH_FIRST,
                    comb,
                    TSTraversal.NO_LIMIT,
                    inputs,
                    (MutableMealyMachine<?, I, ?, Pair<O1, O2>>) out);
        return out;
    }

    /**
     * Constructs a copy of the given Mealy machine in which every transition (with regards to the specified alphabet)
     * is guaranteed to be defined. This includes adding an additional sink state if the original Mealy machine has
     * undefined transitions.
     *
     * @param mealy
     *         the original Mealy machine
     * @param inputs
     *         the inputs to consider for completing the automaton
     * @param undefinedOutput
     *         the output symbol that should be used for new transitions
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a copy of the given Mealy machine in which every transition (with regards to the specified alphabet) is
     * guaranteed to be defined
     */
    public static <I, O> CompactMealy<I, O> complete(MealyMachine<?, I, ?, O> mealy,
                                                     Alphabet<I> inputs,
                                                     O undefinedOutput) {
        return complete(mealy, inputs, undefinedOutput, new CompactMealy<>(inputs));
    }

    /**
     * Constructs a copy of the given Mealy machine in which every transition (with regards to the specified alphabet)
     * is guaranteed to be defined. This includes adding an additional sink state if the original Mealy machine has
     * undefined transitions.
     *
     * @param mealy
     *         the original Mealy machine
     * @param inputs
     *         the inputs to consider for completing the automaton
     * @param undefinedOutput
     *         the output symbol that should be used for new transitions
     * @param out
     *         the instance to which the copy should be written
     * @param <S>
     *         automaton state type
     * @param <I>
     *         input symbol type
     * @param <T>
     *         automaton transition type
     * @param <O>
     *         output symbol type
     * @param <A>
     *         output automaton type
     *
     * @return {@code out}, for convenience.
     */
    public static <S, I, T, O, A extends MutableMealyMachine<S, I, T, O>> A complete(MealyMachine<?, I, ?, O> mealy,
                                                                                     Collection<? extends I> inputs,
                                                                                     O undefinedOutput,
                                                                                     A out) {
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, mealy, inputs, out);
        MutableMealyMachines.complete(out, inputs, undefinedOutput);
        return out;
    }
}
