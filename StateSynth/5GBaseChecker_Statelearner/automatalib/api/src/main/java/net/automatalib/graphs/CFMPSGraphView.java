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
package net.automatalib.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.visualization.CFMPSVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Graph representation of a {@link ContextFreeModalProcessSystem} that displays all nodes of its sub-procedures once,
 * i.e., without incorporating execution semantics such as expansion.
 *
 * @param <N>
 *         common node type
 * @param <L>
 *         label type
 * @param <E>
 *         edge type
 * @param <AP>
 *         atomic proposition type
 *
 * @author frohme
 */
public class CFMPSGraphView<N, L, E, AP> implements Graph<Pair<L, N>, Pair<L, E>> {

    private final Map<L, ProceduralModalProcessGraph<N, L, E, AP, ?>> pmpgs;

    // cast is fine, because we make sure to only query nodes/edges belonging to the respective procedures
    @SuppressWarnings("unchecked")
    public CFMPSGraphView(Map<L, ? extends ProceduralModalProcessGraph<? extends N, L, ? extends E, AP, ?>> pmpgs) {
        this.pmpgs = (Map<L, ProceduralModalProcessGraph<N, L, E, AP, ?>>) pmpgs;
    }

    @Override
    public Collection<Pair<L, E>> getOutgoingEdges(Pair<L, N> node) {
        final L process = node.getFirst();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which pmpgs exist
        final @NonNull ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg = pmpgs.get(process);
        final Collection<E> outgoingEdges = pmpg.getOutgoingEdges(node.getSecond());

        final Collection<Pair<L, E>> result = new ArrayList<>(outgoingEdges.size());

        for (E e : outgoingEdges) {
            result.add(Pair.of(process, e));
        }

        return result;
    }

    @Override
    public Pair<L, N> getTarget(Pair<L, E> edge) {
        final L process = edge.getFirst();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which pmpgs exist
        final @NonNull ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg = pmpgs.get(process);

        return Pair.of(process, pmpg.getTarget(edge.getSecond()));
    }

    @Override
    public Collection<Pair<L, N>> getNodes() {
        final int numNodes = this.pmpgs.values().stream().mapToInt(Graph::size).sum();
        final List<Pair<L, N>> result = new ArrayList<>(numNodes);

        for (Entry<L, ProceduralModalProcessGraph<N, L, E, AP, ?>> e : this.pmpgs.entrySet()) {
            final L process = e.getKey();
            for (N n : e.getValue()) {
                result.add(Pair.of(process, n));
            }
        }

        return result;
    }

    @Override
    public VisualizationHelper<Pair<L, N>, Pair<L, E>> getVisualizationHelper() {
        return new CFMPSVisualizationHelper<>(this.pmpgs);
    }
}
