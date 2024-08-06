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
package net.automatalib.graphs.visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.visualization.DefaultVisualizationHelper;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CFMPSVisualizationHelper<N, L, E> extends DefaultVisualizationHelper<Pair<L, N>, Pair<L, E>> {

    private final Map<L, PMPGVisualizationHelper<N, E, ?>> visualizers;
    private final List<Pair<L, N>> initialNodes;

    // cast is fine, because we make sure to only query nodes/edges belonging to the respective procedures
    @SuppressWarnings("unchecked")
    public CFMPSVisualizationHelper(Map<L, ? extends ProceduralModalProcessGraph<? extends N, L, ? extends E, ?, ?>> pmpgs) {

        this.visualizers = Maps.newHashMapWithExpectedSize(pmpgs.size());
        this.initialNodes = new ArrayList<>(pmpgs.size());

        for (Entry<L, ? extends ProceduralModalProcessGraph<? extends N, L, ? extends E, ?, ?>> e : pmpgs.entrySet()) {
            final ProceduralModalProcessGraph<N, L, E, ?, ?> value =
                    (ProceduralModalProcessGraph<N, L, E, ?, ?>) e.getValue();
            final N initialNode = value.getInitialNode();

            this.visualizers.put(e.getKey(), new PMPGVisualizationHelper<>(value));

            if (initialNode != null) {
                this.initialNodes.add(Pair.of(e.getKey(), initialNode));
            }
        }
    }

    @Override
    protected Collection<Pair<L, N>> initialNodes() {
        return this.initialNodes;
    }

    @Override
    public boolean getNodeProperties(Pair<L, N> node, Map<String, String> properties) {

        final L process = node.getFirst();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which procedures exist
        final @NonNull PMPGVisualizationHelper<N, E, ?> visualizer = this.visualizers.get(process);

        return visualizer.getNodeProperties(node.getSecond(), properties);
    }

    @Override
    public boolean getEdgeProperties(Pair<L, N> src, Pair<L, E> edge, Pair<L, N> tgt, Map<String, String> properties) {

        final L process = edge.getFirst();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which procedures exist
        final @NonNull PMPGVisualizationHelper<N, E, ?> visualizer = this.visualizers.get(process);

        return visualizer.getEdgeProperties(src.getSecond(), edge.getSecond(), tgt.getSecond(), properties);
    }

}
