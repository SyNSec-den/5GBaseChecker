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
package net.automatalib.util.graphs.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.traversal.VisitedState;
import org.checkerframework.checker.nullness.qual.Nullable;

final class BreadthFirstIterator<N, E> implements Iterator<N> {

    private final Queue<N> bfsQueue = new ArrayDeque<>();
    private final MutableMapping<N, @Nullable VisitedState> visited;
    private final IndefiniteGraph<N, E> graph;

    BreadthFirstIterator(IndefiniteGraph<N, E> graph, Collection<? extends N> start) {
        this.graph = graph;
        this.visited = graph.createStaticNodeMapping();
        bfsQueue.addAll(start);
    }

    @Override
    public boolean hasNext() {
        return !bfsQueue.isEmpty();
    }

    @Override
    public N next() {
        N result = bfsQueue.poll();
        if (result == null) {
            throw new NoSuchElementException();
        }

        for (E edge : graph.getOutgoingEdges(result)) {
            N tgt = graph.getTarget(edge);
            if (visited.put(tgt, VisitedState.VISITED) != VisitedState.VISITED) {
                bfsQueue.add(tgt);
            }
        }

        return result;
    }

}
