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
package net.automatalib.incremental.mealy.tree.dynamic;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import net.automatalib.incremental.mealy.tree.Edge;
import org.checkerframework.checker.nullness.qual.Nullable;

final class Node<I, O> {

    private final Map<I, Edge<Node<I, O>, O>> outEdges;

    Node(int expectedSize) {
        this.outEdges = Maps.newHashMapWithExpectedSize(expectedSize);
    }

    Node() {
        this.outEdges = new HashMap<>();
    }

    @Nullable Edge<Node<I, O>, O> getEdge(I input) {
        return outEdges.get(input);
    }

    void setEdge(I symbol, Edge<Node<I, O>, O> edge) {
        outEdges.put(symbol, edge);
    }

    Map<I, Edge<Node<I, O>, O>> getOutEdges() {
        return outEdges;
    }
}