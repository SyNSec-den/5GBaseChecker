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

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.automatalib.graphs.IndefiniteGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Path<N, E> extends AbstractList<E> {

    private final IndefiniteGraph<N, E> graph;
    private final N start;
    private final List<? extends E> edgeList;

    Path(IndefiniteGraph<N, E> graph, N start, List<? extends E> edgeList) {
        this.graph = graph;
        this.start = start;
        this.edgeList = edgeList;
    }

    public Iterator<N> nodeIterator() {
        return new NodeIterator();
    }

    public Iterable<N> nodes() {
        return this::nodeIterator;
    }

    public List<E> edgeList() {
        return Collections.unmodifiableList(edgeList);
    }

    public List<N> nodeList() {
        return new NodeList();
    }

    public N firstNode() {
        return start;
    }

    public @Nullable E firstEdge() {
        if (edgeList.isEmpty()) {
            return null;
        }
        return edgeList.get(0);
    }

    public N endNode() {
        E edge = lastEdge();
        if (edge == null) {
            return start;
        }
        return graph.getTarget(edge);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
        return (Iterator<E>) edgeList.iterator();
    }

    public @Nullable E lastEdge() {
        int idx = edgeList.size() - 1;
        if (idx < 0) {
            return null;
        }
        return edgeList.get(idx);
    }

    @Override
    public E get(int index) {
        return edgeList.get(index);
    }

    @Override
    public int size() {
        return edgeList.size();
    }

    @Override
    public boolean isEmpty() {
        return edgeList.isEmpty();
    }

    public static final class PathData<N, E> {

        public final N start;
        public final List<? extends E> edgeList;

        public PathData(N start, List<? extends E> edgeList) {
            this.start = start;
            this.edgeList = edgeList;
        }

        public Path<N, E> toPath(IndefiniteGraph<N, E> graph) {
            return new Path<>(graph, start, edgeList);
        }
    }

    private final class NodeIterator implements Iterator<N> {

        private Iterator<? extends E> edgeIt;

        @Override
        public boolean hasNext() {
            if (edgeIt == null) {
                return true;
            }
            return edgeIt.hasNext();
        }

        @Override
        public N next() {
            if (edgeIt == null) {
                edgeIt = edgeList.iterator();
                return start;
            }
            E edge = edgeIt.next();
            return graph.getTarget(edge);
        }
    }

    private class NodeList extends AbstractList<N> {

        @Override
        public N get(int index) {
            if (index < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (index == 0) {
                return start;
            }
            E edge = edgeList.get(index - 1);
            return graph.getTarget(edge);
        }

        @Override
        public int size() {
            return edgeList.size() + 1;
        }

        @Override
        public Iterator<N> iterator() {
            return nodeIterator();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

}
