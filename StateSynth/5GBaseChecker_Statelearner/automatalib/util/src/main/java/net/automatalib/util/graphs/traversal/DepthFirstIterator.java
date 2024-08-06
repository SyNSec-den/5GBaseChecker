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
import java.util.Deque;

import com.google.common.collect.AbstractIterator;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.traversal.VisitedState;
import org.checkerframework.checker.nullness.qual.Nullable;

final class DepthFirstIterator<N, E> extends AbstractIterator<N> {

    private final MutableMapping<N, @Nullable VisitedState> visited;
    private final Deque<SimpleDFRecord<N, E>> dfsStack = new ArrayDeque<>();
    private final IndefiniteGraph<N, E> graph;

    DepthFirstIterator(IndefiniteGraph<N, E> graph, Collection<? extends N> start) {
        this.graph = graph;
        this.visited = graph.createStaticNodeMapping();
        for (N startNode : start) {
            dfsStack.push(new SimpleDFRecord<>(startNode));
        }
    }

    @Override
    protected N computeNext() {
        SimpleDFRecord<N, E> rec;
        while ((rec = dfsStack.peek()) != null) {
            if (!rec.wasStarted()) {
                visited.put(rec.node, VisitedState.VISITED);
                rec.start(graph);
                return rec.node;
            } else if (rec.hasNextEdge()) {
                E edge = rec.nextEdge();
                N tgt = graph.getTarget(edge);
                if (visited.get(tgt) != VisitedState.VISITED) {
                    dfsStack.push(new SimpleDFRecord<>(tgt));
                }
            } else {
                dfsStack.pop();
            }
        }
        return endOfData();
    }

}
