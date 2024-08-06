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
package net.automatalib.util.graphs.copy;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.MutableGraph;

final class PlainGraphCopy<N1, E1, N2, E2, NP2, EP2>
        extends AbstractGraphCopy<N1, E1, N2, E2, NP2, EP2, Graph<N1, E1>> {

    PlainGraphCopy(Graph<N1, E1> inGraph,
                   MutableGraph<N2, E2, NP2, EP2> outGraph,
                   Mapping<? super N1, ? extends NP2> npMapping,
                   Mapping<? super E1, ? extends EP2> epMapping) {
        super(inGraph, outGraph, npMapping, epMapping);
    }

    @Override
    public void doCopy() {
        List<NodeRec<N1, N2>> outNodes = new ArrayList<>(inGraph.size());
        // Copy nodes
        for (N1 n1 : inGraph) {
            N2 n2 = copyNode(n1);
            outNodes.add(new NodeRec<>(n1, n2));
        }

        // Copy edges
        for (NodeRec<N1, N2> p : outNodes) {
            N1 n1 = p.inNode;
            N2 n2 = p.outNode;

            for (E1 edge : inGraph.getOutgoingEdges(n1)) {
                N1 tgt1 = inGraph.getTarget(edge);
                copyEdge(n2, edge, tgt1);
            }
        }
    }

    private static class NodeRec<N1, N2> {

        private final N1 inNode;
        private final N2 outNode;

        NodeRec(N1 inNode, N2 outNode) {
            this.inNode = inNode;
            this.outNode = outNode;
        }
    }

}
