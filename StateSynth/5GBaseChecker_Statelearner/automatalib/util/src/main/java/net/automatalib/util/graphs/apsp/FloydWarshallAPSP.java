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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.EdgeWeights;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.util.graphs.Graphs;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Implementation of the Floyd-Warshall dynamic programming algorithm for the all pairs shortest paths problem.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 *
 * @author Malte Isberner
 */
public class FloydWarshallAPSP<N, E> implements APSPResult<N, E> {

    private final int size;
    private final NodeIDs<N> ids;
    private final @Nullable APSPRecord<E>[][] table;

    @SuppressWarnings({"unchecked", "initialization"})
    public FloydWarshallAPSP(Graph<N, E> graph, EdgeWeights<E> ew) {
        this.size = graph.size();
        this.ids = graph.nodeIDs();
        this.table = new APSPRecord[size][size];

        initialize(graph, ew);
    }

    private void initialize(Graph<N, E> graph, EdgeWeights<E> ew) {
        for (int i = 0; i < size; i++) {
            N src = ids.getNode(i);

            Collection<E> edges = graph.getOutgoingEdges(src);

            for (E edge : edges) {
                N tgt = graph.getTarget(edge);
                if (Objects.equals(tgt, src)) {
                    continue;
                }

                int j = ids.getNodeId(tgt);
                float w = ew.getEdgeWeight(edge);
                APSPRecord<E> prev = table[i][j];
                if (prev == null || prev.distance > w) {
                    table[i][j] = new APSPRecord<>(edge, w);
                }
            }
        }
    }

    public static <N, E> APSPResult<N, E> findAPSP(Graph<N, E> graph, EdgeWeights<E> edgeWeights) {
        FloydWarshallAPSP<N, E> fw = new FloydWarshallAPSP<>(graph, edgeWeights);
        fw.findAPSP();
        return fw;
    }

    public void findAPSP() {
        for (int k = 0; k < size; k++) {

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (j == i) {
                        continue;
                    }

                    APSPRecord<E> currRec = table[i][j];

                    if (k == i || k == j) {
                        continue;
                    }

                    APSPRecord<E> part1 = table[i][k], part2 = table[k][j];

                    if (part1 == null || part2 == null) {
                        continue;
                    }

                    float dist1 = part1.distance, dist2 = part2.distance;
                    float total = dist1 + dist2;

                    if (currRec == null) {
                        currRec = new APSPRecord<>(total, k, part1.numEdges + part2.numEdges);
                        table[i][j] = currRec;
                    } else if (currRec.distance > total) {
                        currRec.distance = total;
                        currRec.middle = k;
                        currRec.numEdges = part1.numEdges + part2.numEdges;
                    }

                }
            }
        }
    }

    @Override
    public float getShortestPathDistance(N src, N tgt) {
        int srcId = ids.getNodeId(src), tgtId = ids.getNodeId(tgt);

        if (srcId == tgtId) {
            return 0;
        }

        APSPRecord<E> rec = table[srcId][tgtId];
        if (rec == null) {
            return Graphs.INVALID_DISTANCE;
        }

        return rec.distance;
    }

    @Override
    public @Nullable List<E> getShortestPath(N src, N tgt) {
        int srcId = ids.getNodeId(src), tgtId = ids.getNodeId(tgt);

        if (srcId == tgtId) {
            return Collections.emptyList();
        }

        APSPRecord<E> rec = table[srcId][tgtId];

        if (rec == null) {
            return null;
        }

        List<E> result = new ArrayList<>(rec.numEdges);

        buildPath(result, srcId, tgtId, rec);

        return result;
    }

    @SuppressWarnings("nullness") // our initialization ensures that we don't access undefined indices
    private void buildPath(List<E> path, int srcId, int tgtId, APSPRecord<E> rec) {
        if (rec.middle == -1) {
            path.add(rec.edge);
            return;
        }

        int middle = rec.middle;
        buildPath(path, srcId, middle, table[srcId][middle]);
        buildPath(path, middle, tgtId, table[middle][tgtId]);
    }

    private static final class APSPRecord<E> {

        public final @Nullable E edge;
        public float distance;
        public int middle;
        public int numEdges;

        APSPRecord(E edge, float distance) {
            this.edge = edge;
            this.distance = distance;
            this.middle = -1;
            this.numEdges = 1;
        }

        APSPRecord(float distance, int middle, int numEdges) {
            this.edge = null;
            this.distance = distance;
            this.middle = middle;
            this.numEdges = numEdges;
        }
    }
}
