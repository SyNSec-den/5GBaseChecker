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
package net.automatalib.modelcheckers.m3c.visualization;

import java.util.Map;
import java.util.Objects;

import net.automatalib.graphs.base.compact.CompactEdge;
import net.automatalib.modelcheckers.m3c.solver.WitnessTree;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

/**
 * A base {@link VisualizationHelper} for {@link WitnessTree}s.
 *
 * @author freese
 * @author frohme
 */
abstract class AbstractVisualizationHelper extends DefaultVisualizationHelper<Integer, CompactEdge<String>> {

    protected final WitnessTree<?, ?> resultTree;

    AbstractVisualizationHelper(WitnessTree<?, ?> resultTree) {
        this.resultTree = resultTree;
    }

    @Override
    public boolean getNodeProperties(Integer node, Map<String, String> properties) {
        if (super.getNodeProperties(node, properties)) {
            properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
            properties.put(NodeAttrs.LABEL, Objects.toString(resultTree.getNodeProperty(node)));
            return true;
        }

        return false;
    }

    @Override
    public boolean getEdgeProperties(Integer src,
                                     CompactEdge<String> edge,
                                     Integer tgt,
                                     Map<String, String> properties) {
        if (super.getEdgeProperties(src, edge, tgt, properties)) {
            properties.put(EdgeAttrs.ARROWHEAD, "none");
            properties.put(EdgeAttrs.LABEL, Objects.toString(edge.getProperty()));
            return true;
        }

        return false;
    }
}
