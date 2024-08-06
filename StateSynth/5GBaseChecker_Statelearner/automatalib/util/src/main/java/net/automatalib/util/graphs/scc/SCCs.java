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
package net.automatalib.util.graphs.scc;

import java.util.List;

import net.automatalib.graphs.Graph;
import net.automatalib.util.graphs.traversal.GraphTraversal;

/**
 * Algorithms for finding strongly-connected components (SCCs) in a graph.
 *
 * @author Malte Isberner
 */
public final class SCCs {

    private SCCs() {}

    /**
     * Collects all strongly-connected components in a graph. The SCCs are returned as a list of lists.
     * <p>
     * Tarjan's algorithm is used for realizing the SCC search.
     *
     * @param graph
     *         the graph
     *
     * @return a list of all SCCs, each represented as a list of its nodes
     *
     * @see TarjanSCCVisitor
     */
    public static <N, E> List<List<N>> collectSCCs(Graph<N, E> graph) {
        SCCCollector<N> coll = new SCCCollector<>();
        findSCCs(graph, coll);
        return coll.getSCCList();
    }

    /**
     * Find all strongly-connected components in a graph. When a new SCC is found, the {@link
     * SCCListener#foundSCC(java.util.Collection)} method is invoked. The listener object may hence not be null.
     * <p>
     * Tarjan's algorithm is used for realizing the SCC search.
     *
     * @param graph
     *         the graph
     * @param listener
     *         the SCC listener
     *
     * @see TarjanSCCVisitor
     */
    public static <N, E> void findSCCs(Graph<N, E> graph, SCCListener<N> listener) {
        TarjanSCCVisitor<N, E> vis = new TarjanSCCVisitor<>(graph, listener);
        for (N node : graph) {
            if (!vis.hasVisited(node)) {
                GraphTraversal.depthFirst(graph, node, vis);
            }
        }
    }

}
