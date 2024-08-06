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
package net.automatalib.incremental;

import java.util.Collection;

import net.automatalib.graphs.Graph;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Basic interface for incremental automata constructions. An incremental automaton construction creates an (acyclic)
 * automaton by iterated insertion of example words.
 *
 * @param <A>
 *         the automaton model which is constructed
 * @param <I>
 *         input symbol class
 *
 * @author Malte Isberner
 */
public interface IncrementalConstruction<A, I> {

    /**
     * Checks the current state of the construction against a given target model, and returns a word exposing a
     * difference if there is one.
     *
     * @param target
     *         the target automaton model
     * @param inputs
     *         the set of input symbols to consider
     * @param omitUndefined
     *         if this is set to {@code true}, then undefined transitions in the {@code target} model will be
     *         interpreted as "unspecified/don't know" and omitted in the equivalence test. Otherwise, they will be
     *         interpreted in the usual manner (e.g., non-accepting sink in case of DFAs).
     *
     * @return a separating word, or {@code null} if no difference could be found.
     */
    @Nullable Word<I> findSeparatingWord(A target, Collection<? extends I> inputs, boolean omitUndefined);

    /**
     * Checks whether this class has definitive information about a given word.
     *
     * @param word
     *         the word
     *
     * @return {@code true} if this class has definitive information about the word, {@code false} otherwise.
     */
    boolean hasDefinitiveInformation(Word<? extends I> word);

    /**
     * Retrieves a <i>graph view</i> of the current state of the construction. The graph model should be backed by the
     * construction, i.e., subsequent changes will be reflected in the graph model.
     *
     * @return a graph view on the current state of the construction
     */
    Graph<?, ?> asGraph();

    /**
     * Retrieves a <i>transition system view</i> of the current state of the construction. The transition system model
     * should be backed by the construction, i.e., subsequent changes will be reflected in the transition system.
     *
     * @return a transition system view on the current state of the construction
     */
    DeterministicTransitionSystem<?, I, ?> asTransitionSystem();
}
