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
package net.automatalib.modelcheckers.m3c.formula.visitor;

import java.util.HashSet;
import java.util.Set;

import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;

/**
 * A visitor that transforms a given mu-calculus or CTL formula to negation-normal-form.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public class NNFVisitor<L, AP> {

    private Set<String> varsToNegate;

    public FormulaNode<L, AP> transformToNNF(FormulaNode<L, AP> node) {
        this.varsToNegate = new HashSet<>();
        return visit(node, false);
    }

    private FormulaNode<L, AP> visit(FormulaNode<L, AP> node, boolean negate) {
        if (node instanceof GfpNode) {
            return visitGFPNode((GfpNode<L, AP>) node, negate);
        } else if (node instanceof LfpNode) {
            return visitLFPNode((LfpNode<L, AP>) node, negate);
        } else if (node instanceof AndNode) {
            return visitAndNode((AndNode<L, AP>) node, negate);
        } else if (node instanceof AtomicNode) {
            return visitAtomicNode((AtomicNode<L, AP>) node, negate);
        } else if (node instanceof BoxNode) {
            return visitBoxNode((BoxNode<L, AP>) node, negate);
        } else if (node instanceof DiamondNode) {
            return visitDiamondNode((DiamondNode<L, AP>) node, negate);
        } else if (node instanceof FalseNode) {
            return visitFalseNode(negate);
        } else if (node instanceof VariableNode) {
            return visitVariableNode((VariableNode<L, AP>) node, negate);
        } else if (node instanceof NotNode) {
            return visitNotNode((NotNode<L, AP>) node, negate);
        } else if (node instanceof OrNode) {
            return visitOrNode((OrNode<L, AP>) node, negate);
        } else if (node instanceof TrueNode) {
            return visitTrueNode(negate);
        } else {
            throw new IllegalArgumentException("Node is not a ModalMuNode");
        }
    }

    private FormulaNode<L, AP> visitGFPNode(GfpNode<L, AP> node, boolean negate) {
        if (!negate) {
            FormulaNode<L, AP> childNode = visit(node.getChild(), false);
            return new GfpNode<>(node.getVariable(), childNode);
        }

        varsToNegate.add(node.getVariable());
        FormulaNode<L, AP> childNode = visit(node.getChild(), true);
        varsToNegate.remove(node.getVariable());

        return new LfpNode<>(node.getVariable(), childNode);
    }

    private FormulaNode<L, AP> visitLFPNode(LfpNode<L, AP> node, boolean negate) {
        if (!negate) {
            FormulaNode<L, AP> childNode = visit(node.getChild(), false);
            return new LfpNode<>(node.getVariable(), childNode);
        }

        varsToNegate.add(node.getVariable());
        FormulaNode<L, AP> childNode = visit(node.getChild(), true);
        varsToNegate.remove(node.getVariable());

        return new GfpNode<>(node.getVariable(), childNode);
    }

    private FormulaNode<L, AP> visitAndNode(AndNode<L, AP> node, boolean negate) {
        final FormulaNode<L, AP> leftChild = visit(node.getLeftChild(), negate);
        final FormulaNode<L, AP> rightChild = visit(node.getRightChild(), negate);

        return negate ? new OrNode<>(leftChild, rightChild) : new AndNode<>(leftChild, rightChild);
    }

    private FormulaNode<L, AP> visitAtomicNode(AtomicNode<L, AP> node, boolean negate) {
        final FormulaNode<L, AP> newNode = new AtomicNode<>(node.getProposition());
        return negate ? new NotNode<>(newNode) : newNode;
    }

    private FormulaNode<L, AP> visitBoxNode(BoxNode<L, AP> node, boolean negate) {
        final FormulaNode<L, AP> childNode = visit(node.getChild(), negate);
        final L action = node.getAction();

        return negate ? new DiamondNode<>(action, childNode) : new BoxNode<>(action, childNode);
    }

    private FormulaNode<L, AP> visitDiamondNode(DiamondNode<L, AP> node, boolean negate) {
        final FormulaNode<L, AP> childNode = visit(node.getChild(), negate);
        final L action = node.getAction();

        return negate ? new BoxNode<>(action, childNode) : new DiamondNode<>(action, childNode);
    }

    private FormulaNode<L, AP> visitFalseNode(boolean negate) {
        return negate ? new TrueNode<>() : new FalseNode<>();
    }

    private FormulaNode<L, AP> visitVariableNode(VariableNode<L, AP> node, boolean negate) {
        final FormulaNode<L, AP> newNode = new VariableNode<>(node.getVariable());
        boolean negateVariable = negate ^ varsToNegate.contains(node.getVariable());

        return negateVariable ? new NotNode<>(newNode) : newNode;
    }

    private FormulaNode<L, AP> visitNotNode(NotNode<L, AP> node, boolean negate) {
        return visit(node.getChild(), !negate);
    }

    private FormulaNode<L, AP> visitOrNode(OrNode<L, AP> node, boolean negate) {
        final FormulaNode<L, AP> leftChild = visit(node.getLeftChild(), negate);
        final FormulaNode<L, AP> rightChild = visit(node.getRightChild(), negate);

        return negate ? new AndNode<>(leftChild, rightChild) : new OrNode<>(leftChild, rightChild);
    }

    private FormulaNode<L, AP> visitTrueNode(boolean negate) {
        return negate ? new FalseNode<>() : new TrueNode<>();
    }

}
