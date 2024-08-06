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

import javax.swing.JOptionPane;

import net.automatalib.graphs.Graph;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(VisualizationProvider.class)
public class DummyVP implements VisualizationProvider {

    /**
     * the {@link #getId() id} of this {@link VisualizationProvider}.
     */
    public static final String ID = "dummy";

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyVP.class);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean checkUsable() {
        return true;
    }

    @SuppressWarnings("nullness") // false-positive on JOptionPane.showMessageDialog
    @Override
    public <N, E> void visualize(Graph<N, E> graph,
                                 List<VisualizationHelper<N, ? super E>> additionalHelpers,
                                 boolean modal,
                                 Map<String, String> options) {
        final String errorMsg = "Attempted to visualize graph, but no usable visualization provider was configured.";
        LOGGER.error(errorMsg);
        if (modal) {
            JOptionPane.showMessageDialog(null, errorMsg + "\nPress OK to continue ...");
        }
    }
}
