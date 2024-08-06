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

/**
 * Interface for bidirectional graph. A bidirectional graph is conceptually the same as a normal (directed) graph, but
 * provides direct access to not only the outgoing, but also the incoming edges of each state.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 *
 * @author Malte Isberner
 */
public interface BidirectionalGraph<N, E> extends Graph<N, E> {

    /**
     * Retrieves the incoming edges of a given node.
     *
     * @param node
     *         the node
     *
     * @return all incoming edges of the specified node.
     */
    Collection<E> getIncomingEdges(N node);

    /**
     * Retrieves the source node of a given edge.
     *
     * @param edge
     *         the edge
     *
     * @return the source node of the given edge
     */
    N getSource(E edge);

    /**
     * Interface for {@link Graph.IntAbstraction node integer abstractions} of a {@link BidirectionalGraph}.
     *
     * @param <E>
     *         edge type
     */
    interface IntAbstraction<E> extends Graph.IntAbstraction<E> {

        Collection<E> getIncomingEdges(int node);

        int getIntSource(E edge);

    }
}
