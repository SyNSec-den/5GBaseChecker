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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import net.automatalib.graphs.base.compact.CompactSimpleGraph;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Malte Isberner
 * @author Malte Mues
 */
public class SCCTest {

    @Test
    public void testExample1() {

        Integer n0, n1, n2, n3;
        CompactSimpleGraph<Void> graph;
        graph = new CompactSimpleGraph<>();

        n0 = graph.addNode();
        n1 = graph.addNode();
        n2 = graph.addNode();
        n3 = graph.addNode();

        graph.connect(n0, n1);
        graph.connect(n1, n0);
        graph.connect(n1, n2);
        graph.connect(n2, n1);
        graph.connect(n2, n3);

        @SuppressWarnings("unchecked")
        Set<Set<Integer>> expectedSCCs = Sets.newHashSet(Sets.newHashSet(0, 1, 2), Sets.newHashSet(3));

        Set<Set<Integer>> computedSCCs = computeSCCs(graph);

        Assert.assertEquals(computedSCCs.size(), 2);
        Assert.assertEquals(computedSCCs, expectedSCCs);
    }

    @Test
    public void testExample2() {
        CompactSimpleGraph<Void> graph = new CompactSimpleGraph<>();
        Integer n0, n1, n2, n3, n4;

        n0 = graph.addNode();
        n1 = graph.addNode();
        n2 = graph.addNode();
        n3 = graph.addNode();
        n4 = graph.addNode();

        graph.connect(n0, n1);
        graph.connect(n1, n2);
        graph.connect(n2, n3);
        graph.connect(n3, n1);
        graph.connect(n2, n4);

        @SuppressWarnings("unchecked")
        Set<Set<Integer>> expectedSCCs =
                Sets.newHashSet(Sets.newHashSet(n0), Sets.newHashSet(n1, n2, n3), Sets.newHashSet(n4));

        Set<Set<Integer>> computedSCCs = computeSCCs(graph);

        Assert.assertEquals(computedSCCs.size(), 3);
        Assert.assertEquals(computedSCCs, expectedSCCs);
    }

    /*
     * This example is taken from the following slide deck:
     * https://cs.nyu.edu/courses/spring17/CSCI-UA.0310-001/graphs-scc.pdf
     */
    @Test
    public void testExample3() {
        CompactSimpleGraph<Void> graph = new CompactSimpleGraph<>();
        Integer a, b, c, d, e, f, g, h;
        a = graph.addNode();
        b = graph.addNode();
        c = graph.addNode();
        d = graph.addNode();
        e = graph.addNode();
        f = graph.addNode();
        g = graph.addNode();
        h = graph.addNode();

        graph.connect(a, b);
        graph.connect(b, c);
        graph.connect(b, f);
        graph.connect(b, e);
        graph.connect(c, d);
        graph.connect(c, g);
        graph.connect(d, c);
        graph.connect(d, h);
        graph.connect(e, a);
        graph.connect(e, f);
        graph.connect(f, g);
        graph.connect(g, f);
        graph.connect(g, h);

        @SuppressWarnings("unchecked")
        Set<Set<Integer>> expectedSCCs = Sets.newHashSet(Sets.newHashSet(a, b, e),
                                                         Sets.newHashSet(c, d),
                                                         Sets.newHashSet(h),
                                                         Sets.newHashSet(f, g));

        Set<Set<Integer>> computedSCCs = computeSCCs(graph);

        Assert.assertEquals(computedSCCs.size(), 4);
        Assert.assertEquals(computedSCCs, expectedSCCs);
    }

    /*
     * This example is taken from the following slide deck:
     * http://www.cse.cuhk.edu.hk/~taoyf/course/comp3506/lec/scc.pdf
     */
    @Test
    public void testExample4() {
        CompactSimpleGraph<Void> graph = new CompactSimpleGraph<>();
        Integer a, b, c, d, e, f, g, h, i, j, k, l;
        a = graph.addNode();
        b = graph.addNode();
        c = graph.addNode();
        d = graph.addNode();
        e = graph.addNode();
        f = graph.addNode();
        g = graph.addNode();
        h = graph.addNode();
        i = graph.addNode();
        j = graph.addNode();
        k = graph.addNode();
        l = graph.addNode();

        graph.connect(a, c);
        graph.connect(b, a);
        graph.connect(c, b);
        graph.connect(d, b);
        graph.connect(d, e);
        graph.connect(e, a);
        graph.connect(e, f);
        graph.connect(e, g);
        graph.connect(f, d);
        graph.connect(f, k);
        graph.connect(k, l);
        graph.connect(l, f);
        graph.connect(g, d);
        graph.connect(j, e);
        graph.connect(j, g);
        graph.connect(j, j);
        graph.connect(j, h);
        graph.connect(h, i);
        graph.connect(i, h);
        graph.connect(i, g);

        @SuppressWarnings("unchecked")
        Set<Set<Integer>> expectedSCCs = Sets.newHashSet(Sets.newHashSet(a, b, c),
                                                         Sets.newHashSet(d, e, f, g, l, k),
                                                         Sets.newHashSet(i, h),
                                                         Sets.newHashSet(j));

        Set<Set<Integer>> computedSCCs = computeSCCs(graph);

        Assert.assertEquals(computedSCCs.size(), 4);
        Assert.assertEquals(computedSCCs, expectedSCCs);
    }

