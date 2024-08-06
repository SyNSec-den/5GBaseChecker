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

import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVector;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVectorLogicDDManager;
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
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ADDTransformerTest {

    private static DependencyGraph<String, String> dg;
    private static BooleanVectorLogicDDManager xddManager;
    private static OrNode<String, String> orNode;
    private static DiamondNode<String, String> diaNode1;
    private static DiamondNode<String, String> diaNode2;
    private static BoxNode<String, String> boxNode;
    private static TrueNode<String, String> trueNode;

    @BeforeClass
    public static void setup() throws ParseException {
        String formula = "mu X.(<b>[b]true || <>X)";
        dg = new DependencyGraph<>(M3CParser.parse(formula));
        xddManager = new BooleanVectorLogicDDManager(dg.getNumVariables());
        final LfpNode<String, String> gfpNode = (LfpNode<String, String>) dg.getAST();
        orNode = (OrNode<String, String>) gfpNode.getChild();
        diaNode1 = (DiamondNode<String, String>) orNode.getLeftChild();
        diaNode2 = (DiamondNode<String, String>) orNode.getRightChild();
        boxNode = (BoxNode<String, String>) diaNode1.getChild();
        trueNode = (TrueNode<String, String>) boxNode.getChild();
    }

    @Test
    void testADDIdentity() {
        ADDTransformer<String, String> transformer = new ADDTransformer<>(xddManager);
        double numVarCombinations = Math.pow(2, dg.getNumVariables());

        /* Check output of each possible input */
        for (int i = 0; i < numVarCombinations; i++) {

            /* Construct boolean input vector from BitSet */
            BitSet bs = BitSet.valueOf(new long[] {i});
            boolean[] input = new boolean[dg.getNumVariables()];
            for (int idx = 0; idx < dg.getNumVariables(); idx++) {
                input[idx] = bs.get(idx);
            }

            /* Test correctness of identity by checking if input equals output */
            BitSet satisfiedVars = transformer.evaluate(input);
            for (int idx = 0; i < input.length; i++) {
                if (input[idx]) {
                    Assert.assertTrue(satisfiedVars.get(idx));
                }
                if (satisfiedVars.get(idx)) {
                    Assert.assertTrue(input[idx]);
                }
            }
        }
    }

    @Test
    void testADDNodeInitialization() {
        ADDTransformer<String, String> transformer = new ADDTransformer<>(xddManager, dg);
        XDD<BooleanVector> add = transformer.getAdd();
        Assert.assertNotNull(add);
        Assert.assertTrue(add.isConstant());
        boolean[] leafData = add.v().data();
        for (EquationalBlock<String, String> block : dg.getBlocks()) {
            for (FormulaNode<String, String> node : block.getNodes()) {
                boolean val = leafData[node.getVarNumber()];
                boolean isMaxBlock = block.isMaxBlock();
                Assert.assertEquals(isMaxBlock, val);
            }
        }
    }

    @Test
    void testEdgeTransformerMust() {
        ADDTransformer<String, String> transformer =
                new ADDTransformer<>(xddManager, "b", new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST), dg);
        double numVarCombinations = Math.pow(2, dg.getNumVariables());

        for (int i = 0; i < numVarCombinations; i++) {

            /* Construct boolean  input vector from BitSet */
            BitSet bs = BitSet.valueOf(new long[] {i});
            boolean[] input = new boolean[dg.getNumVariables()];
            for (int idx = 0; idx < dg.getNumVariables(); idx++) {
                input[idx] = bs.get(idx);
            }

            BitSet satisfiedVars = transformer.evaluate(input);
            Assert.assertFalse(satisfiedVars.get(orNode.getVarNumber()));

            boolean diaNode1ExpectedTrue = input[diaNode1.getVarNumberChild()];
            boolean diaNode1ActualTrue = satisfiedVars.get(diaNode1.getVarNumber());
            Assert.assertEquals(diaNode1ExpectedTrue, diaNode1ActualTrue);

            boolean diaNode2ExpectedTrue = input[diaNode2.getVarNumberChild()];
            boolean diaNode2ActualTrue = satisfiedVars.get(diaNode2.getVarNumber());
            Assert.assertEquals(diaNode2ExpectedTrue, diaNode2ActualTrue);

            boolean boxNodeExpectedTrue = input[boxNode.getVarNumberChild()];
            boolean boxNodeActualTrue = satisfiedVars.get(boxNode.getVarNumber());
            Assert.assertEquals(boxNodeExpectedTrue, boxNodeActualTrue);

            Assert.assertFalse(satisfiedVars.get(trueNode.getVarNumber()));
        }
    }

    @Test
    void testEdgeTransformerNoMatch() {
        ADDTransformer<String, String> transformer =
                new ADDTransformer<>(xddManager, "a", new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST), dg);
        double numVarCombinations = Math.pow(2, dg.getNumVariables());
        for (int i = 0; i < numVarCombinations; i++) {

            /* Construct boolean  input vector from BitSet */
            BitSet bs = BitSet.valueOf(new long[] {i});
            boolean[] input = new boolean[dg.getNumVariables()];
            for (int idx = 0; idx < dg.getNumVariables(); idx++) {
                input[idx] = bs.get(idx);
            }

            BitSet satisfiedVars = transformer.evaluate(input);
            Assert.assertFalse(satisfiedVars.get(orNode.getVarNumber()));

            Assert.assertFalse(satisfiedVars.get(diaNode1.getVarNumber()));

            boolean diaNode2ExpectedTrue = input[diaNode2.getVarNumberChild()];
            boolean diaNode2ActualTrue = satisfiedVars.get(diaNode2.getVarNumber());
            Assert.assertEquals(diaNode2ExpectedTrue, diaNode2ActualTrue);

            Assert.assertTrue(satisfiedVars.get(boxNode.getVarNumber()));

            Assert.assertFalse(satisfiedVars.get(trueNode.getVarNumber()));
        }
    }

    @Test
    void testEdgeTransformerMay() {
        ADDTransformer<String, String> transformer =
                new ADDTransformer<>(xddManager, "b", new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY), dg);
        double numVarCombinations = Math.pow(2, dg.getNumVariables());
        for (int i = 0; i < numVarCombinations; i++) {

            /* Construct boolean  input vector from BitSet */
            BitSet bs = BitSet.valueOf(new long[] {i});
            boolean[] input = new boolean[dg.getNumVariables()];
            for (int idx = 0; idx < dg.getNumVariables(); idx++) {
                input[idx] = bs.get(idx);
            }

            BitSet satisfiedVars = transformer.evaluate(input);
            Assert.assertFalse(satisfiedVars.get(orNode.getVarNumber()));

            Assert.assertFalse(satisfiedVars.get(diaNode1.getVarNumber()));

            Assert.assertFalse(satisfiedVars.get(diaNode2.getVarNumber()));

            boolean boxNodeExpectedTrue = input[boxNode.getVarNumberChild()];
            boolean boxNodeActualTrue = satisfiedVars.get(boxNode.getVarNumber());
            Assert.assertEquals(boxNodeExpectedTrue, boxNodeActualTrue);

            Assert.assertFalse(satisfiedVars.get(trueNode.getVarNumber()));
        }
    }

    @Test
    void testComposition() {
        ADDTransformer<String, String> transformer = new ADDTransformer<>(xddManager, dg);
        ADDTransformer<String, String> identity = new ADDTransformer<>(xddManager);
        ADDTransformer<String, String> composition = transformer.compose(identity);
        Assert.assertEquals(transformer, composition);

        ADDTransformer<String, String> inverseComposition = identity.compose(transformer);
        Assert.assertEquals(transformer, inverseComposition);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    void testCompositionWithTwoIdentities() {
        ADDTransformer<String, String> identity = new ADDTransformer<>(xddManager);
        ADDTransformer<String, String> anotherIdentity = new ADDTransformer<>(xddManager);
        identity.compose(anotherIdentity);
    }

    @Test
    void testUpdate() throws ParseException {
        final String formulaWithNegatedAP = "mu X.(<b><b>!'a' || <>X)";
        DependencyGraph<String, String> dependencyGraph = new DependencyGraph<>(M3CParser.parse(formulaWithNegatedAP));
        ADDTransformer<String, String> transformer = new ADDTransformer<>(xddManager, dependencyGraph);
        Set<String> atomicPropositions = new HashSet<>();
        atomicPropositions.add("a");
        ADDTransformer<String, String> updatedTransformer =
                transformer.createUpdate(atomicPropositions, Collections.emptyList(), dependencyGraph.getBlock(0));
        XDD<BooleanVector> add = updatedTransformer.getAdd();
        Assert.assertNotNull(add);
        add.monadicApply(vector -> {
            boolean[] result = vector.data();
            Assert.assertFalse(result[0]);
            Assert.assertFalse(result[1]);
            Assert.assertFalse(result[2]);
            Assert.assertFalse(result[3]);
            Assert.assertTrue(result[4]);
            Assert.assertFalse(result[5]);
            return new BooleanVector(result);
        });
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void testUpdateDeadlockException() throws ParseException {
        final String formulaWithNegatedAP = "mu X.(<b><b>!'a' || <>X)";
        DependencyGraph<String, String> dependencyGraph = new DependencyGraph<>(M3CParser.parse(formulaWithNegatedAP));
        ADDTransformer<String, String> transformer = new ADDTransformer<>(xddManager, dependencyGraph);
        Set<String> atomicPropositions = new HashSet<>();
        atomicPropositions.add("a");
        EquationalBlock<String, String> block = new EquationalBlock<>(false);
        block.addNode(new AGNode<>(new TrueNode<>()));
        transformer.createUpdate(atomicPropositions, Collections.emptyList(), block);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void testUpdateException() throws ParseException {
        final String formulaWithNegatedAP = "mu X.(<b><b>!'a' || <>X)";
        DependencyGraph<String, String> dependencyGraph = new DependencyGraph<>(M3CParser.parse(formulaWithNegatedAP));
        ADDTransformer<String, String> transformer = new ADDTransformer<>(xddManager, dependencyGraph);
        Set<String> atomicPropositions = new HashSet<>();
        atomicPropositions.add("a");
        EquationalBlock<String, String> block = new EquationalBlock<>(false);
        block.addNode(new AGNode<>(new TrueNode<>()));
        transformer.createUpdate(atomicPropositions, Collections.singletonList(transformer), block);
    }

}
