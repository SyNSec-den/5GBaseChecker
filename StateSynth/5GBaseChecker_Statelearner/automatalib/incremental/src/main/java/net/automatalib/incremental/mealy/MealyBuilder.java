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
package net.automatalib.incremental.mealy;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.graphs.Graph;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * A utility interface to share functionality between {@link IncrementalMealyBuilder}s and
 * {@link AdaptiveMealyBuilder}s.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public interface MealyBuilder<I, O>
        extends IncrementalConstruction<MealyMachine<?, I, ?, O>, I>, SupportsGrowingAlphabet<I> {

    /**
     * Retrieves the output word for the given input word. If no definitive information for the input word exists, the
     * output for the longest known prefix will be returned.
     *
     * @param inputWord
     *         the input word
     * @param output
     *         a consumer for constructing the output word
     *
     * @return {@code true} if the information contained was complete (in this case,
     * {@code word.length() == output.size()} will hold), {@code false} otherwise.
     */
    boolean lookup(Word<? extends I> inputWord, List<? super O> output);

    default Word<O> lookup(Word<? extends I> inputWord) {
        WordBuilder<O> wb = new WordBuilder<>(inputWord.size());
        lookup(inputWord, wb);
        return wb.toWord();
    }

    @Override
    default boolean hasDefinitiveInformation(Word<? extends I> word) {
        return lookup(word, new ArrayList<>(word.length()));
    }

    @Override
    GraphView<I, O, ?, ?> asGraph();

    @Override
    MealyTransitionSystem<?, I, ?, O> asTransitionSystem();

    interface GraphView<I, O, N, E> extends Graph<N, E> {

        I getInputSymbol(E edge);

        O getOutputSymbol(E edge);

        N getInitialNode();
    }
}
