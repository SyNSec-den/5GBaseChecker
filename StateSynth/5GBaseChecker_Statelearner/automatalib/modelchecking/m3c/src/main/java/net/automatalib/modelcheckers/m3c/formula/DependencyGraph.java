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
package net.automatalib.modelcheckers.m3c.formula;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.commons.util.collections.BitSetIterator;
import net.automatalib.modelcheckers.m3c.formula.modalmu.AbstractFixedPointFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;

/**
 * A dependency graph is used to represent a hierarchical equational system.
 *
 * @param <L>
 *         edge label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public final class DependencyGraph<L, AP> {

    /* All formulaNodes except FixedPoint- and VariableNode */
    private final List<FormulaNode<L, AP>> formulaNodes;

    /* FormulaNodes divided into equational blocks */
    private final List<EquationalBlock<L, AP>> blocks;

    /* nu X1 -> fixedPointVarMap.get("X1") returns the node associated to nu X1 */
    private final Map<String, FormulaNode<L, AP>> fixedPointVarMap;
    private final FormulaNode<L, AP> ast;
    private final int numVars;

    public DependencyGraph(FormulaNode<L, AP> root) {
        this.formulaNodes = new ArrayList<>();
        this.blocks = new ArrayList<>();
        this.fixedPointVarMap = new HashMap<>();
        this.ast = root.toNNF();
        this.numVars = setVarNumbers(ast, 0);
        createEquationalBlocks(ast);
    }

    private void sortBlocks() {
        for (EquationalBlock<L, AP> block : blocks) {
            Collections.reverse(block.getNodes());
        }
    }

    private void createEquationalBlocks(FormulaNode<L, AP> root) {
        boolean isMax = !(root instanceof LfpNode);
        EquationalBlock<L, AP> block = new EquationalBlock<>(isMax);
        blocks.add(block);
        createEquationalBlocks(root, 0);
        sortBlocks();
    }

    private void createEquationalBlocks(FormulaNode<L, AP> node, int blockNumber) {
        EquationalBlock<L, AP> currentBlock = blocks.get(blockNumber);
        boolean isMax = currentBlock.isMaxBlock();

        /* Check if new equational block has to be created */
        int newBlockNumber = blockNumber;
        if (node instanceof GfpNode && !isMax) {
            newBlockNumber = blocks.size();
            currentBlock = new EquationalBlock<>(true);
            blocks.add(currentBlock);
        } else if (node instanceof LfpNode && isMax) {
            newBlockNumber = blocks.size();
            currentBlock = new EquationalBlock<>(false);
            blocks.add(currentBlock);
        }

        /* Only keep track of non FixedPoint/VariableNodes in blocks */
        if (!(node instanceof AbstractFixedPointFormulaNode || node instanceof VariableNode)) {
            currentBlock.addNode(node);
        }

        /* Recurse into subtrees */
        if (node instanceof AbstractUnaryFormulaNode) {
            final AbstractUnaryFormulaNode<L, AP> unaryNode = (AbstractUnaryFormulaNode<L, AP>) node;
            createEquationalBlocks(unaryNode.getChild(), newBlockNumber);
        } else if (node instanceof AbstractBinaryFormulaNode) {
            final AbstractBinaryFormulaNode<L, AP> binaryNode = (AbstractBinaryFormulaNode<L, AP>) node;
            createEquationalBlocks(binaryNode.getLeftChild(), newBlockNumber);
            createEquationalBlocks(binaryNode.getRightChild(), newBlockNumber);
        }
    }

    private int setVarNumbers(FormulaNode<L, AP> node, int varNumber) {
        /* Fill fixedPointVarMap */
        if (node instanceof AbstractFixedPointFormulaNode) {
            fixedPointVarMap.put(((AbstractFixedPointFormulaNode<L, AP>) node).getVariable(), node);
        }

        /* Set node's variableNumber */
        if (node instanceof VariableNode) {
            /* VariableNode has same variableNumber as the fixed point it references */
            String refVariable = ((VariableNode<L, AP>) node).getVariable();
            FormulaNode<L, AP> refNode = fixedPointVarMap.get(refVariable);
            assert refNode != null : "Cannot reference unknown variable"; // validated by the parser
            node.setVarNumber(refNode.getVarNumber());
        } else {
            node.setVarNumber(varNumber);
        }

        int newVarNumber = varNumber;
        /* Only count non FixedPoint/VariableNodes */
        if (!(node instanceof AbstractFixedPointFormulaNode || node instanceof VariableNode)) {
            newVarNumber++;
            formulaNodes.add(node);
        }

        /* Recurse into subtrees */
        if (node instanceof AbstractUnaryFormulaNode) {
            final AbstractUnaryFormulaNode<L, AP> unaryNode = (AbstractUnaryFormulaNode<L, AP>) node;
            newVarNumber = setVarNumbers(unaryNode.getChild(), newVarNumber);
        } else if (node instanceof AbstractBinaryFormulaNode) {
            final AbstractBinaryFormulaNode<L, AP> binaryNode = (AbstractBinaryFormulaNode<L, AP>) node;
            newVarNumber = setVarNumbers(binaryNode.getLeftChild(), newVarNumber);
            newVarNumber = setVarNumbers(binaryNode.getRightChild(), newVarNumber);
        }

        return newVarNumber;
    }

    /**
     * Returns the equational block for the given index.
     *
     * @param index
     *         index of the equational block to return
     *
     * @return the equational block at the given {@code index}.
     */
    public EquationalBlock<L, AP> getBlock(int index) {
        return blocks.get(index);
    }

    /**
     * Returns the number of variables which is equal to the number of subformulas.
     *
     * @return the number of variables.
     */
    public int getNumVariables() {
        return numVars;
    }

    /**
     * Returns the list of all subformulas.
     *
     * @return the list of all subformulas.
     */
    public List<FormulaNode<L, AP>> getFormulaNodes() {
        return formulaNodes;
    }

    /**
     * Returns all equational blocks of the equational system.
     *
     * @return all equational blocks of the equational system.
     */
    public List<EquationalBlock<L, AP>> getBlocks() {
        return blocks;
    }

    /**
     * Returns the abstract syntax tree of the input formula after it has been transformed into negation normal form.
     *
     * @return the abstract syntax tree in negation normal form.
     */
    public FormulaNode<L, AP> getAST() {
        return ast;
    }

    /**
     * Returns a boolean array that is sized according to {@link #getNumVariables()} such that every index provided in
     * {@code satisfiedVars} is set to {@code true}.
     *
     * @param satisfiedVars
     *         the set of indices that should be set to {@code true}
     *
     * @return a boolean array that is sized according to {@link #getNumVariables()} such that every index provided in
     * {@code satisfiedVars} is set to {@code true}.
     */
    public boolean[] toBoolArray(BitSet satisfiedVars) {
        final boolean[] arr = new boolean[getNumVariables()];
        final BitSetIterator iter = new BitSetIterator(satisfiedVars);

        while (iter.hasNext()) {
            arr[iter.nextInt()] = true;
        }

        return arr;
    }
}
