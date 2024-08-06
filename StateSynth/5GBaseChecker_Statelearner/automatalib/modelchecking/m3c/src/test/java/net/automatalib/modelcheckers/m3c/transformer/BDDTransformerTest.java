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
package net.automatalib.modelcheckers.m3c.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AGNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BDDTransformerTest {

    private static DependencyGraph<String, String> dg;
    private static BDDManager bddManager;
    private static OrNode<String, String> orNode;
    private static DiamondNode<String, String> diaNode1;
    private static DiamondNode<String, String> diaNode2;
    private static BoxNode<String, String> boxNode;
    private static TrueNode<String, String> trueNode;

    @BeforeClass
    public static void setup() throws ParseException {
        final String formula = "mu X.(<b>[b]true || <>X)";
        dg = new DependencyGraph<>(M3CParser.parse(formula));
        bddManager = new BDDManager();
        final LfpNode<String, String> gfpNode = (LfpNode<String, String>) dg.getAST();
        orNode = (OrNode<String, String>) gfpNode.getChild();
        diaNode1 = (DiamondNode<String, String>) orNode.getLeftChild();
        diaNode2 = (DiamondNode<String, String>) orNode.getRightChild();
        boxNode = (BoxNode<String, String>) diaNode1.getChild();
        trueNode = (TrueNode<String, String>) boxNode.getChild();
    }

    @Test
    void testBDDIdentity() {
        BDDTransformer<String, String> transformer = new BDDTransformer<>(bddManager, dg.getNumVariables());
        for (int var = 0; var < transformer.getNumberOfVars(); var++) {
            Assert.assertEquals(transformer.getBDD(var), bddManager.ithVar(var));
        }
    }

    @Test
    void testBDDNodeInitialization() {
        BDDTransformer<String, String> transformer = new BDDTransformer<>(bddManager, dg);
        for (EquationalBlock<String, String> block : dg.getBlocks()) {
            for (FormulaNode<String, String> node : block.getNodes()) {
                BDD actual = transformer.getBDD(node.getVarNumber());
                BDD expected;
                if (block.isMaxBlock()) {
                    expected = bddManager.readOne();
                } else {
                    expected = bddManager.readLogicZero();
                }
                Assert.assertEquals(expected, actual);
            }
        }
    }

    @Test
    void testEdgeTransformerMust() {
        BDDTransformer<String, String> transformer =
                new BDDTransformer<>(bddManager, "b", new ModalEdgePropertyImpl(ModalType.MUST), dg);

        BDD bddOrNode = transformer.getBDD(orNode.getVarNumber());
        BDD expectedBDDOrNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDOrNode, bddOrNode);

        BDD bddDiaNode1 = transformer.getBDD(diaNode1.getVarNumber());
        BDD expectedBDDDiaNode1 = bddManager.ithVar(diaNode1.getVarNumberChild());
        Assert.assertEquals(expectedBDDDiaNode1, bddDiaNode1);

        BDD bddDiaNode2 = transformer.getBDD(diaNode2.getVarNumber());
        BDD expectedBDDDiaNode2 = bddManager.ithVar(diaNode2.getVarNumberChild());
        Assert.assertEquals(expectedBDDDiaNode2, bddDiaNode2);

        BDD bddBoxNode = transformer.getBDD(boxNode.getVarNumber());
        BDD expectedBDDBoxNode = bddManager.ithVar(boxNode.getVarNumberChild());
        Assert.assertEquals(expectedBDDBoxNode, bddBoxNode);

        BDD bddTrueNode = transformer.getBDD(trueNode.getVarNumber());
        BDD expectedBDDTrueNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDTrueNode, bddTrueNode);
    }

    @Test
    void testEdgeTransformerNoMatch() {
        BDDTransformer<String, String> transformer =
                new BDDTransformer<>(bddManager, "a", new ModalEdgePropertyImpl(ModalType.MUST), dg);

        BDD bddOrNode = transformer.getBDD(orNode.getVarNumber());
        BDD expectedBDDOrNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDOrNode, bddOrNode);

        BDD bddDiaNode1 = transformer.getBDD(diaNode1.getVarNumber());
        BDD expectedBDDDiaNode1 = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDDiaNode1, bddDiaNode1);

        BDD bddDiaNode2 = transformer.getBDD(diaNode2.getVarNumber());
        BDD expectedBDDDiaNode2 = bddManager.ithVar(diaNode2.getVarNumberChild());
        Assert.assertEquals(expectedBDDDiaNode2, bddDiaNode2);

        BDD bddBoxNode = transformer.getBDD(boxNode.getVarNumber());
        BDD expectedBDDBoxNode = bddManager.readOne();
        Assert.assertEquals(expectedBDDBoxNode, bddBoxNode);

        BDD bddTrueNode = transformer.getBDD(trueNode.getVarNumber());
        BDD expectedBDDTrueNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDTrueNode, bddTrueNode);
    }

    @Test
    void testEdgeTransformerMay() {
        BDDTransformer<String, String> transformer =
                new BDDTransformer<>(bddManager, "b", new ModalEdgePropertyImpl(ModalType.MAY), dg);

        BDD bddOrNode = transformer.getBDD(orNode.getVarNumber());
        BDD expectedBDDOrNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDOrNode, bddOrNode);

        BDD bddDiaNode1 = transformer.getBDD(diaNode1.getVarNumber());
        BDD expectedBDDDiaNode1 = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDDiaNode1, bddDiaNode1);

        BDD bddDiaNode2 = transformer.getBDD(diaNode2.getVarNumber());
        BDD expectedBDDDiaNode2 = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDDiaNode2, bddDiaNode2);

        BDD bddBoxNode = transformer.getBDD(boxNode.getVarNumber());
        BDD expectedBDDBoxNode = bddManager.ithVar(boxNode.getVarNumberChild());
        Assert.assertEquals(expectedBDDBoxNode, bddBoxNode);

        BDD bddTrueNode = transformer.getBDD(trueNode.getVarNumber());
        BDD expectedBDDTrueNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDTrueNode, bddTrueNode);
    }

    @Test
    void testComposition() {
        BDDTransformer<String, String> transformer = new BDDTransformer<>(bddManager, dg);
        BDDTransformer<String, String> identity = new BDDTransformer<>(bddManager, dg.getNumVariables());
        BDDTransformer<String, String> composition = transformer.compose(identity);
        Assert.assertEquals(5, composition.getNumberOfVars());
        Assert.assertEquals(transformer, composition);

        BDDTransformer<String, String> inverseComposition = identity.compose(transformer);
        Assert.assertEquals(transformer, inverseComposition);
    }

    @Test
    void testOrBDDListOnes() {
        BDDTransformer<String, String> edgeTransformer =
                new BDDTransformer<>(bddManager, "b", new ModalEdgePropertyImpl(ModalType.MUST), dg);
        BDD[] oneBDDs = new BDD[dg.getNumVariables()];
        for (int var = 0; var < oneBDDs.length; var++) {
            oneBDDs[var] = bddManager.readOne();
        }
        BDDTransformer<String, String> oneTransformer = new BDDTransformer<>(bddManager, oneBDDs);

        List<BDDTransformer<String, String>> comps = new ArrayList<>();
        comps.add(edgeTransformer);
        comps.add(oneTransformer);

        BDD disjunction = edgeTransformer.orBddList(comps, diaNode1.getVarNumber());
        Assert.assertEquals(bddManager.readOne(), disjunction);
    }

    @Test
    void testOrBDDListZeros() {
        BDDTransformer<String, String> edgeTransformer =
                new BDDTransformer<>(bddManager, "b", new ModalEdgePropertyImpl(ModalType.MUST), dg);
        BDD[] oneBDDs = new BDD[dg.getNumVariables()];
        for (int var = 0; var < oneBDDs.length; var++) {
            oneBDDs[var] = bddManager.readLogicZero();
        }
        BDDTransformer<String, String> oneTransformer = new BDDTransformer<>(bddManager, oneBDDs);

        List<BDDTransformer<String, String>> comps = new ArrayList<>();
        comps.add(edgeTransformer);
        comps.add(oneTransformer);
        BDD disjunction = edgeTransformer.orBddList(comps, diaNode1.getVarNumber());
        Assert.assertEquals(bddManager.ithVar(diaNode1.getVarNumberChild()), disjunction);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUpdateException() throws ParseException {
        final String formulaWithNegatedAP = "mu X.(<b><b>!'a' || <>X)";
        DependencyGraph<String, String> dependencyGraph = new DependencyGraph<>(M3CParser.parse(formulaWithNegatedAP));
        BDDTransformer<String, String> transformer = new BDDTransformer<>(bddManager, dependencyGraph);
        Set<String> atomicPropositions = new HashSet<>();
        atomicPropositions.add("a");
        EquationalBlock<String, String> block = new EquationalBlock<>(false);
        block.addNode(new AGNode<>(new TrueNode<>()));
        transformer.createUpdate(atomicPropositions, Collections.emptyList(), block);
    }

}
