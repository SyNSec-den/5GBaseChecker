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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A graph that supports (desirably efficient) removal of nodes and edges.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 *
 * @author Malte Isberner
 */
public interface ShrinkableGraph<N, E> extends Graph<N, E> {

    /**
     * Removes a node from this graph. All incoming and outgoing edges are removed as well.
     *
     * @param node
     *         the node to remove.
     */
    default void removeNode(N node) {
        removeNode(node, null);
    }

    /**
     * Removes a node from this graph, and redirects all incoming edges to the given replacement node (node that
     * outgoing edges are still removed). If a {@code null} replacement is specified, then this function behaves
     * equivalently to the above {@link #removeNode(Object)}.
     *
     * @param node
     *         the node to remove
     * @param replacement
     *         the replacement node for incoming edges
     */
    void removeNode(N node, @Nullable N replacement);

    /**
     * Removes an outgoing edge from the given node.
     *
     * @param node
     *         the node
     * @param edge
     *         the edge to remove
     */
    void removeEdge(N node, E edge);
}
