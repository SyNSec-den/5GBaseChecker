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
package net.automatalib.graphs;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.random.RandomUtil;
import net.automatalib.graphs.base.compact.CompactBidiGraph;
import net.automatalib.graphs.base.compact.CompactGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntAbstractionTest {

    private static final int SIZE = 25;
    private static final List<Character> NPS = CollectionsUtil.charRange('A', 'F');
    private static final List<Character> EPS = CollectionsUtil.charRange('0', '9');

    @Test
    public void testCompactGraphs() {

        final CompactGraph<Character, Character> graph = new CompactGraph<>();
        final CompactGraph<Character, Character> abstraction = new CompactGraph<>();

        fillGraphs(new Random(42), graph, abstraction, SIZE, NPS, EPS);
        checkGraphs(graph, abstraction);
    }

    @Test
    public void testCompactBidiGraphs() {

        final CompactBidiGraph<Character, Character> graph = new CompactBidiGraph<>();
        final CompactBidiGraph<Character, Character> abstraction = new CompactBidiGraph<>();

        fillGraphs(new Random(42), graph, abstraction, SIZE, NPS, EPS);
        checkGraphs(graph, abstraction);
        checkIncomingNodes(graph, abstraction);
    }

    private <N, E1, E2, NP, EP> void fillGraphs(Random random,
                                                MutableGraph<N, E1, NP, EP> graph,
                                                MutableGraph.IntAbstraction<E2, NP, EP> abstraction,
                                                int size,
                                                List<NP> nodeProperties,
                                                List<EP> edgeProperties) {

        for (int j = 0; j < size; j++) {
            final NP np = RandomUtil.choose(nodeProperties, random);

            graph.addNode(np);
            abstraction.addIntNode(np);
        }

        final NodeIDs<N> nodeIDs = graph.nodeIDs();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (random.nextBoolean()) { // connect
                    final EP ep = RandomUtil.choose(edgeProperties, random);

                    if (random.nextBoolean()) { // direct EP
                        final E1 ge = graph.connect(nodeIDs.getNode(i), nodeIDs.getNode(j));
                        graph.setEdgeProperty(ge, ep);

                        final E2 ae = abstraction.connect(i, j);
                        abstraction.setEdgeProperty(ae, ep);
                    } else {
                        graph.connect(nodeIDs.getNode(i), nodeIDs.getNode(j), ep);
                        abstraction.connect(i, j, ep);
                    }
                }
            }
        }

    }

    private <N, E1, E2, NP, EP> void checkGraphs(MutableGraph<N, E1, NP, EP> graph,
                                                 MutableGraph.IntAbstraction<E2, NP, EP> abstraction) {

        Assert.assertEquals(graph.size(), abstraction.size());

        final NodeIDs<N> nodeIDs = graph.nodeIDs();
        for (N n : graph) {
            final int id = nodeIDs.getNodeId(n);

            Assert.assertEquals(graph.getNodeProperty(n), abstraction.getNodeProperty(id));

            final Collection<E1> gEdges = graph.getOutgoingEdges(n);
            final Collection<E2> aEdges = abstraction.getOutgoingEdges(id);
            Assert.assertEquals(gEdges.size(), aEdges.size());

            final Set<N> gSuccs = gEdges.stream().map(graph::getTarget).collect(Collectors.toSet());
            final Set<N> aSuccs = aEdges.stream()
                                        .mapToInt(abstraction::getIntTarget)
                                        .mapToObj(nodeIDs::getNode)
                                        .collect(Collectors.toSet());
            Assert.assertEquals(gSuccs, aSuccs);

            final Set<EP> gProps = graph.outgoingEdgesStream(n).map(graph::getEdgeProperty).collect(Collectors.toSet());
            final Set<EP> aProps =
                    abstraction.outgoingEdgesStream(id).map(abstraction::getEdgeProperty).collect(Collectors.toSet());
            Assert.assertEquals(gProps, aProps);

            for (N n1 : graph) {
                final int id1 = nodeIDs.getNodeId(n1);
                Assert.assertEquals(graph.isConnected(n, n1), abstraction.isConnected(id, id1));

                final Collection<E1> gEdgesInBtwn = graph.getEdgesBetween(n, n1);
                final Collection<E2> aEdgesInBtwn = abstraction.getEdgesBetween(id, id1);
                Assert.assertEquals(gEdgesInBtwn.size(), aEdgesInBtwn.size());
            }
        }
    }

    private <N, E1, E2> void checkIncomingNodes(BidirectionalGraph<N, E1> graph,
                                                BidirectionalGraph.IntAbstraction<E2> abstraction) {

        final NodeIDs<N> nodeIDs = graph.nodeIDs();
        for (N n : graph) {
            final int id = nodeIDs.getNodeId(n);

            final Collection<E1> gEdges = graph.getIncomingEdges(n);
            final Collection<E2> aEdges = abstraction.getIncomingEdges(id);
            Assert.assertEquals(gEdges.size(), aEdges.size());

            final Set<N> gSuccs = gEdges.stream().map(graph::getSource).collect(Collectors.toSet());
            final Set<N> aSuccs = aEdges.stream()
                                        .mapToInt(abstraction::getIntSource)
                                        .mapToObj(nodeIDs::getNode)
                                        .collect(Collectors.toSet());
            Assert.assertEquals(gSuccs, aSuccs);
        }
    }

}
