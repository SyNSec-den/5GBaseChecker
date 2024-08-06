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
package net.automatalib.util.graphs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import com.google.common.collect.AbstractIterator;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import org.checkerframework.checker.index.qual.NonNegative;

@SuppressWarnings("nullness") // dataflow dependent nullness is hard to describe
final class FindShortestPathsIterator<N, E> extends AbstractIterator<Path<N, E>> {

    private final Queue<N> bfsQueue;
    private final IndefiniteGraph<N, E> graph;
    private final MutableMapping<N, Pred<N, E>> preds;
    private final Predicate<? super N> targetPred;
    private final int limit;

    FindShortestPathsIterator(IndefiniteGraph<N, E> graph,
                              Collection<? extends N> start,
                              @NonNegative int limit,
                              Predicate<? super N> targetPred) {
        this.graph = graph;
        this.preds = graph.createStaticNodeMapping();
        this.limit = limit;
        this.targetPred = targetPred;

        this.bfsQueue = new ArrayDeque<>(start.size());

        for (N startNode : start) {
            preds.put(startNode, new Pred<>(null, null, 0));
            bfsQueue.add(startNode);
        }
    }

    @Override
    protected Path<N, E> computeNext() {
        while (!bfsQueue.isEmpty()) {
            N curr = bfsQueue.poll();
            if (targetPred.test(curr)) {
                return makePath(curr);
            }

            final int currentDepth = preds.get(curr).depth;

            for (E edge : graph.getOutgoingEdges(curr)) {
                N tgt = graph.getTarget(edge);
                Pred<N, E> targetPred = preds.get(tgt);
                if (targetPred == null && currentDepth < limit) {
                    preds.put(tgt, new Pred<>(curr, edge, currentDepth + 1));
                    bfsQueue.add(tgt);
                }
            }
        }

        return endOfData();
    }

    private Path<N, E> makePath(N target) {
        N currNode = target;
        Pred<N, E> pred = preds.get(currNode);

        List<E> edges = new ArrayList<>(pred.depth);

        while (pred != null && pred.edge != null) {
            edges.add(pred.edge);

            currNode = pred.node;
            pred = preds.get(currNode);
        }

        Collections.reverse(edges);

        return new Path<>(graph, currNode, edges);
    }

    private static final class Pred<N, E> {

        public final N node;
        public final E edge;
        public final int depth;

        Pred(N node, E edge, int depth) {
            this.node = node;
            this.edge = edge;
            this.depth = depth;
        }
    }

}
