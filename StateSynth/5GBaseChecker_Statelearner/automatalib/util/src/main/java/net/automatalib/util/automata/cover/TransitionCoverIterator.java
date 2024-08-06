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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.commons.util.collections.AbstractTwoLevelIterator;
import net.automatalib.words.Word;

/**
 * An iterator for the transition cover of an automaton. Words are computed lazily (i.e. only when request by {@link
 * #next()}.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 * @see Covers#transitionCover(DeterministicAutomaton, Collection, Collection)
 */
class TransitionCoverIterator<I> extends AbstractTwoLevelIterator<Word<I>, I, Word<I>> {

    private final Collection<I> inputs;

    @SuppressWarnings("unchecked")
    TransitionCoverIterator(DeterministicAutomaton<?, I, ?> automaton, Collection<? extends I> inputs) {
        super(new IncrementalStateCoverIterator<>(automaton, inputs, Collections.emptyList()));
        this.inputs = (Collection<I>) inputs;
    }

    @Override
    protected Iterator<I> l2Iterator(Word<I> l1Object) {
        return inputs.iterator();
    }

    @Override
    protected Word<I> combine(Word<I> l1Object, I l2Object) {
        return l1Object.append(l2Object);
    }
}
