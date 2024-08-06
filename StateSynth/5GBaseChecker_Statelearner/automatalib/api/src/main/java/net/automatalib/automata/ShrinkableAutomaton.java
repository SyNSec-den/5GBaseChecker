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
package net.automatalib.automata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A mutable automaton that also supports destructive modifications, i.e., removal of states and transitions.
 *
 * @param <S>
 *         state class
 * @param <I>
 *         input symbol class
 * @param <T>
 *         transition class
 * @param <SP>
 *         state property class
 * @param <TP>
 *         transition property class
 *
 * @author Malte Isberner
 */
public interface ShrinkableAutomaton<S, I, T, SP, TP> extends MutableAutomaton<S, I, T, SP, TP> {

    static <S, I, T, SP, TP> void unlinkState(MutableAutomaton<S, I, T, SP, TP> automaton,
                                              S state,
                                              @Nullable S replacement,
                                              Collection<I> inputs) {

        for (S curr : automaton) {
            if (Objects.equals(state, curr)) {
                continue;
            }

            for (I input : inputs) {
                Collection<T> transitions = automaton.getTransitions(curr, input);
                if (transitions.isEmpty()) {
                    continue;
                }

                boolean modified = false;
                List<T> modTransitions = new ArrayList<>(transitions);

                ListIterator<T> it = modTransitions.listIterator();
                while (it.hasNext()) {
                    T trans = it.next();
                    if (automaton.getSuccessor(trans) == state) {
                        if (replacement == null) {
                            it.remove();
                        } else {
                            T transRep = automaton.copyTransition(trans, replacement);
                            it.set(transRep);
                        }
                        modified = true;
                    }
                }

                if (modified) {
                    automaton.setTransitions(curr, input, modTransitions);
                }
            }
        }
    }

    /**
     * removes a state from the automaton.
     *
     * @param state
     *         state to be removed
     */
    default void removeState(S state) {
        removeState(state, null);
    }

    //FIXME: should this be replaceState?

    /**
     * Removes a state from the automaton. All ingoing transitions to this state are redirected to the given replacement
     * state. If a <code>null</code> replacement is given, then this method behaves like the above {@link
     * #removeState(Object)}.
     *
     * @param state
     *         the state to remove
     * @param replacement
     *         the replacement state, or <code>null</code>
     */
    void removeState(S state, @Nullable S replacement);
}
