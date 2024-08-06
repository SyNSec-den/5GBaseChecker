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
package net.automatalib.util.graphs.sssp;

import java.util.List;

import net.automatalib.util.graphs.Graphs;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Result interface for the single-source shortest path (SSSP) problem.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 *
 * @author Malte Isberner
 */
public interface SSSPResult<N, E> {

    /**
     * Retrieves the node the source was started from.
     *
     * @return the source node
     */
    N getInitialNode();

    /**
     * Retrieves the length of the shortest path from the initial node to the given one.
     *
     * @param target
     *         the target node
     *
     * @return the length of the shortest path from the initial node to the given target node, or {@link
     * Graphs#INVALID_DISTANCE} if there exists no such path.
     */
    float getShortestPathDistance(N target);

    /**
     * Retrieves the shortest path from the initial node to the given one (as a sequence of edges), or {@code null} if
     * there exists no such path.
     * <p>
     * Note that implementations might construct these paths on-the-fly.
     *
     * @param target
     *         the target node
     *
     * @return the path from the initial node to the given target node, or {@code null} if there exists no such path.
     */
    @Nullable List<E> getShortestPath(N target);

    /**
     * Retrieves the incoming edge via which the given node is reached on the shortest path. If the node is not
     * reachable or it is the initial node, {@code null} is returned.
     *
     * @param target
     *         the target node
     *
     * @return the reaching edge on the shortest path, or {@code null}.
     */
    @Nullable E getShortestPathEdge(N target);
}
