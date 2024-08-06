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

import net.automatalib.graphs.base.compact.CompactEdge;
import net.automatalib.modelcheckers.m3c.solver.WitnessTree;
import net.automatalib.visualization.VisualizationHelper;

/**
 * A {@link VisualizationHelper} for {@link WitnessTree}s that emphasizes edges of the witness and de-emphasizes the
 * remaining ones.
 *
 * @author freese
 * @author frohme
 */
public class EdgeVisualizationHelper extends AbstractVisualizationHelper {

    public EdgeVisualizationHelper(WitnessTree<?, ?> resultTree) {
        super(resultTree);
    }

    @Override
    public boolean getEdgeProperties(Integer src,
                                     CompactEdge<String> edge,
                                     Integer tgt,
                                     Map<String, String> properties) {
        if (super.getEdgeProperties(src, edge, tgt, properties)) {
            if (resultTree.getNodeProperty(src).isPartOfResult && resultTree.getNodeProperty(tgt).isPartOfResult) {
                properties.put(EdgeAttrs.STYLE, EdgeStyles.BOLD);
            } else {
                properties.put(EdgeAttrs.STYLE, EdgeStyles.DASHED);
            }
            return true;
        }
        return false;
    }
}
