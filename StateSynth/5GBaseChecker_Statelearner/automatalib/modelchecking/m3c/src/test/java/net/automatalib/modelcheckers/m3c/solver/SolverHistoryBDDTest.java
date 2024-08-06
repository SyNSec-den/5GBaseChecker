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

import java.util.Map;

import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.modelcheckers.m3c.transformer.BDDTransformer;
import net.automatalib.modelcheckers.m3c.transformer.BDDTransformerSerializer;
import org.testng.Assert;

public class SolverHistoryBDDTest extends AbstractSolverHistoryTest<BDDTransformer<String, String>> {

    private final BDDManager bddManager;

    public SolverHistoryBDDTest() {
        this.bddManager = new BDDManager();
        this.serializer = new BDDTransformerSerializer<>(bddManager);
    }

    @Override
    public AbstractDDSolver<BDDTransformer<String, String>, String, String> getSolver() {
        return new BDDSolver<>(cfmps);
    }

    @Override
    public void shutdownDDManager() {
        bddManager.quit();
    }

    @Override
    public <N> void testInitialPropertyTransformers(SolverData<N, BDDTransformer<String, String>, String, String> data,
                                                    N s1,
                                                    N s2) {

        final ProceduralModalProcessGraph<N, String, ?, String, ?> pmpg = data.getPmpg();
        final Mapping<N, BDDTransformer<String, String>> initialPropertyTransformers =
                data.getInitialPropertyTransformers(serializer);

        final BDDTransformer<String, String> startPT = initialPropertyTransformers.get(pmpg.getInitialNode());
        final BDDTransformer<String, String> endPT = initialPropertyTransformers.get(pmpg.getFinalNode());
        final BDDTransformer<String, String> s1PT = initialPropertyTransformers.get(s1);
        final BDDTransformer<String, String> s2PT = initialPropertyTransformers.get(s2);

        // the PTs of start, s1 and s2 are initialized with an array of zero BDDs
        Assert.assertEquals(startPT, s1PT);
        Assert.assertEquals(startPT, s2PT);
        Assert.assertEquals(s1PT, s2PT);
        // the end node PT is initialized with the identity function
        Assert.assertNotEquals(startPT, endPT);

        final BDD zeroDD = bddManager.readLogicZero();
        for (int i = 0; i < startPT.getNumberOfVars(); i++) {
            Assert.assertEquals(startPT.getBDD(i), zeroDD);
        }
        for (int i = 0; i < endPT.getNumberOfVars(); i++) {
            BDD ithDD = endPT.getBDD(i);
            Assert.assertEquals(ithDD, bddManager.ithVar(i));
        }
    }

    @Override
    public void testMustTransformers(SolverHistory<BDDTransformer<String, String>, String, String> history) {
        final Map<String, BDDTransformer<String, String>> mustTransformers = history.getMustTransformers(serializer);
        Assert.assertEquals(mustTransformers.size(), 3);
        final BDDTransformer<String, String> aPT = mustTransformers.get("a");
        final BDDTransformer<String, String> bPT = mustTransformers.get("b");
        final BDDTransformer<String, String> ePT = mustTransformers.get("e");
        Assert.assertEquals(aPT, ePT);
        Assert.assertNotEquals(aPT, bPT);

        for (int i = 0; i < aPT.getNumberOfVars(); i++) {
            final BDD aDD = aPT.getBDD(i);
            if (i == 4) {
                Assert.assertEquals(aDD, bddManager.ithVar(0));
            } else {
                Assert.assertEquals(aDD, bddManager.readLogicZero());
            }
        }

        for (int i = 0; i < bPT.getNumberOfVars(); i++) {
            final BDD bDD = bPT.getBDD(i);
            if (i == 1 || i == 2) {
                Assert.assertEquals(bDD, bddManager.ithVar(i + 1));
            } else if (i == 4) {
                Assert.assertEquals(bDD, bddManager.ithVar(0));
            } else {
                Assert.assertEquals(bDD, bddManager.readLogicZero());
            }
        }
    }

}
