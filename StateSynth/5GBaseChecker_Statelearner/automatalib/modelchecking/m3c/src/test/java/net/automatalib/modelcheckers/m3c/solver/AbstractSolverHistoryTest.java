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
package net.automatalib.modelcheckers.m3c.solver;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.transformer.TransformerSerializer;
import net.automatalib.modelcheckers.m3c.util.Examples;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public abstract class AbstractSolverHistoryTest<T extends AbstractPropertyTransformer<T, String, String>> {

    protected final ContextFreeModalProcessSystem<String, String> cfmps;
    protected TransformerSerializer<T, String, String> serializer;

    public AbstractSolverHistoryTest() {
        this.cfmps = Examples.getCfmpsAnBn(Collections.emptySet());
    }

    @AfterClass
    public void after() {
        shutdownDDManager();
    }

    public abstract AbstractDDSolver<T, String, String> getSolver();

    public abstract <N> void testInitialPropertyTransformers(SolverData<N, T, String, String> data, N s1, N s2);

    public abstract void testMustTransformers(SolverHistory<T, String, String> history);

    public abstract void shutdownDDManager();

    @Test
    public void testSolverHistory() throws ParseException {
        final AbstractDDSolver<T, String, String> solver = getSolver();
        final FormulaNode<String, String> formula = M3CParser.parse("mu X.(<b><b>true || <>X)");
        final SolverHistory<T, String, String> history = solver.solveAndRecordHistory(formula);
        final Map<String, SolverData<?, T, String, String>> data = history.getData();

        Assert.assertEquals(data.size(), 1);

        final SolverData<?, T, String, String> solverData = data.get("P");

        Assert.assertNotNull(solverData);
        testSolverHistory(history, solverData);
    }

    private <N> void testSolverHistory(SolverHistory<T, String, String> history,
                                       SolverData<N, T, String, String> data) {
        final ProceduralModalProcessGraph<N, String, ?, String, ?> pmpg = data.getPmpg();
        final N initialNode = pmpg.getInitialNode();
        final N s1 = getS1(pmpg);
        final N s2 = getS2(pmpg, s1);

        testNodeIDs(data);
        testInitialPropertyTransformers(data, s1, s2);
        testInitialSatisifedSubformulas(data, s1, s2);
        testMustTransformers(history);
        testSolverStates(history, pmpg, initialNode, s1, s2);

        Assert.assertTrue(history.getMayTransformers(serializer).isEmpty());
        Assert.assertTrue(history.isSat());
    }

    private <N> void testSolverStates(SolverHistory<T, String, String> history,
                                      ProceduralModalProcessGraph<N, String, ?, String, ?> pmpg,
                                      N initialNode,
                                      N s1,
                                      N s2) {
        final List<N> updatedOrder =
                Arrays.asList(initialNode, s1, initialNode, s2, s1, initialNode, s1, initialNode, s1, initialNode, s1);
        Assert.assertEquals(history.getSolverStates().size(), updatedOrder.size());
        List<Set<N>> workSets = getWorkSetHistory(initialNode, s1, s2);
        boolean[] allAPDeadlockedNode = new boolean[5];
        allAPDeadlockedNode[3] = true;
        for (int i = 0; i < updatedOrder.size(); i++) {
            final N expectedNode = updatedOrder.get(i);
            final SolverState<?, T, String, String> solverState = history.getSolverStates().get(i);
            @SuppressWarnings("unchecked")
            final N actualNode = (N) solverState.getUpdatedNode();
            Assert.assertEquals(actualNode, expectedNode);
            Assert.assertEquals(pmpg.getOutgoingEdges(actualNode).size(),
                                solverState.getCompositions(serializer).size());
            Assert.assertEquals(solverState.getUpdatedNodePMPG(), cfmps.getMainProcess());
            Assert.assertEquals(solverState.getWorkSet().get(cfmps.getMainProcess()), workSets.get(i));
            testSatisfiedSubformulasAndUpdatedPT(allAPDeadlockedNode, solverState);
        }
    }

    protected <N> void testNodeIDs(SolverData<N, ?, String, String> data) {

        final ProceduralModalProcessGraph<N, String, ?, String, ?> pmpg = data.getPmpg();
        final NodeIDs<N> nodeIDs = data.getNodeIDs();

        // check that nodeIDs contains a mapping for each node in pmpg
        for (final N node : pmpg.getNodes()) {
            nodeIDs.getNodeId(node);
        }
    }

    protected <N> void testInitialSatisifedSubformulas(SolverData<N, ?, String, String> data, N s1, N s2) {

        final ProceduralModalProcessGraph<N, String, ?, String, ?> pmpg = data.getPmpg();
        final Mapping<N, List<FormulaNode<String, String>>> initialSatisfiedSubformulas =
                data.getInitialSatisfiedSubformulas();

        Assert.assertTrue(initialSatisfiedSubformulas.get(pmpg.getInitialNode()).isEmpty());
        Assert.assertTrue(initialSatisfiedSubformulas.get(s1).isEmpty());
        Assert.assertTrue(initialSatisfiedSubformulas.get(s2).isEmpty());

        final List<FormulaNode<String, String>> finalNodeSatisfiedSubformulas =
                initialSatisfiedSubformulas.get(pmpg.getFinalNode());
        Assert.assertEquals(finalNodeSatisfiedSubformulas.size(), 1);
        Assert.assertTrue(finalNodeSatisfiedSubformulas.get(0) instanceof TrueNode);
    }

    public void testSatisfiedSubformulasAndUpdatedPT(boolean[] allAPDeadlockedNode,
                                                     SolverState<?, T, String, String> solverState) {
        final BitSet actualSatisfiedSubformulas = solverState.getUpdatedNodeSatisfiedSubformula()
                                                             .stream()
                                                             .map(FormulaNode::getVarNumber)
                                                             .collect(BitSet::new, BitSet::set, BitSet::or);
        final T updatedPropertyTransformer = solverState.getUpdatedPropTransformer(serializer);
        BitSet expectedSatisfiedSubformulas = updatedPropertyTransformer.evaluate(allAPDeadlockedNode);
        Assert.assertEquals(actualSatisfiedSubformulas, expectedSatisfiedSubformulas);
    }

    protected <N> List<Set<N>> getWorkSetHistory(N initialNode, N s1, N s2) {
        final Set<N> workSetOnlyS1 = Collections.singleton(s1);
        final Set<N> workSetOnlyInitialNode = Collections.singleton(initialNode);

        return Arrays.asList(new HashSet<>(Arrays.asList(s1, s2)),
                             new HashSet<>(Arrays.asList(initialNode, s2)),
                             Collections.singleton(s2),
                             workSetOnlyS1,
                             workSetOnlyInitialNode,
                             workSetOnlyS1,
                             workSetOnlyInitialNode,
                             workSetOnlyS1,
                             workSetOnlyInitialNode,
                             workSetOnlyS1,
                             Collections.emptySet());
    }

    protected <N, E> N getS1(ProceduralModalProcessGraph<N, String, E, String, ?> pmpg) {
        final N initalNode = pmpg.getInitialNode();
        // the initial node is connected to s1 by an "a" labeled edge
        for (final E edge : pmpg.getOutgoingEdges(initalNode)) {
            if ("a".equals(pmpg.getEdgeLabel(edge))) {
                return pmpg.getTarget(edge);
            }
        }
        throw new IllegalStateException("unexpected modal process graph");
    }

    protected <N, E> N getS2(ProceduralModalProcessGraph<N, String, E, String, ?> pmpg, N s1) {
        // s1's only adjacent target is s2
        for (final N adjacentTarget : pmpg.getAdjacentTargets(s1)) {
            return adjacentTarget;
        }
        throw new IllegalStateException("unexpected modal process graph");
    }

}
