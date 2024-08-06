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
package net.automatalib.graphs.base.compact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.graphs.MutableGraph.IntAbstraction;
import net.automatalib.graphs.concepts.NodeIDs;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractCompactGraph<E extends CompactEdge<EP>, NP, EP>
        implements MutableGraph<Integer, E, NP, EP>, IntAbstraction<E, NP, EP>, NodeIDs<Integer> {

    private final ResizingArrayStorage<List<E>> edges;
    private int size;

    public AbstractCompactGraph() {
        this.edges = new ResizingArrayStorage<>(List.class);
    }

    public AbstractCompactGraph(int initialCapacity) {
        this.edges = new ResizingArrayStorage<>(List.class, initialCapacity);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Collection<Integer> getNodes() {
        return CollectionsUtil.intRange(0, size);
    }

    @Override
    public NodeIDs<Integer> nodeIDs() {
        return this;
    }

    @Override
    public Collection<E> getOutgoingEdges(Integer node) {
        return getOutgoingEdges(node.intValue());
    }

    @Override
    public Collection<E> getOutgoingEdges(int node) {
        return Collections.unmodifiableCollection(edges.array[node]);
    }

    @Override
    public Integer getTarget(E edge) {
        return getIntTarget(edge);
    }

    @Override
    public int getIntTarget(E edge) {
        return edge.getTarget();
    }

    @Override
    public Integer addNode(@Nullable NP property) {
        return addIntNode(property);
    }

    @Override
    public int addIntNode(@Nullable NP property) {
        int n = size++;
        edges.ensureCapacity(n + 1);
        edges.array[n] = new ArrayList<>();
        setNodeProperty(n, property);
        return n;
    }

    @Override
    public void setNodeProperty(Integer node, @Nullable NP property) {
        setNodeProperty(node.intValue(), property);
    }

    @Override
    public E connect(Integer source, Integer target, @Nullable EP property) {
        return connect(source.intValue(), target.intValue(), property);
    }

    @Override
    public E connect(int source, int target, @Nullable EP property) {
        E edge = createEdge(source, target, property);
        List<E> edges = this.edges.array[source];
        edge.outIndex = edges.size();
        edges.add(edge);
        return edge;
    }

    protected abstract E createEdge(int source, int target, @Nullable EP property);

    @Override
    public void setEdgeProperty(E edge, EP property) {
        edge.setProperty(property);
    }

    @Override
    public int getNodeId(Integer node) {
        return node;
    }

    @Override
    public Integer getNode(int id) {
        return id;
    }

    @Override
    public NP getNodeProperty(Integer node) {
        return getNodeProperty(node.intValue());
    }

    @Override
    public EP getEdgeProperty(E edge) {
        return edge.getProperty();
    }

}