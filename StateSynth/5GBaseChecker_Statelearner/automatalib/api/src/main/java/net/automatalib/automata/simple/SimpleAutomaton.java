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
package net.automatalib.automata.simple;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.concepts.FiniteRepresentation;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.helpers.SimpleStateIDs;
import net.automatalib.automata.helpers.StateIDGrowingMapping;
import net.automatalib.automata.helpers.StateIDStaticMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.simple.SimpleTS;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simple automaton, i.e., a {@link SimpleTS} with a finite number of states.
 *
 * @param <S>
 *         state class.
 * @param <I>
 *         input symbol class.
 *
 * @author Malte Isberner
 */
public interface SimpleAutomaton<S, I> extends SimpleTS<S, I>, Iterable<S>, FiniteRepresentation {

    @Override
    default Iterator<S> iterator() {
        return getStates().iterator();
    }

    /**
     * Retrieves all states of the transition system. Implementing classes should return an unmodifiable collection
     *
     * @return all states in the transition system
     */
    Collection<S> getStates();

    @Override
    default <@Nullable V> MutableMapping<S, V> createStaticStateMapping() {
        return new StateIDStaticMapping<>(stateIDs(), size());
    }

    @Override
    default <@Nullable V> MutableMapping<S, V> createDynamicStateMapping() {
        return new StateIDGrowingMapping<>(stateIDs(), size());
    }

    default StateIDs<S> stateIDs() {
        return new SimpleStateIDs<>(this);
    }

    /**
     * Retrieves the size (number of states) of this transition system.
     *
     * @return the number of states of this transition system
     */
    @Override
    default int size() {
        return getStates().size();
    }
}
