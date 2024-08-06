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

public class DefaultGraphTraversalVisitor<N, E, D> implements GraphTraversalVisitor<N, E, D> {

    @Override
    public GraphTraversalAction processInitial(N initialNode, Holder<D> outData) {
        return GraphTraversalAction.EXPLORE;
    }

    @Override
    public boolean startExploration(N node, D data) {
        return true;
    }

    @Override
    public void finishExploration(N node, D data) {}

    @Override
    public GraphTraversalAction processEdge(N srcNode, D srcData, E edge, N tgtNode, Holder<D> outData) {
        return GraphTraversalAction.EXPLORE;
    }

    @Override
    public void backtrackEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {}

}
