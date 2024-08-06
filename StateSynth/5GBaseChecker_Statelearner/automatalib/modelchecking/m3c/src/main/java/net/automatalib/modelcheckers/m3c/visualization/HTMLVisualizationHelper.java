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

import net.automatalib.modelcheckers.m3c.solver.WitnessTree;
import net.automatalib.modelcheckers.m3c.solver.WitnessTreeState;
import net.automatalib.visualization.VisualizationHelper;

/**
 * A {@link VisualizationHelper} for {@link WitnessTree}s that renders the node labels as an HTML-based table. Note that
 * the syntax is specific to a DOT-based visualization.
 *
 * @author freese
 * @author frohme
 */
public class HTMLVisualizationHelper extends AbstractVisualizationHelper {

    private final boolean shortDisplay;

    public HTMLVisualizationHelper(WitnessTree<?, ?> resultTree) {
        this(resultTree, false);
    }

    public HTMLVisualizationHelper(WitnessTree<?, ?> resultTree, boolean shortDisplay) {
        super(resultTree);
        this.shortDisplay = shortDisplay;
    }

    @Override
    public boolean getNodeProperties(Integer node, Map<String, String> properties) {

        if (super.getNodeProperties(node, properties)) {

            final WitnessTreeState<?, ?, ?, ?> prop = resultTree.getNodeProperty(node);

            final String label =
                    "<HTML><TABLE BORDER=\"0px\">" +
                    "<TR><TD>State:</TD><TD>" + prop.state + "</TD> </TR>" +
                    "<TR><TD>Context:</TD><TD>" + prop.context + "</TD></TR>" +
                    "<TR><TD>Procedure:</TD><TD>" + prop.procedure + "</TD> </TR>" +
                    "<TR><TD>ReturnAddress:</TD><TD>" + (prop.stack != null ? prop.stack.state : null) + "</TD></TR>" +
                    "<TR><TD>Formula:</TD><TD>" + renderFormula(node) + "</TD></TR>" +
                    "</TABLE>";

            properties.put(NodeAttrs.LABEL, label);
            return true;
        }

        return false;

    }

    private String renderFormula(Integer node) {
        final WitnessTreeState<?, ?, ?, ?> prop = resultTree.getNodeProperty(node);

        if (shortDisplay) {
            return Integer.toString(prop.subformula.getVarNumber());
        } else {
            final String formula = prop.subformula.toString();
            return formula.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

}
