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

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import net.automatalib.commons.util.mappings.MapMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.helpers.IndefiniteNormalGraphView;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simplified interface for indefinite graphs, exposing only adjacency information, but no further information about
 * edge objects.
 *
 * @param <N>
 *         node type
 *
 * @author Malte Isberner
 */
public interface IndefiniteSimpleGraph<N> {

    Collection<N> getAdjacentTargets(N node);

    default Stream<N> adjacentTargetsStream(N node) {
        return getAdjacentTargets(node).stream();
    }

    default boolean isConnected(N source, N target) {
        return adjacentTargetsStream(source).anyMatch(n -> Objects.equals(n, target));
    }

    default <@Nullable V> MutableMapping<N, V> createStaticNodeMapping() {
        return new MapMapping<>();
    }

    default <@Nullable V> MutableMapping<N, V> createDynamicNodeMapping() {
        return new MapMapping<>();
    }

    default IndefiniteGraph<N, ?> asNormalGraph() {
        return new IndefiniteNormalGraphView<>(this);
    }

}
