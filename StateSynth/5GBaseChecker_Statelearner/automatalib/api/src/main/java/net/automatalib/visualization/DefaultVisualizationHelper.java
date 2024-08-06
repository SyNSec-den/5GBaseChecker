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
package net.automatalib.visualization;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class DefaultVisualizationHelper<N, E> implements VisualizationHelper<N, E> {

    private @MonotonicNonNull Set<N> initialNodes;

    protected Collection<N> initialNodes() {
        return Collections.emptySet();
    }

    @Override
    public boolean getNodeProperties(N node, Map<String, String> properties) {
        if (initialNodes == null) {
            initialNodes = new HashSet<>(initialNodes());
        }

        properties.putIfAbsent(NodeAttrs.LABEL, String.valueOf(node));
        properties.putIfAbsent(NodeAttrs.SHAPE, NodeShapes.CIRCLE);
        properties.putIfAbsent(NodeAttrs.INITIAL, Boolean.toString(initialNodes.contains(node)));

        return true;
    }

    @Override
    public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
        properties.putIfAbsent(NodeAttrs.LABEL, String.valueOf(edge));
        return true;
    }

}
