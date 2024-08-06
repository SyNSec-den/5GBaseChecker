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
package net.automatalib.visualization.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.automatalib.visualization.VisualizationHelper;

public class AggregateVisualizationHelper<N, E> implements VisualizationHelper<N, E> {

    private final List<VisualizationHelper<N, ? super E>> helpers;

    public AggregateVisualizationHelper(VisualizationHelper<N, ? super E> rootVisualizer,
                                        List<? extends VisualizationHelper<N, ? super E>> helpers) {
        this.helpers = new ArrayList<>(helpers.size() + 1);
        this.helpers.add(rootVisualizer);
        this.helpers.addAll(helpers);
    }

    public AggregateVisualizationHelper(List<? extends VisualizationHelper<N, ? super E>> helpers) {
        this.helpers = new ArrayList<>(helpers);
    }

    @Override
    public void getGlobalNodeProperties(Map<String, String> properties) {
        for (VisualizationHelper<N, ? super E> helper : helpers) {
            helper.getGlobalNodeProperties(properties);
        }
    }

    @Override
    public void getGlobalEdgeProperties(Map<String, String> properties) {
        for (VisualizationHelper<N, ? super E> helper : helpers) {
            helper.getGlobalEdgeProperties(properties);
        }
    }

    @Override
    public boolean getNodeProperties(N node, Map<String, String> properties) {
        for (VisualizationHelper<N, ? super E> helper : helpers) {
            if (!helper.getNodeProperties(node, properties)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
        for (VisualizationHelper<N, ? super E> helper : helpers) {
            if (!helper.getEdgeProperties(src, edge, tgt, properties)) {
                return false;
            }
        }
        return true;
    }

}
