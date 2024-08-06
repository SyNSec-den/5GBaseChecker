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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A base implementation of a {@link DFSVisitor}.
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
public class BaseDFSVisitor<N, E, @Nullable D> implements DFSVisitor<N, E, D> {

    @Override
    public D initialize(N node) {
        return null;
    }

    @Override
    public void explore(N node, D data) {}

    @Override
    public void finish(N node, D data) {}

    @Override
    public D treeEdge(N srcNode, D srcData, E edge, N tgtNode) {
        edge(srcNode, srcData, edge, tgtNode);
        return null;
    }

    /**
     * Most general edge handler. In their default implementations, the following methods resort to calling this method:
     * <ul> <li>{@link #treeEdge(Object, Object, Object, Object)} <li>{@link #nontreeEdge(Object, Object, Object,
     * Object, Object)} </ul> Provided that the latter is not overwritten, the following methods resort to this method
     * indirectly in their default implementation: <ul> <li>{@link #grayTarget(Object, Object, Object, Object, Object)}
     * <li>{@link #blackTarget(Object, Object, Object, Object, Object)} </ul>
     *
     * @param srcNode
     *         the source node
     * @param srcData
     *         the data associated with the source node
     * @param edge
     *         the edge that is being processed
     * @param tgtNode
     *         the target node of this edge
     */
    public void edge(N srcNode, D srcData, E edge, N tgtNode) {}

    @Override
    public void backEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
        grayTarget(srcNode, srcData, edge, tgtNode, tgtData);
    }

    public void grayTarget(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
        nontreeEdge(srcNode, srcData, edge, tgtNode, tgtData);
    }

    public void nontreeEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
        edge(srcNode, srcData, edge, tgtNode);
    }

    @Override
    public void crossEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
        blackTarget(srcNode, srcData, edge, tgtNode, tgtData);
    }

    public void blackTarget(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
        nontreeEdge(srcNode, srcData, edge, tgtNode, tgtData);
    }

    @Override
    public void forwardEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
        blackTarget(srcNode, srcData, edge, tgtNode, tgtData);
    }

    @Override
    public void backtrackEdge(N srcNode, D srcDate, E edge, N tgtNode, D tgtData) {}
}
