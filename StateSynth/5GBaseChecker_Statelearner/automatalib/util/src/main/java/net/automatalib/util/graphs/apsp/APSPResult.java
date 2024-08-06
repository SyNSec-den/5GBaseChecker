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
package net.automatalib.util.graphs.apsp;

import java.util.List;

import net.automatalib.util.graphs.Graphs;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Result interface for the all pairs shortest paths problem.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 *
 * @author Malte Isberner
 */
public interface APSPResult<N, E> {

    /**
     * Retrieves the length of the shortest path between the given nodes.
     *
     * @param src
     *         the source node
     * @param tgt
     *         the target node
     *
     * @return the length of the shortest path from {@code src} to {@code tgt}, or {@link Graphs#INVALID_DISTANCE} if
     * there exists no such path.
     */
    float getShortestPathDistance(N src, N tgt);

    /**
     * Retrieves the shortest path between the given nodes, or {@code null} if there exists no such path.
     *
     * @param src
     *         the source node
     * @param tgt
     *         the target node
     *
     * @return the shortest path from {@code src} to {@code tgt}, or {@code null} if there exists no such path.
     */
    @Nullable List<E> getShortestPath(N src, N tgt);
}
