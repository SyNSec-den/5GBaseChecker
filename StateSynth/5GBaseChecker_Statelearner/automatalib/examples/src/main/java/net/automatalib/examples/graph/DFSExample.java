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
package net.automatalib.examples.graph;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.graphs.base.compact.CompactEdge;
import net.automatalib.graphs.base.compact.CompactSimpleGraph;
import net.automatalib.util.graphs.traversal.BaseDFSVisitor;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.visualization.Visualization;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.VisualizationHelper.EdgeStyles;

public final class DFSExample {

    private DFSExample() {}

    public static void main(String[] args) {
        CompactSimpleGraph<Void> graph = new CompactSimpleGraph<>();

        int n0 = graph.addIntNode(), n1 = graph.addIntNode(), n2 = graph.addIntNode(), n3 = graph.addIntNode(), n4 =
                graph.addIntNode();

        graph.connect(n0, n1);
        graph.connect(n0, n2);
        graph.connect(n1, n1);
        graph.connect(n1, n2);
        graph.connect(n2, n3);
        graph.connect(n3, n1);
        graph.connect(n3, n0);
        graph.connect(n0, n4);
        graph.connect(n4, n3);

        MyDFSVisitor<Integer, CompactEdge<Void>> vis = new MyDFSVisitor<>();
        GraphTraversal.dfs(graph, n0, vis);
        DFSResultDOTHelper<Integer, CompactEdge<Void>> helper = new DFSResultDOTHelper<>(vis);

        Visualization.visualize(graph, helper);
    }

    public enum EdgeType {
        TREE(EdgeStyles.BOLD),
        FORWARD(EdgeStyles.DOTTED),
        BACK(EdgeStyles.SOLID),
        CROSS(EdgeStyles.DASHED);

        private final String style;

        EdgeType(String style) {
            this.style = style;
        }

        public String getStyle() {
            return style;
        }
    }

    public static class MyDFSVisitor<N, E> extends BaseDFSVisitor<N, E, Void> {

        private final Map<N, Integer> dfsNumbers = new HashMap<>();
        private final Map<E, EdgeType> edgeTypes = new HashMap<>();

        @Override
        public void explore(N node, Void data) {
            dfsNumbers.put(node, dfsNumbers.size());
        }

        @Override
        public Void treeEdge(N srcNode, Void srcData, E edge, N tgtNode) {
            edgeTypes.put(edge, EdgeType.TREE);
            return null;
        }

        @Override
        public void backEdge(N srcNode, Void srcData, E edge, N tgtNode, Void tgtData) {
            edgeTypes.put(edge, EdgeType.BACK);
        }

        @Override
        public void crossEdge(N srcNode, Void srcData, E edge, N tgtNode, Void tgtData) {
            edgeTypes.put(edge, EdgeType.CROSS);
        }

        @Override
        public void forwardEdge(N srcNode, Void srcData, E edge, N tgtNode, Void tgtData) {
            edgeTypes.put(edge, EdgeType.FORWARD);
        }

        public Map<N, Integer> getDfsNumbers() {
            return dfsNumbers;
        }

        public Map<E, EdgeType> getEdgeTypes() {
            return edgeTypes;
        }
    }

    public static class DFSResultDOTHelper<N, E> implements VisualizationHelper<N, E> {

        private final Map<N, Integer> dfsNumbers;
        private final Map<E, EdgeType> edgeTypes;

        public DFSResultDOTHelper(MyDFSVisitor<N, E> vis) {
            this(vis.getDfsNumbers(), vis.getEdgeTypes());
        }

        public DFSResultDOTHelper(Map<N, Integer> dfsNumbers, Map<E, EdgeType> edgeTypes) {
            this.dfsNumbers = dfsNumbers;
            this.edgeTypes = edgeTypes;
        }

        @Override
        public boolean getNodeProperties(N node, Map<String, String> properties) {
            String lbl = properties.get(NodeAttrs.LABEL);
            Integer dfsNum = dfsNumbers.get(node);
            assert dfsNum != null;
            properties.put(NodeAttrs.LABEL, lbl + " [#" + dfsNum + "]");
            return true;
        }

        @Override
        public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
            EdgeType et = edgeTypes.get(edge);
            assert et != null;
            properties.put(EdgeAttrs.STYLE, et.getStyle());
            properties.remove(EdgeAttrs.LABEL);
            return true;
        }
    }

}
