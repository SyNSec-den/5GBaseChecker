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

import java.util.List;
import java.util.Map;

import net.automatalib.graphs.Graph;

/**
 * @author Malte Isberner
 */
public interface VisualizationProvider {

    /**
     * Returns the id of the visualization provider. This value is used and matched against the
     * {@link net.automatalib.AutomataLibProperty#VISUALIZATION_PROVIDER} property to select the chosen visualization
     * provider.
     *
     * @return the id of the provider
     */
    String getId();

    /**
     * Returns a description of the provider.
     *
     * @return the description of the provider
     */
    default String getDescription() {
        return "";
    }

    /**
     * Returns the priority of the provider. If no provider is selected via the
     * {@link net.automatalib.AutomataLibProperty#VISUALIZATION_PROVIDER} property, the provider with the highest
     * priority is chosen.
     *
     * @return the priority of the provider
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Checks whether the provider is supported on the current platform.
     *
     * @return {@code true} if the provider is able to visualiza, {@code false} otherwise
     *
     * @see #visualize(Graph, List, boolean, Map)
     */
    boolean checkUsable();

    /**
     * Visualizes the given graph by means of executing the visualization implementation.
     *
     * @param graph
     *         the graph model to visualize
     * @param additionalHelpers
     *         additional helpers to influence the visualization
     * @param modal
     *         a flag, whether the visualized graph should be displayed in a modal dialog (halting the current program
     *         execution) or not.
     * @param visOptions
     *         additional options for the provider
     * @param <N>
     *         the node type of the graph model
     * @param <E>
     *         the edge type of the graph model
     */
    <N, E> void visualize(Graph<N, E> graph,
                          List<VisualizationHelper<N, ? super E>> additionalHelpers,
                          boolean modal,
                          Map<String, String> visOptions);
}
