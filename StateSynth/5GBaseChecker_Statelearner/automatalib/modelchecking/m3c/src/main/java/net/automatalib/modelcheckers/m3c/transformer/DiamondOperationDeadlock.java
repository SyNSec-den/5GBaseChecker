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

import java.util.Set;
import java.util.function.UnaryOperator;

import info.scce.addlib.dd.xdd.latticedd.example.BooleanVector;
import net.automatalib.modelcheckers.m3c.formula.AbstractBinaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;

/**
 * Implementation of the diamond function when the node has exactly one successor.
 *
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public class DiamondOperationDeadlock<AP> implements UnaryOperator<BooleanVector> {

    private final EquationalBlock<?, AP> block;
    private final Set<AP> atomicPropositions;

    public DiamondOperationDeadlock(Set<AP> atomicPropositions, EquationalBlock<?, AP> block) {
        this.atomicPropositions = atomicPropositions;
        this.block = block;
    }

    @Override
    public BooleanVector apply(BooleanVector vector) {
        final boolean[] result = vector.data().clone();
        for (FormulaNode<?, AP> node : block.getNodes()) {
            final int currentVar = node.getVarNumber();
            if (node instanceof BoxNode) {
                result[currentVar] = true;
            } else if (node instanceof DiamondNode) {
                result[currentVar] = false;
            } else if (node instanceof AbstractBinaryFormulaNode) {
                final AbstractBinaryFormulaNode<?, ?> binaryNode = (AbstractBinaryFormulaNode<?, ?>) node;
                final int xj1 = binaryNode.getVarNumberLeft();
                final int xj2 = binaryNode.getVarNumberRight();
                if (binaryNode instanceof AndNode) {
                    result[currentVar] = result[xj1] && result[xj2];
                } else {
                    result[currentVar] = result[xj1] || result[xj2];
                }
            } else if (node instanceof TrueNode) {
                result[currentVar] = true;
            } else if (node instanceof FalseNode) {
                result[currentVar] = false;
            } else if (node instanceof NotNode) {
                final NotNode<?, ?> notNode = (NotNode<?, ?>) node;
                result[currentVar] = !result[notNode.getVarNumberChild()];
            } else if (node instanceof AtomicNode) {
                @SuppressWarnings("unchecked")
                final AP prop = ((AtomicNode<?, AP>) node).getProposition();
                result[currentVar] = atomicPropositions.contains(prop);
            } else {
                throw new IllegalArgumentException("The current equational block contains an unsupported formula type.");
            }
        }
        return new BooleanVector(result);
    }

}
