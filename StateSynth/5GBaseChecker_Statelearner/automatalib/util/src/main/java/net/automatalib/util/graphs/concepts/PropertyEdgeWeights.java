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
package net.automatalib.util.graphs.concepts;

import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.concepts.EdgeWeights;

public class PropertyEdgeWeights<E> implements EdgeWeights<E> {

    private final UniversalGraph<?, E, ?, ? extends Number> graph;

    public PropertyEdgeWeights(UniversalGraph<?, E, ?, ? extends Number> graph) {
        this.graph = graph;
    }

    @Override
    public float getEdgeWeight(E edge) {
        Number prop = graph.getEdgeProperty(edge);
        return prop.floatValue();
    }

}
