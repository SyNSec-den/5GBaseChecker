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

import net.automatalib.ts.UniversalTransitionSystem;

/**
 * A universal graph, i.e., with (possibly empty) node and edge properties. For a documentation on the concept of
 * "universal", see {@link UniversalTransitionSystem}.
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
public interface UniversalIndefiniteGraph<N, E, NP, EP> extends IndefiniteGraph<N, E> {

    /**
     * Retrieves the property of a given node.
     *
     * @param node
     *         the node
     *
     * @return the property of the specified node
     */
    NP getNodeProperty(N node);

    /**
     * Retrieves the property of a given edge.
     *
     * @param edge
     *         the edge
     *
     * @return the property of the specified edge
     */
    EP getEdgeProperty(E edge);
}
