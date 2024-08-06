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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

import net.automatalib.commons.util.Holder;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.util.graphs.traversal.DFRecord.LastEdge;
import net.automatalib.util.traversal.TraversalOrder;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class GraphTraversal {

    private GraphTraversal() {} // prevent inheritance

    public static <N, E, D> boolean traverse(TraversalOrder order,
                                             IndefiniteGraph<N, E> graph,
                                             int limit,
                                             N initialNode,
                                             GraphTraversalVisitor<N, E, D> vis) {
        return traverse(order, graph, limit, Collections.singleton(initialNode), vis);
    }

    public static <N, E, D> boolean traverse(TraversalOrder order,
                                             IndefiniteGraph<N, E> graph,
                                             int limit,
                                             Collection<? extends N> initialNodes,
                                             GraphTraversalVisitor<N, E, D> vis) {
        switch (order) {
            case BREADTH_FIRST:
                return breadthFirst(graph, limit, initialNodes, vis);
            case DEPTH_FIRST:
                return depthFirst(graph, limit, initialNodes, vis);
            default:
                throw new IllegalArgumentException("Unknown traversal order " + order);
        }
    }

    public static <N, E, D> boolean traverse(TraversalOrder order,
                                             IndefiniteGraph<N, E> graph,
                                             N initialNode,
                                             GraphTraversalVisitor<N, E, D> vis) {
        return traverse(order, graph, Collections.singleton(initialNode), vis);
    }

    public static <N, E, D> boolean traverse(TraversalOrder order,
                                             IndefiniteGraph<N, E> graph,
                                             Collection<? extends N> initialNodes,
                                             GraphTraversalVisitor<N, E, D> vis) {
        return traverse(order, graph, -1, initialNodes, vis);
    }

    public static <N, E, D> boolean depthFirst(IndefiniteGraph<N, E> graph,
                                               int limit,
                                               Collection<? extends N> initialNodes,
                                               GraphTraversalVisitor<N, E, D> vis) {

        // setting the following to false means that the traversal had to be aborted
        // due to reaching the limit
        boolean complete = true;

        int nodeCount = 0;

        Deque<DFRecord<N, E, D>> dfsStack = new ArrayDeque<>();

        Holder<D> dataHolder = new Holder<>();

        for (N init : initialNodes) {

            dataHolder.value = null;
            GraphTraversalAction act = vis.processInitial(init, dataHolder);

            switch (act) {
                case IGNORE:
                case ABORT_NODE:
                    continue;
                case ABORT_TRAVERSAL:
                    return complete;
                case EXPLORE:
                    if (nodeCount != limit) {
                        dfsStack.push(new DFRecord<>(init, dataHolder.value));
                        nodeCount++;
                    } else {
                        complete = false;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action " + act);
            }
        }

        while (!dfsStack.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull DFRecord<N, E, D> current = dfsStack.peek();

            N currNode = current.node;
            D currData = current.data;

            if (current.start(graph) && !vis.startExploration(currNode, currData)) {
                dfsStack.pop();
                continue;
            }

            LastEdge<E, N, D> lastEdge = current.getLastEdge();
            if (lastEdge != null) {
                vis.backtrackEdge(currNode, currData, lastEdge.edge, lastEdge.node, lastEdge.data);
            }

            if (!current.hasNextEdge()) {
                dfsStack.pop();
                vis.finishExploration(currNode, currData);
                continue;
            }

            E edge = current.nextEdge();

            N tgt = graph.getTarget(edge);

            GraphTraversalAction act = vis.processEdge(currNode, currData, edge, tgt, dataHolder);

            switch (act) {
                case IGNORE:
                    continue;
                case ABORT_NODE:
                    dfsStack.pop();
                    continue;
                case ABORT_TRAVERSAL:
                    return complete;
                case EXPLORE:
                    if (nodeCount != limit) {
                        D data = dataHolder.value;
                        current.setLastEdge(edge, tgt, data);
                        dfsStack.push(new DFRecord<>(tgt, data));
                        nodeCount++;
                    } else {
                        complete = false;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action " + act);
            }
        }

        return complete;
    }

    public static <N, E, D> boolean depthFirst(IndefiniteGraph<N, E> graph,
                                               N initNode,
                                               GraphTraversalVisitor<N, E, D> vis) {
        return depthFirst(graph, Collections.singleton(initNode), vis);
    }

    public static <N, E, D> boolean depthFirst(IndefiniteGraph<N, E> graph,
                                               int limit,
                                               N initNode,
                                               GraphTraversalVisitor<N, E, D> vis) {
        return depthFirst(graph, limit, Collections.singleton(initNode), vis);
    }

    public static <N, E, D> boolean depthFirst(IndefiniteGraph<N, E> graph,
                                               Collection<? extends N> initialNodes,
                                               GraphTraversalVisitor<N, E, D> vis) {
        return depthFirst(graph, -1, initialNodes, vis);
    }

    public static <N, E, D> boolean breadthFirst(IndefiniteGraph<N, E> graph,
                                                 int limit,
                                                 Collection<? extends N> initialNodes,
                                                 GraphTraversalVisitor<N, E, D> vis) {

        Queue<BFRecord<N, D>> bfsQueue = new ArrayDeque<>();

        // setting the following to false means that the traversal had to be aborted
        // due to reaching the limit
        boolean complete = true;
        int nodeCount = 0;

        Holder<D> dataHolder = new Holder<>();

        for (N init : initialNodes) {
            dataHolder.value = null;
            GraphTraversalAction act = vis.processInitial(init, dataHolder);

            switch (act) {
                case IGNORE:
                case ABORT_NODE:
                    continue;
                case ABORT_TRAVERSAL:
                    return complete;
                case EXPLORE:
                    if (nodeCount != limit) { // not equals will always be true for negative limit values
                        bfsQueue.add(new BFRecord<>(init, dataHolder.value));
                        nodeCount++;
                    } else {
                        complete = false;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action " + act);
            }
        }

        bfs_loop:
        while (!bfsQueue.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull BFRecord<N, D> current = bfsQueue.poll();

            N currNode = current.node;
            D currData = current.data;

            if (!vis.startExploration(currNode, currData)) {
                continue;
            }

            Collection<E> edges = graph.getOutgoingEdges(currNode);

            for (E edge : edges) {

                N tgtNode = graph.getTarget(edge);

                dataHolder.value = null;
                GraphTraversalAction act = vis.processEdge(currNode, currData, edge, tgtNode, dataHolder);

                switch (act) {
                    case IGNORE:
                        continue;
                    case ABORT_NODE:
                        continue bfs_loop;
                    case ABORT_TRAVERSAL:
                        return complete;
                    case EXPLORE:
                        if (nodeCount != limit) { // not equals will always be true for negative limit values
                            bfsQueue.offer(new BFRecord<>(tgtNode, dataHolder.value));
                            nodeCount++;
                        } else {
                            complete = false;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown action " + act);
                }
            }

            vis.finishExploration(currNode, currData);
        }

        return complete;
    }

    public static <N, E, D> boolean breadthFirst(IndefiniteGraph<N, E> graph,
                                                 int limit,
                                                 N initialNode,
                                                 GraphTraversalVisitor<N, E, D> visitor) {
        return breadthFirst(graph, limit, Collections.singleton(initialNode), visitor);
    }

    public static <N, E, D> boolean breadthFirst(IndefiniteGraph<N, E> graph,
                                                 Collection<? extends N> initialNodes,
                                                 GraphTraversalVisitor<N, E, D> visitor) {
        return breadthFirst(graph, -1, initialNodes, visitor);
    }

    public static <N, E, D> boolean breadthFirst(IndefiniteGraph<N, E> graph,
                                                 N initialNode,
                                                 GraphTraversalVisitor<N, E, D> visitor) {
        return breadthFirst(graph, Collections.singleton(initialNode), visitor);
    }

    public static <N, E, D> boolean dfs(IndefiniteGraph<N, E> graph,
                                        N initialNode,
                                        DFSVisitor<? super N, ? super E, D> visitor) {
        return dfs(graph, Collections.singleton(initialNode), visitor);
    }

    public static <N, E, D> boolean dfs(IndefiniteGraph<N, E> graph,
                                        int limit,
                                        Collection<? extends N> initialNodes,
                                        DFSVisitor<? super N, ? super E, D> visitor) {
        GraphTraversalVisitor<N, E, DFSData<D>> traversalVisitor = new DFSTraversalVisitor<>(graph, visitor);
        return depthFirst(graph, limit, initialNodes, traversalVisitor);
    }

    public static <N, E, D> boolean dfs(IndefiniteGraph<N, E> graph,
                                        Collection<? extends N> initialNodes,
                                        DFSVisitor<? super N, ? super E, D> visitor) {
        return dfs(graph, -1, initialNodes, visitor);
    }

    public static <N, E> Iterable<N> breadthFirstOrder(final IndefiniteGraph<N, E> graph,
                                                       final Collection<? extends N> start) {

        return () -> bfIterator(graph, start);
    }

    public static <N, E> Iterator<N> bfIterator(IndefiniteGraph<N, E> graph, Collection<? extends N> start) {
        return new BreadthFirstIterator<>(graph, start);
    }

    public static <N, E> Iterable<N> depthFirstOrder(final IndefiniteGraph<N, E> graph,
                                                     final Collection<? extends N> start) {

        return () -> dfIterator(graph, start);
    }

    public static <N, E> Iterator<N> dfIterator(IndefiniteGraph<N, E> graph, Collection<? extends N> start) {
        return new DepthFirstIterator<>(graph, start);
    }

}
