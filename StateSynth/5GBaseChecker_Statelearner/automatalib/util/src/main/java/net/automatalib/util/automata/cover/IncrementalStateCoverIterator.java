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
package net.automatalib.util.automata.cover;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

import com.google.common.collect.AbstractIterator;
import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An iterator for the state cover of an automaton. Words are computed lazily (i.e. only when request by {@link
 * #next()}.
 * <p>
 * Supports incremental computation, i.e. given a set of cover traces, only sequences for states not covered by these
 * traces are returned.
 *
 * @param <S>
 *         automaton state type
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 * @see Covers#stateCover(DeterministicAutomaton, Collection, Collection)
 */
class IncrementalStateCoverIterator<S, I> extends AbstractIterator<Word<I>> {

    private final DeterministicAutomaton<S, I, ?> automaton;
    private final Collection<? extends I> inputs;
    private final Collection<? extends Word<I>> oldCover;

    private final MutableMapping<S, @Nullable Record<S, I>> reach;
    private final Queue<Record<S, I>> bfsQueue;

    private Iterator<? extends I> inputIterator;
    private Record<S, I> curr;

    IncrementalStateCoverIterator(DeterministicAutomaton<S, I, ?> automaton,
                                  Collection<? extends I> inputs,
                                  Collection<? extends Word<I>> oldCover) {
        this.automaton = automaton;
        this.inputs = inputs;
        this.oldCover = oldCover;
        this.reach = automaton.createStaticStateMapping();
        this.bfsQueue = new ArrayDeque<>();
    }

    @Override
    protected Word<I> computeNext() {
        // first invocation
        if (inputIterator == null) {
            boolean hasEpsilon = initialize();

            curr = bfsQueue.poll();
            inputIterator = inputs.iterator();

            if (!hasEpsilon) {
                return Word.epsilon();
            }
        }

        while (curr != null) {
            S state = curr.state;
            Word<I> as = curr.accessSequence;

            while (inputIterator.hasNext()) {
                final I in = inputIterator.next();
                final S succ = automaton.getSuccessor(state, in);

                if (succ == null) {
                    continue;
                }

                if (reach.get(succ) == null) {
                    Word<I> succAs = as.append(in);
                    Record<S, I> succRec = new Record<>(succ, succAs);
                    reach.put(succ, succRec);
                    bfsQueue.add(succRec);
                    return succAs;
                }
            }

            curr = bfsQueue.poll();
            inputIterator = inputs.iterator();
        }

        return endOfData();
    }

    private boolean initialize() {

        final S init = automaton.getInitialState();

        if (init == null) {
            // abuse the fact that endOfData() mutates our state, so we effectively construct the empty iterator here.
            endOfData();
            return false;
        }

        Covers.buildReachFromStateCover(reach, bfsQueue, automaton, oldCover, Record::new);

        if (reach.get(init) == null) {
            // apparently the initial state was not yet covered
            Record<S, I> rec = new Record<>(init, Word.epsilon());
            reach.put(init, rec);
            bfsQueue.add(rec);

            return false;
        }

        return true;
    }
}
