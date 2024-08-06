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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.visualization.DefaultVisualizationHelper;

public class PMPGVisualizationHelper<N, E, AP> extends DefaultVisualizationHelper<N, E> {

    private final ProceduralModalProcessGraph<N, ?, E, AP, ?> pmpg;

    public PMPGVisualizationHelper(ProceduralModalProcessGraph<N, ?, E, AP, ?> pmpg) {
        this.pmpg = pmpg;
    }

    @Override
    protected Collection<N> initialNodes() {
        final N initialNode = pmpg.getInitialNode();

        if (initialNode == null) {
            return Collections.emptySet();
        }

        return Collections.singleton(initialNode);
    }

    @Override
    public boolean getNodeProperties(N node, Map<String, String> properties) {

        if (!super.getNodeProperties(node, properties)) {
            return false;
        }

        final Set<AP> aps = pmpg.getNodeProperty(node);

        if (aps.isEmpty()) {
            properties.put(NodeAttrs.LABEL, "");
        } else {
            properties.put(NodeAttrs.LABEL, aps.toString());
        }

        properties.put(NodeAttrs.SHAPE, NodeShapes.CIRCLE);

        return true;
    }

    @Override
    public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {

        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
            return false;
        }

        final ProceduralModalEdgeProperty prop = pmpg.getEdgeProperty(edge);
        final StringJoiner styleJoiner = new StringJoiner(",");

        if (prop.isMayOnly()) {
            styleJoiner.add(EdgeStyles.DASHED);
        }

        if (prop.isProcess()) {
            styleJoiner.add(EdgeStyles.BOLD);
        }

        properties.put(EdgeAttrs.LABEL, String.valueOf(pmpg.getEdgeLabel(edge)));
        properties.put(EdgeAttrs.STYLE, styleJoiner.toString());

        return true;
    }

}
