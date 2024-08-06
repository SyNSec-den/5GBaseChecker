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

import net.automatalib.commons.util.Holder;

/**
 * Visitor interface for graph traversals.
 * <p>
 * This interface declares methods that are called upon basic graph traversal actions.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 * @param <D>
 *         user data class
 *
 * @author Malte Isberner
 */
public interface GraphTraversalVisitor<N, E, D> {

    /**
     * Called when a node is processed <i>initially</i>.
     *
     * @param initialNode
     *         the node that is processed
     *
     * @return the action to perform
     */
    GraphTraversalAction processInitial(N initialNode, Holder<D> outData);

    /**
     * Called when the exploration of a node is started.
     *
     * @param node
     *         the node whose exploration is about to be started
     * @param data
     *         the user data associated with this node
     *
     * @return the action to perform
     */
    boolean startExploration(N node, D data);

    /**
     * Called when the exploration of a node is finished.
     *
     * @param node
     *         the node whose exploration is being finished
     * @param inData
     *         the user data associated with this node
     */
    void finishExploration(N node, D inData);

    /**
     * Called when an edge is processed.
     *
     * @param srcNode
     *         the source node
     * @param srcData
     *         the user data associated with the source node
     * @param edge
     *         the edge that is being processed
     *
     * @return the action to perform
     */
    GraphTraversalAction processEdge(N srcNode, D srcData, E edge, N tgtNode, Holder<D> outData);

    void backtrackEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData);
}
