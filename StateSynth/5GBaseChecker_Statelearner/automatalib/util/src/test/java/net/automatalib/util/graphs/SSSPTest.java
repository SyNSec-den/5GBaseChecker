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
package net.automatalib.util.graphs;

import java.util.Arrays;
import java.util.List;

import net.automatalib.graphs.base.compact.CompactEdge;
import net.automatalib.graphs.base.compact.CompactSimpleGraph;
import net.automatalib.graphs.concepts.EdgeWeights;
import net.automatalib.util.graphs.concepts.PropertyEdgeWeights;
import net.automatalib.util.graphs.sssp.SSSPResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class SSSPTest {

    Integer n0, n1, n2, n3, n4, n5;
    private CompactSimpleGraph<Float> graph;
    private EdgeWeights<CompactEdge<Float>> weights;

    @BeforeClass
    public void setUp() {
        graph = new CompactSimpleGraph<>();

        n0 = graph.addNode();
        n1 = graph.addNode();
        n2 = graph.addNode();
        n3 = graph.addNode();
        n4 = graph.addNode();
        n5 = graph.addNode();

        graph.connect(n0, n1, Float.valueOf(2.3f));
        graph.connect(n1, n2, Float.valueOf(3.0f));
        graph.connect(n0, n2, Float.valueOf(6.0f));
        graph.connect(n2, n3, Float.valueOf(10.0f));
        graph.connect(n1, n3, Float.valueOf(7.0f));
        graph.connect(n2, n1, Float.valueOf(1.0f));
        graph.connect(n3, n4, Float.valueOf(1.0f));
        graph.connect(n2, n4, Float.valueOf(5.1f));
        graph.connect(n4, n1, Float.valueOf(10.0f));
        graph.connect(n5, n1, Float.valueOf(0.1f));

        this.weights = new PropertyEdgeWeights<>(graph);
    }

    @Test
    public void testSSSP() {
        SSSPResult<Integer, CompactEdge<Float>> sssp = Graphs.findSSSP(graph, n0, weights);

        assertSPDist(sssp, n1, 2.3f); // n0 -> n1
        assertSPNodes(sssp, n1, n0, n1);
        assertSPDist(sssp, n2, 5.3f); // n0 -> n1 -> n2
        assertSPNodes(sssp, n2, n0, n1, n2);
        assertSPDist(sssp, n3, 9.3f); // n0 -> n1 -> n3
        assertSPNodes(sssp, n3, n0, n1, n3);
        assertSPDist(sssp, n4, 10.3f); // n0 -> n1 -> n3 -> n4
        assertSPNodes(sssp, n4, n0, n1, n3, n4);
        assertSPDist(sssp, n5, Graphs.INVALID_DISTANCE);
    }

    private static <N> void assertSPDist(SSSPResult<N, ?> res, N tgt, float dist) {
        Assert.assertEquals(res.getShortestPathDistance(tgt), dist);
    }

    private void assertSPNodes(SSSPResult<Integer, CompactEdge<Float>> res, Integer tgt, Integer... expNodes) {
        List<Integer> nodes = Graphs.toNodeList(res.getShortestPath(tgt), graph, res.getInitialNode());
        Assert.assertEquals(nodes, Arrays.asList(expNodes));
    }
}
