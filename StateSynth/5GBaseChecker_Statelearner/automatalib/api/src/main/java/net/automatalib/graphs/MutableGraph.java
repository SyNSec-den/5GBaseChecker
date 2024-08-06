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
 * A graph that allows modification. Note that this interface only exposes methods for extending a graph. If also
 * destructive modifications should be performed, {@link ShrinkableGraph} is the adequate interface.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 * @param <NP>
 *         node property class
 * @param <EP>
 *         edge property class
 *
 * @author Malte Isberner
 */
public interface MutableGraph<N, E, NP, EP> extends UniversalGraph<N, E, NP, EP> {

    /**
     * Adds a new node with default properties to the graph. This method behaves equivalently to the below {@link
     * #addNode(Object)} with a <code>null</code> parameter.
     *
     * @return the newly inserted node
     */
    default N addNode() {
        return addNode(null);
    }

    /**
     * Adds a new node to the graph.
     *
     * @param property
     *         the property for the new node
     *
     * @return the newly inserted node
     */
    N addNode(@Nullable NP property);

    /**
     * Inserts an edge in the graph, with the default property. Calling this method should be equivalent to invoking
     * {@link #connect(Object, Object, Object)} with a {@code null} property value.
     *
     * @param source
     *         the source node
     * @param target
     *         the target node
     *
     * @return the edge connecting the given nodes
     */
    default E connect(N source, N target) {
        return connect(source, target, null);
    }

    /**
     * Inserts an edge in the graph.
     *
     * @param source
     *         the source node of the edge
     * @param target
     *         the target node of the edge
     * @param property
     *         the property of the edge
     *
     * @return the newly inserted edge
     */
    E connect(N source, N target, @Nullable EP property);

    void setNodeProperty(N node, NP property);

    void setEdgeProperty(E edge, EP property);

    /**
     * Interface for {@link UniversalGraph.IntAbstraction node integer abstractions} of a {@link MutableGraph}.
     *
     * @param <E>
     *         edge type
     * @param <NP>
     *         node property type
     * @param <EP>
     *         edge property type
     */
    interface IntAbstraction<E, NP, EP> extends UniversalGraph.IntAbstraction<E, NP, EP> {

        default int addIntNode() {
            return addIntNode(null);
        }

        int addIntNode(@Nullable NP property);

        default E connect(int source, int target) {
            return connect(source, target, null);
        }

        E connect(int source, int target, @Nullable EP property);

        void setNodeProperty(int node, @Nullable NP property);

        void setEdgeProperty(E edge, EP property);
    }
}
