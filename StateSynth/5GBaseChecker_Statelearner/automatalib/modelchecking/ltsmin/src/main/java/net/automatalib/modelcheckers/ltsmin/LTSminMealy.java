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
package net.automatalib.modelcheckers.ltsmin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.serialization.fsm.parser.FSMFormatException;
import net.automatalib.util.automata.transducers.MealyFilter;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * A feature of this {@link net.automatalib.modelchecking.ModelChecker}, is that one can remove particular output
 * symbols from the a given MealyMachine hypothesis. This is useful when those symbols are actually symbols representing
 * system deadlocks. When checking LTL formulae special attention has to be given to deadlock situations.
 *
 * @param <I> the input type
 * @param <O> the input type
 * @param <R> the type of a counterexample
 *
 * @author Jeroen Meijer
 */
public interface LTSminMealy<I, O, R>
        extends ModelChecker.MealyModelChecker<I, O, String, R>, LTSmin<I, MealyMachine<?, I, ?, O>, R> {

    /**
     * Converts the given {@code fsm} to a {@link CompactMealy}.
     *
     * @param fsm
     *         the FSM to convert.
     * @param originalAutomaton
     *         the original automaton on which the property is checked.
     * @param inputs
     *         the alphabet for the returned automaton.
     *
     * @return the {@link CompactMealy}.
     *
     * @throws IOException
     *         when {@code fsm} can not be read.
     * @throws FSMFormatException
     *         when {@code fsm} is invalid.
     */
    CompactMealy<I, O> fsm2Mealy(File fsm, MealyMachine<?, I, ?, O> originalAutomaton, Collection<? extends I> inputs)
            throws IOException;

    /**
     * Writes the given {@link MealyMachine} to the {@code etf} file.
     *
     * @param automaton
     *         the {@link MealyMachine} to write.
     * @param inputs
     *         the alphabet.
     * @param etf
     *         the file to write to.
     *
     * @throws IOException
     *         when {@code etf} can not be read.
     */
    void mealy2ETF(MealyMachine<?, I, ?, O> automaton, Collection<? extends I> inputs, File etf) throws IOException;

    /**
     * Writes the {@link MealyMachine} to the {@code etf} file while pruning way the outputs given in {@link
     * #getSkipOutputs()}.
     *
     * @param mealyMachine
     *         the {@link MealyMachine} to write.
     *
     * @throws IOException
     *         see {@link #mealy2ETF(MealyMachine, Collection, File)}.
     */
    @Override
    default void automaton2ETF(MealyMachine<?, I, ?, O> mealyMachine, Collection<? extends I> inputs, File etf)
            throws IOException {
        final Alphabet<I> alphabet = Alphabets.fromCollection(inputs);
        mealy2ETF(MealyFilter.pruneTransitionsWithOutput(mealyMachine, alphabet, getSkipOutputs()), inputs, etf);
    }

    /**
     * Gets a function that transforms edges in the FSM file to actual output.
     *
     * @return the Function.
     */
    Function<String, O> getString2Output();
}
