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
package net.automatalib.graphs.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.Iterators;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.graphs.ShrinkableGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A very simple graph realization, where nodes can be arbitrary Java objects. This graph does not support edge
 * properties.
 * <p>
 * This class provides maximum flexibility, but should only be used if performance is not a major concern.
 *
 * @param <N>
 *         node type
 *
 * @author Malte Isberner
 */
public class SimpleMapGraph<@Nullable N> implements MutableGraph<N, N, N, Void>, ShrinkableGraph<N, N> {

    private final Map<N, Collection<N>> structureMap;
    private final Supplier<? extends Collection<N>> adjCollSupplier;

    /**
     * Initializes a graph where the adjacency structure is stored using a {@link HashMap}, and adjacency information
     * for a single node is stored using {@link HashSet}s.
     */
    public SimpleMapGraph() {
        this(HashSet::new);
    }

    /**
     * Initializes a graph where the adjacency structure is stored using a {@link HashMap}, and adjacency information
     * for a single node is stored in data structures created via the provided supplier.
     *
     * @param adjCollSupplier
     *         the supplier for per-node adjacency collections
     */
    public SimpleMapGraph(Supplier<? extends Collection<N>> adjCollSupplier) {
        this(new HashMap<>(), adjCollSupplier);
    }

    /**
     * Initializes a graph using the given adjacency structure, and adjacency information for a single node is stored
     * using {@link HashSet}s.
     *
     * @param structureMap
     *         the map for the overall graph structure
     */
    public SimpleMapGraph(Map<N, Collection<N>> structureMap) {
        this(structureMap, HashSet::new);
    }

    /**
     * Initializes a graph using the given adjacency structure, and adjacency information for a single node is stored in
     * data structures created via the provided supplier.
     *
     * @param structureMap
     *         the map for the overall graph structure
     * @param adjCollSupplier
     *         the supplier for per-node adjacency collections
     */
    public SimpleMapGraph(Map<N, Collection<N>> structureMap, Supplier<? extends Collection<N>> adjCollSupplier) {
        this.structureMap = structureMap;
        this.adjCollSupplier = adjCollSupplier;
    }

    @Override
    public Collection<N> getAdjacentTargets(N node) {
        return getOutgoingEdges(node);
    }

    @Override
    public Stream<N> adjacentTargetsStream(N node) {
        return outgoingEdgesStream(node);
    }

    @SuppressWarnings("nullness") // the passed structureMap decides whether we support nulls
    @Override
    public Collection<N> getOutgoingEdges(N node) {
        return Collections.unmodifiableCollection(structureMap.getOrDefault(node, Collections.emptySet()));
    }

    @Override
    public N getTarget(N edge) {
        return edge;
    }

    @Override
    public Set<N> getNodes() {
        return Collections.unmodifiableSet(structureMap.keySet());
    }

    @Override
    public Iterator<N> iterator() {
        return Iterators.unmodifiableIterator(structureMap.keySet().iterator());
    }

    @Override
    public N getNodeProperty(N node) {
        return node;
    }

    @Override
    public Void getEdgeProperty(N edge) {
        return null;
    }

    @Override
    public N addNode(@Nullable N property) {
        structureMap.putIfAbsent(property, adjCollSupplier.get());
        return property;
    }

    @SuppressWarnings("nullness") // the passed structureMap decides whether we support nulls
    @Override
    public N connect(N source, N target, Void property) {
        structureMap.get(source).add(target);
        return target;
    }

    @Override
    @Deprecated
    public void setNodeProperty(N node, N property) {
        if (!Objects.equals(node, property)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setEdgeProperty(N edge, Void property) {}

    @Override
    public void removeNode(N node, @Nullable N replacement) {
        structureMap.remove(node);
        structureMap.values().forEach(a -> {
            if (a.remove(node) && replacement != null) {
                a.add(replacement);
            }
        });
    }

    @SuppressWarnings("nullness") // the passed structureMap decides whether we support nulls
    @Override
    public void removeEdge(N node, N edge) {
        structureMap.get(node).remove(edge);
    }

}