    @Test
    public void testExample5() {
        CompactSimpleGraph<Void> graph = new CompactSimpleGraph<>();
        Integer n0, n1, n2, n3, n4, n5, n6, n7, n8, n9;

        n0 = graph.addNode();
        n1 = graph.addNode();
        n2 = graph.addNode();
        n3 = graph.addNode();
        n4 = graph.addNode();
        n5 = graph.addNode();
        n6 = graph.addNode();
        n7 = graph.addNode();
        n8 = graph.addNode();
        n9 = graph.addNode();

        graph.connect(n5, n7);
        graph.connect(n0, n8);
        graph.connect(n9, n4);
        graph.connect(n9, n7);
        graph.connect(n1, n5);
        graph.connect(n8, n4);
        graph.connect(n8, n2);
        graph.connect(n5, n9);
        graph.connect(n8, n7);
        graph.connect(n5, n6);
        graph.connect(n4, n7);
        graph.connect(n0, n5);
        graph.connect(n6, n8);
        graph.connect(n1, n6);
        graph.connect(n3, n9);
        graph.connect(n4, n6);
        graph.connect(n9, n5);
        graph.connect(n2, n7);
        graph.connect(n1, n0);
        graph.connect(n2, n0);

        @SuppressWarnings("unchecked")
        Set<Set<Integer>> expectedSCCs =
                Sets.newHashSet(Sets.newHashSet(n3), Sets.newHashSet(n7),
                                Sets.newHashSet(n1), Sets.newHashSet(n0, n2, n4, n5, n6, n8, n9));

        Set<Set<Integer>> computedSCCs = computeSCCs(graph);

        /*
         * An invariant of this algorithm is that every node in the input graph should be maintained in the result set.
         */
        Set<Integer> nodeSet = Sets.newHashSet(n0, n1, n2, n3, n4, n5, n6, n7, n8, n9);
        computedSCCs.forEach(nodeSet::removeAll);
        Assert.assertEquals(nodeSet.size(), 0);

        Assert.assertEquals(computedSCCs, expectedSCCs);
        Assert.assertEquals(computedSCCs.size(), 4);
    }

    /*
     * The following example is taken from Trajans original paper
     * about his SCC-algorithm.
     * TARJAN, Robert. Depth-first search and linear graph algorithms.
     * SIAM journal on computing, 1972, 1. Jg., Nr. 2, S. 146-160.
     */
    @Test
    public void testTarjansSCCPaperExample() {
        CompactSimpleGraph<Void> graph = new CompactSimpleGraph<>();
        Integer n1, n2, n3, n4, n5, n6, n7, n8;

        n1 = graph.addNode();
        n2 = graph.addNode();
        n3 = graph.addNode();
        n4 = graph.addNode();
        n5 = graph.addNode();
        n6 = graph.addNode();
        n7 = graph.addNode();
        n8 = graph.addNode();

        graph.connect(n1, n2);
        graph.connect(n2, n3);
        graph.connect(n2, n8);
        graph.connect(n3, n4);
        graph.connect(n3, n7);
        graph.connect(n4, n5);
        graph.connect(n5, n3);
        graph.connect(n5, n6);
        graph.connect(n7, n6);
        graph.connect(n7, n4);
        graph.connect(n8, n1);
        graph.connect(n8, n7);

        @SuppressWarnings("unchecked")
        Set<Set<Integer>> expectedSCCs =
                Sets.newHashSet(Sets.newHashSet(n1, n2, n8), Sets.newHashSet(n6), Sets.newHashSet(n3, n4, n5, n7));

        Set<Set<Integer>> computedSCCs = computeSCCs(graph);

        Assert.assertEquals(computedSCCs.size(), 3);
        Assert.assertEquals(computedSCCs, expectedSCCs);
    }

    private Set<Set<Integer>> computeSCCs(CompactSimpleGraph<Void> graph) {
        return Graphs.collectSCCs(graph).stream().map(HashSet::new).collect(Collectors.toSet());
    }
}
