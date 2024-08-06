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
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

final class DFSTraversalVisitor<N, E, D> implements GraphTraversalVisitor<N, E, DFSData<D>> {

    private final DFSVisitor<? super N, ? super E, D> visitor;
    private final MutableMapping<N, @Nullable DFSData<D>> records;
    private int dfsNum;

    DFSTraversalVisitor(IndefiniteGraph<N, E> graph, DFSVisitor<? super N, ? super E, D> visitor) {
        this.visitor = visitor;
        this.records = graph.createStaticNodeMapping();
    }

    @Override
    public GraphTraversalAction processInitial(N initialNode, Holder<DFSData<D>> outData) {
        D data = visitor.initialize(initialNode);
        DFSData<D> rec = new DFSData<>(data, dfsNum++);
        records.put(initialNode, rec);

        outData.value = rec;
        return GraphTraversalAction.EXPLORE;
    }

    @Override
    public boolean startExploration(N node, DFSData<D> data) {
        visitor.explore(node, data.data);
        return true;
    }

    @Override
    public void finishExploration(N node, DFSData<D> data) {
        visitor.finish(node, data.data);
        data.finished = true;
    }

    @Override
    public GraphTraversalAction processEdge(N srcNode,
                                            DFSData<D> srcData,
                                            E edge,
                                            N tgtNode,
                                            Holder<DFSData<D>> outData) {
        DFSData<D> tgtRec = records.get(tgtNode);
        if (tgtRec == null) {
            D data = visitor.treeEdge(srcNode, srcData.data, edge, tgtNode);
            tgtRec = new DFSData<>(data, dfsNum++);
            records.put(tgtNode, tgtRec);

            outData.value = tgtRec;
            return GraphTraversalAction.EXPLORE;
        }
        if (!tgtRec.finished) {
            visitor.backEdge(srcNode, srcData.data, edge, tgtNode, tgtRec.data);
        } else if (tgtRec.dfsNumber > srcData.dfsNumber) {
            visitor.forwardEdge(srcNode, srcData.data, edge, tgtNode, tgtRec.data);
        } else {
            visitor.crossEdge(srcNode, srcData.data, edge, tgtNode, tgtRec.data);
        }

        return GraphTraversalAction.IGNORE;
    }

    @Override
    public void backtrackEdge(N srcNode, DFSData<D> srcData, E edge, N tgtNode, DFSData<D> tgtData) {
        visitor.backtrackEdge(srcNode, srcData.data, edge, tgtNode, tgtData.data);
    }

}
