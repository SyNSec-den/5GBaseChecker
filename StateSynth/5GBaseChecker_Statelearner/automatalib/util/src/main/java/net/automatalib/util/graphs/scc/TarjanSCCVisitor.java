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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.automatalib.commons.util.Holder;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.util.graphs.traversal.GraphTraversalAction;
import net.automatalib.util.graphs.traversal.GraphTraversalVisitor;

/**
 * Depth-first traversal visitor realizing Tarjan's algorithm for finding all strongly-connected components (SCCs) in a
 * graph.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 *
 * @author Malte Isberner
 */
public class TarjanSCCVisitor<N, E> implements GraphTraversalVisitor<N, E, TarjanSCCRecord> {

    private static final int SCC_FINISHED = -1;
    private final MutableMapping<N, TarjanSCCRecord> records;

    /**
     * The stack for currently investigated SCCs.
     * <p>
     * Note: Due to the nature of DFS traversal, we may encounter new SCCs before we have finished currently
     * investigated ones. As a result, when finishing an SCC, one has to check, to not pop too many records from this
     * stack.
     */
    private final List<TarjanSCCRecord> currentSccRecordStack = new ArrayList<>();

    /**
     * The node equivalent of {@link #currentSccRecordStack}. The same characteristics apply to this stack.
     */
    private final List<N> currentSccNodeStack = new ArrayList<>();
    private final SCCListener<N> listener;
    private int counter;

    /**
     * Constructor.
     *
     * @param graph
     *         the graph
     * @param listener
     *         the SCC listener to use, <b>must not be null</b>
     */
    public TarjanSCCVisitor(Graph<N, E> graph, SCCListener<N> listener) {
        records = graph.createStaticNodeMapping();
        this.listener = listener;
    }

    @Override
    public GraphTraversalAction processInitial(N initialNode, Holder<TarjanSCCRecord> outData) {
        outData.value = createRecord();
        return GraphTraversalAction.EXPLORE;
    }

    @Override
    public boolean startExploration(N node, TarjanSCCRecord data) {
        records.put(node, data);
        currentSccRecordStack.add(data);
        currentSccNodeStack.add(node);
        return true;
    }

    @Override
    public void finishExploration(N node, TarjanSCCRecord data) {
        // finished the initial node of this SCC
        if (data.sccId == data.number) {
            int numOfNodes = 0;
            final ListIterator<TarjanSCCRecord> iter = currentSccRecordStack.listIterator(currentSccRecordStack.size());

            final int nodeId = data.number;
            int prevId;
            do {
                final TarjanSCCRecord prev = iter.previous();
                prevId = prev.number;
                numOfNodes++;
                prev.sccId = SCC_FINISHED;
                iter.remove();
            } while (prevId != nodeId);

            final int nodeStackSize = currentSccNodeStack.size();
            final List<N> sccNodes = currentSccNodeStack.subList(nodeStackSize - numOfNodes, nodeStackSize);
            listener.foundSCC(sccNodes);
            sccNodes.clear();
        }
    }

    @Override
    public GraphTraversalAction processEdge(N srcNode,
                                            TarjanSCCRecord srcData,
                                            E edge,
                                            N tgtNode,
                                            Holder<TarjanSCCRecord> dataHolder) {
        TarjanSCCRecord rec = records.get(tgtNode);
        if (rec == null) {
            rec = createRecord();
            dataHolder.value = rec;
            return GraphTraversalAction.EXPLORE;
        }

        if (rec.sccId != SCC_FINISHED) {
            int tgtId = rec.number;
            /*
             * if our successor has a lower scc id than we do, it belongs to an SCC of one of our ascendants,
             * Thus we have detected a cycle and belong to the same SCC.
             */
            if (tgtId < srcData.sccId) {
                srcData.sccId = tgtId;
            }
        }
        return GraphTraversalAction.IGNORE;
    }

    @Override
    public void backtrackEdge(N srcNode, TarjanSCCRecord srcData, E edge, N tgtNode, TarjanSCCRecord tgtData) {
        int tgtId = tgtData.sccId;
        /*
         * if during backtracking, we detect our successor has a lower scc id than we do, it belongs to an SCC of one of
         * our ascendants. Thus we have detected a cycle and belong to the same SCC.
         */
        if (tgtId != SCC_FINISHED && tgtId < srcData.sccId) {
            srcData.sccId = tgtId;
        }
    }

    private TarjanSCCRecord createRecord() {
        return new TarjanSCCRecord(counter++);
    }

    public boolean hasVisited(N node) {
        return records.get(node) != null;
    }

}
