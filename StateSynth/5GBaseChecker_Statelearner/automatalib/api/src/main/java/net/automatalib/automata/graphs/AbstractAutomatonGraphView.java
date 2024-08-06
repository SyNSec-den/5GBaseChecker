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
package net.automatalib.automata.graphs;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.Automaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.NodeIDs;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractAutomatonGraphView<S, A extends Automaton<S, ?, ?>, E> implements Graph<S, E> {

    protected final A automaton;

    public AbstractAutomatonGraphView(A automaton) {
        this.automaton = automaton;
    }

    @Override
    public <@Nullable V> MutableMapping<S, V> createStaticNodeMapping() {
        return automaton.createStaticStateMapping();
    }

    @Override
    public <@Nullable V> MutableMapping<S, V> createDynamicNodeMapping() {
        return automaton.createDynamicStateMapping();
    }

    @Override
    public int size() {
        return automaton.size();
    }

    @Override
    public Collection<S> getNodes() {
        return automaton.getStates();
    }

    @Override
    public Iterator<S> iterator() {
        return automaton.iterator();
    }

    @Override
    public NodeIDs<S> nodeIDs() {
        return new StateAsNodeIDs<>(automaton.stateIDs());
    }

}
