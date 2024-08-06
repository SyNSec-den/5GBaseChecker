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

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.graphs.IndefiniteGraph;

class SimpleDFRecord<N, E> {

    public final N node;

    private Iterator<E> edgeIterator;

    SimpleDFRecord(N node) {
        this.node = node;
    }

    public final boolean wasStarted() {
        return edgeIterator != null;
    }

    public final boolean start(IndefiniteGraph<N, E> graph) {
        if (edgeIterator != null) {
            return false;
        }
        Collection<E> outEdges = graph.getOutgoingEdges(node);
        this.edgeIterator = outEdges.iterator();
        return true;
    }

    public final boolean hasNextEdge() {
        if (edgeIterator == null) {
            throw new IllegalStateException("Edge iteration not yet started");
        }
        return edgeIterator.hasNext();
    }

    public final E nextEdge() {
        if (edgeIterator == null) {
            throw new IllegalStateException("Edge iteration not yet started");
        }
        return edgeIterator.next();
    }

}
