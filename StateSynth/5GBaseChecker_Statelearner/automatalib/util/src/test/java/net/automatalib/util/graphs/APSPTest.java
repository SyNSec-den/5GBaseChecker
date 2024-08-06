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
import net.automatalib.util.graphs.apsp.APSPResult;
import net.automatalib.util.graphs.concepts.PropertyEdgeWeights;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class APSPTest {

    Integer n0, n1, n2, n3, n4;
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

        graph.connect(n0, n1, Float.valueOf(2.3f));
        graph.connect(n1, n2, Float.valueOf(3.0f));
        graph.connect(n0, n2, Float.valueOf(6.0f));
        graph.connect(n2, n3, Float.valueOf(10.0f));
        graph.connect(n1, n3, Float.valueOf(7.0f));
        graph.connect(n2, n1, Float.valueOf(1.0f));
        graph.connect(n3, n4, Float.valueOf(1.0f));
        graph.connect(n2, n4, Float.valueOf(5.1f));
        graph.connect(n4, n1, Float.valueOf(10.0f));

        this.weights = new PropertyEdgeWeights<>(graph);
    }

    @Test
    public void testAPSP() {
        APSPResult<Integer, CompactEdge<Float>> apsp = Graphs.findAPSP(graph, weights);

        assertSPDist(apsp, n0, n1, 2.3f); // n0 -> n1
        assertSPNodes(apsp, n0, n1, n0, n1);
        assertSPDist(apsp, n0, n2, 5.3f); // n0 -> n1 -> n2
        assertSPNodes(apsp, n0, n2, n0, n1, n2);
        assertSPDist(apsp, n0, n3, 9.3f); // n0 -> n1 -> n3
        assertSPNodes(apsp, n0, n3, n0, n1, n3);
        assertSPDist(apsp, n0, n4, 10.3f); // n0 -> n1 -> n3 -> n4
        assertSPNodes(apsp, n0, n4, n0, n1, n3, n4);

        assertSPDist(apsp, n1, n0, Graphs.INVALID_DISTANCE);
        assertSPDist(apsp, n1, n2, 3.0f); // n1 -> n2
        assertSPNodes(apsp, n1, n2, n1, n2);
        assertSPDist(apsp, n1, n3, 7.0f); // n1 -> n3
        assertSPNodes(apsp, n1, n3, n1, n3);
        assertSPDist(apsp, n1, n4, 8.0f); // n1 -> n3 -> n4
        assertSPNodes(apsp, n1, n4, n1, n3, n4);

        assertSPDist(apsp, n2, n0, Graphs.INVALID_DISTANCE);
        assertSPDist(apsp, n2, n1, 1.0f); // n2 -> n1
        assertSPNodes(apsp, n2, n1, n2, n1);
        assertSPDist(apsp, n2, n3, 8.0f); // n2 -> n1 -> n3
        assertSPNodes(apsp, n2, n3, n2, n1, n3);
        assertSPDist(apsp, n2, n4, 5.1f); // n2 -> n4
        assertSPNodes(apsp, n2, n4, n2, n4);

        assertSPDist(apsp, n3, n0, Graphs.INVALID_DISTANCE);
        assertSPDist(apsp, n3, n1, 11.0f); // n3 -> n4 -> n1
        assertSPNodes(apsp, n3, n1, n3, n4, n1);
        assertSPDist(apsp, n3, n2, 14.0f); // n3 -> n4 -> n1 -> n2
        assertSPNodes(apsp, n3, n2, n3, n4, n1, n2);
        assertSPDist(apsp, n3, n4, 1.0f); // n3 -> n4
        assertSPNodes(apsp, n3, n4, n3, n4);

        assertSPDist(apsp, n4, n0, Graphs.INVALID_DISTANCE);
        assertSPDist(apsp, n4, n1, 10.0f); // n4 -> n1
        assertSPNodes(apsp, n4, n1, n4, n1);
        assertSPDist(apsp, n4, n2, 13.0f); // n4 -> n1 -> n2
        assertSPNodes(apsp, n4, n2, n4, n1, n2);
        assertSPDist(apsp, n4, n3, 17.0f); // n4 -> n1 -> n3
        assertSPNodes(apsp, n4, n3, n4, n1, n3);
    }

    private static <N> void assertSPDist(APSPResult<N, ?> res, N src, N tgt, float dist) {
        Assert.assertEquals(res.getShortestPathDistance(src, tgt), dist);
    }

    private void assertSPNodes(APSPResult<Integer, CompactEdge<Float>> res,
                               Integer src,
                               Integer tgt,
                               Integer... expNodes) {
        List<Integer> nodes = Graphs.toNodeList(res.getShortestPath(src, tgt), graph, src);
        Assert.assertEquals(nodes, Arrays.asList(expNodes));
    }

}
