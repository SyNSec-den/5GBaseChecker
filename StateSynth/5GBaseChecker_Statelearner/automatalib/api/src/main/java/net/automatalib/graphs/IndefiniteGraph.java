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
package net.automatalib.graphs;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface for an (indefinite) graph structure. A graph consists of nodes, each of which has outgoing edges connecting
 * to other nodes. In an indefinite graph, the node set is not required to be finite.
 *
 * @param <N>
 *         node type
 * @param <E>
 *         edge type
 *
 * @author Malte Isberner
 */
public interface IndefiniteGraph<N, E> extends IndefiniteSimpleGraph<N> {

    @Override
    default Collection<N> getAdjacentTargets(N node) {
        return adjacentTargetsStream(node).collect(Collectors.toList());
    }

    @Override
    default Stream<N> adjacentTargetsStream(N node) {
        return outgoingEdgesStream(node).map(this::getTarget);
    }

    @Override
    default IndefiniteGraph<N, E> asNormalGraph() {
        return this;
    }

    /**
     * Retrieves the outgoing edges of a given node.
     *
     * @param node
     *         the node.
     *
     * @return a {@link Collection} of all outgoing edges, or <code>null</code> if the node has no outgoing edges.
     */
    Collection<E> getOutgoingEdges(N node);

    default Stream<E> outgoingEdgesStream(N node) {
        return getOutgoingEdges(node).stream();
    }

    default Collection<E> getEdgesBetween(N from, N to) {
        return getOutgoingEdges(from).stream()
                                     .filter(e -> Objects.equals(getTarget(e), to))
                                     .collect(Collectors.toList());
    }

    /**
     * Retrieves, for a given edge, its target node.
     *
     * @param edge
     *         the edge.
     *
     * @return the target node of the given edge.
     */
    N getTarget(E edge);

}
