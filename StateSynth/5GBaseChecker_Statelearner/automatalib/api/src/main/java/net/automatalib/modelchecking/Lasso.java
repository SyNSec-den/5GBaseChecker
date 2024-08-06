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
package net.automatalib.modelchecking;

import java.util.SortedSet;

import net.automatalib.automata.concepts.DetOutputAutomaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;

/**
 * A lasso is an single infinite word.
 * <p>
 * The implementation is an automaton such that its singleton language is the infinite word. Also, the implementation is
 * actually the finite representation (by unrolling the loop) of the infinite word, including information how many times
 * the loop of the lasso is unrolled.
 *
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 *
 * @author Jeroen Meijer
 */
public interface Lasso<I, D> extends DetOutputAutomaton<Integer, I, Integer, D>, InputAlphabetHolder<I> {

    /**
     * Gets the finite representation of the lasso.
     *
     * @return the Word.
     */
    Word<I> getWord();

    /**
     * Gets the loop of the lasso.
     *
     * @return the Word.
     */
    Word<I> getLoop();

    /**
     * Gets the prefix of the lasso.
     *
     * @return the Word.
     */
    Word<I> getPrefix();

    /**
     * Gets the finite representation of the output of the lasso.
     *
     * @return the output type D.
     */
    D getOutput();

    /**
     * The sorted set containing some symbol indices after which the begin state of the loop is visited.
     */
    SortedSet<Integer> getLoopBeginIndices();

    /**
     * Returns the number of times the loop is unfolded.
     *
     * The returned value is always greater than 0.
     *
     * @return the number of times the loop is unfolded.
     */
    int getUnfolds();

    /**
     * Returns the original automaton from which this lasso is constructed.
     *
     * @return the original automaton.
     */
    DetOutputAutomaton<?, I, ?, D> getAutomaton();

    /**
     * A DFALasso is a lasso for {@link DFA}s.
     *
     * @param <I>
     *         the input type
     */
    interface DFALasso<I> extends Lasso<I, Boolean>, DFA<Integer, I> {}

    /**
     * A MealyLasso is a lasso for {@link MealyMachine}s.
     *
     * @param <I>
     *         the input type
     * @param <O>
     *         the output type
     */
    interface MealyLasso<I, O> extends Lasso<I, Word<O>>, MealyMachine<Integer, I, Integer, O> {}
}
