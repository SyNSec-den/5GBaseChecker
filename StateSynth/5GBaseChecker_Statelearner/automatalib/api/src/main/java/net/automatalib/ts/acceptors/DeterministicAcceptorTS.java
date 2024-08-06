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
package net.automatalib.ts.acceptors;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.Iterables;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.UniversalDTS;

/**
 * A deterministic acceptor transition system.
 *
 * @author Malte Isberner
 * @see AcceptorTS
 * @see DeterministicTransitionSystem
 */
public interface DeterministicAcceptorTS<S, I>
        extends AcceptorTS<S, I>, UniversalDTS<S, I, S, Boolean, Void>, SuffixOutput<I, Boolean> {

    @Override
    default Boolean computeOutput(Iterable<? extends I> input) {
        return accepts(input);
    }

    @Override
    default Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
        return computeOutput(Iterables.concat(prefix, suffix));
    }

    @Override
    default boolean accepts(Iterable<? extends I> input) {
        S state = getState(input);
        return state != null && isAccepting(state);
    }

    @Override
    default boolean isAccepting(Collection<? extends S> states) {
        if (states.isEmpty()) {
            return false;
        }
        Iterator<? extends S> stateIt = states.iterator();
        assert stateIt.hasNext();

        // 'firstState' should be cached here
        @SuppressWarnings("PMD.PrematureDeclaration")
        S firstState = stateIt.next();
        if (stateIt.hasNext()) {
            throw new IllegalArgumentException("Acceptance of state sets is undefined for DFAs");
        }
        return isAccepting(firstState);
    }
}
