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
package net.automatalib.visualization.dot;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import net.automatalib.graphs.Graph;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.VisualizationProvider;
import org.kohsuke.MetaInfServices;

@MetaInfServices(VisualizationProvider.class)
public class GraphVizSwingVisualizationProvider implements VisualizationProvider {

    /**
     * the {@link #getId() id} of this {@link VisualizationProvider}.
     */
    public static final String ID = "graphviz-swing";

    private static final int PRIORITY = 11;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean checkUsable() {
        return DOT.checkUsable();
    }

    @Override
    public <N, E> void visualize(Graph<N, E> graph,
                                 List<VisualizationHelper<N, ? super E>> additionalHelpers,
                                 boolean modal,
                                 Map<String, String> visOptions) {
        try (Writer w = DOT.createDotWriter(modal)) {
            GraphDOT.write(graph, w, additionalHelpers);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error rendering graph: " + ex.getMessage());
        }
    }
}
