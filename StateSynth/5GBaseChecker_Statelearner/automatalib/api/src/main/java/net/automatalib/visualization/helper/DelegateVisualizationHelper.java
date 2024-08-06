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

import java.util.Map;

import net.automatalib.visualization.VisualizationHelper;

public class DelegateVisualizationHelper<N, E> implements VisualizationHelper<N, E> {

    private final VisualizationHelper<N, ? super E> parent;

    protected DelegateVisualizationHelper(VisualizationHelper<N, ? super E> parent) {
        this.parent = parent;
    }

    @Override
    public void getGlobalNodeProperties(Map<String, String> properties) {
        parent.getGlobalNodeProperties(properties);
    }

    @Override
    public void getGlobalEdgeProperties(Map<String, String> properties) {
        parent.getGlobalEdgeProperties(properties);
    }

    @Override
    public boolean getNodeProperties(N node, Map<String, String> properties) {
        return parent.getNodeProperties(node, properties);
    }

    @Override
    public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
        return parent.getEdgeProperties(src, edge, tgt, properties);
    }
}
