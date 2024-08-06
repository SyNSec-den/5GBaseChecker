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

import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AWUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EWUNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;

/**
 * A visitor that transforms a given CTL formula to an equivalent mu-calculus formula.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public class CTLToMuCalc<L, AP> implements FormulaNodeVisitor<FormulaNode<L, AP>, L, AP> {

    private int numFixedPointVars;

    public CTLToMuCalc() {
        numFixedPointVars = 0;
    }

    public FormulaNode<L, AP> toMuCalc(FormulaNode<L, AP> ctlFormula) {
        numFixedPointVars = 0;
        return visit(ctlFormula);
    }

    @Override
    public FormulaNode<L, AP> visit(FormulaNode<L, AP> node) {
        return node.accept(this);
    }

    @Override
    public FormulaNode<L, AP> visit(AFNode<L, AP> node) {
        /* AF p = mu X.(toMu(p) | (<>true & []X)) */
        FormulaNode<L, AP> p = visit(node.getChild());
        String fixedPointVar = getFixedPointVar();
        DiamondNode<L, AP> hasSuccessor = new DiamondNode<>(new TrueNode<>());
        BoxNode<L, AP> allSuccessorSatisfyX = new BoxNode<>(new VariableNode<>(fixedPointVar));
        AndNode<L, AP> and = new AndNode<>(hasSuccessor, allSuccessorSatisfyX);
        OrNode<L, AP> or = new OrNode<>(p, and);
        return new LfpNode<>(fixedPointVar, or);
    }

    @Override
    public FormulaNode<L, AP> visit(AGNode<L, AP> node) {
        /* AG p = nu X.(p & []X) */
        FormulaNode<L, AP> p = visit(node.getChild());
        String fixedPointVar = getFixedPointVar();
        BoxNode<L, AP> allSuccessorsSatisfyX = new BoxNode<>(new VariableNode<>(fixedPointVar));
        AndNode<L, AP> and = new AndNode<>(p, allSuccessorsSatisfyX);
        return new GfpNode<>(fixedPointVar, and);
    }

    @Override
    public FormulaNode<L, AP> visit(AUNode<L, AP> node) {
        /* A[p U q] = mu X.(toMu(q) | (toMu(p) & (<>true & []X))) */
        FormulaNode<L, AP> p = visit(node.getLeftChild());
        FormulaNode<L, AP> q = visit(node.getRightChild());
        String fixedPointVar = getFixedPointVar();
        DiamondNode<L, AP> hasSuccessor = new DiamondNode<>(new TrueNode<>());
        BoxNode<L, AP> allSuccessorsSatisfyX = new BoxNode<>(new VariableNode<>(fixedPointVar));
        AndNode<L, AP> innerAnd = new AndNode<>(hasSuccessor, allSuccessorsSatisfyX);
        AndNode<L, AP> outerAnd = new AndNode<>(p, innerAnd);
        OrNode<L, AP> or = new OrNode<>(q, outerAnd);
        return new LfpNode<>(fixedPointVar, or);
    }

    @Override
    public FormulaNode<L, AP> visit(AWUNode<L, AP> node) {
        /* A[p WU q] = !E[!q U (!p & !q)] */
        FormulaNode<L, AP> p = visit(node.getLeftChild());
        FormulaNode<L, AP> q = visit(node.getRightChild());
        AndNode<L, AP> and = new AndNode<>(new NotNode<>(p), new NotNode<>(q));
        EUNode<L, AP> ewu = new EUNode<>(new NotNode<>(visit(node.getRightChild())), and);
        return visit(new NotNode<>(ewu));
    }

    @Override
    public FormulaNode<L, AP> visit(EFNode<L, AP> node) {
        /* EF p = mu X.(toMu(p) | <>X) */
        String fixedPointVar = getFixedPointVar();
        FormulaNode<L, AP> p = visit(node.getChild());
        DiamondNode<L, AP> hasSuccessorSatisfyingX = new DiamondNode<>(new VariableNode<>(fixedPointVar));
        OrNode<L, AP> orNode = new OrNode<>(p, hasSuccessorSatisfyingX);
        return new LfpNode<>(fixedPointVar, orNode);
    }

    @Override
    public FormulaNode<L, AP> visit(EGNode<L, AP> node) {
        /* EG p = nu X.(toMu(p) & (<>X | [] false)) */
        String fixedPointVar = getFixedPointVar();
        FormulaNode<L, AP> childNode = visit(node.getChild());
        DiamondNode<L, AP> hasSuccessorSatisfyingX = new DiamondNode<>(new VariableNode<>(fixedPointVar));
        BoxNode<L, AP> hasNoSuccessor = new BoxNode<>(new FalseNode<>());
        OrNode<L, AP> or = new OrNode<>(hasSuccessorSatisfyingX, hasNoSuccessor);
        AndNode<L, AP> and = new AndNode<>(childNode, or);
        return new GfpNode<>(fixedPointVar, and);
    }

    @Override
    public FormulaNode<L, AP> visit(EUNode<L, AP> node) {
        /* E[p U q] => mu X.(toMu(q) | (toMu(p) & <>X)) */
        String fixedPointVar = getFixedPointVar();
        FormulaNode<L, AP> p = visit(node.getLeftChild());
        FormulaNode<L, AP> q = visit(node.getRightChild());
        AndNode<L, AP> andNode = new AndNode<>(p, new DiamondNode<>(new VariableNode<>(fixedPointVar)));
        OrNode<L, AP> orNode = new OrNode<>(q, andNode);
        return new LfpNode<>(fixedPointVar, orNode);
    }

    @Override
    public FormulaNode<L, AP> visit(EWUNode<L, AP> node) {
        /* E[p WU q] = E[p U q] | EG p */
        FormulaNode<L, AP> p = visit(node.getLeftChild());
        FormulaNode<L, AP> q = visit(node.getRightChild());
        EUNode<L, AP> until = new EUNode<>(p, q);
        EGNode<L, AP> egNode = new EGNode<>(visit(node.getLeftChild()));
        return visit(new OrNode<>(until, egNode));
    }

    @Override
    public FormulaNode<L, AP> visit(AndNode<L, AP> node) {
        FormulaNode<L, AP> leftChild = visit(node.getLeftChild());
        FormulaNode<L, AP> rightChild = visit(node.getRightChild());
        return new AndNode<>(leftChild, rightChild);
    }

    @Override
    public FormulaNode<L, AP> visit(AtomicNode<L, AP> node) {
        return new AtomicNode<>(node.getProposition());
    }

    @Override
    public FormulaNode<L, AP> visit(BoxNode<L, AP> node) {
        FormulaNode<L, AP> childNode = visit(node.getChild());
        return new BoxNode<>(node.getAction(), childNode);
    }

    @Override
    public FormulaNode<L, AP> visit(DiamondNode<L, AP> node) {
        FormulaNode<L, AP> childNode = visit(node.getChild());
        return new DiamondNode<>(node.getAction(), childNode);
    }

    @Override
    public FormulaNode<L, AP> visit(FalseNode<L, AP> node) {
        return new FalseNode<>();
    }

    @Override
    public FormulaNode<L, AP> visit(NotNode<L, AP> node) {
        FormulaNode<L, AP> childNode = visit(node.getChild());
        return new NotNode<>(childNode);
    }

    @Override
    public FormulaNode<L, AP> visit(OrNode<L, AP> node) {
        FormulaNode<L, AP> leftChild = visit(node.getLeftChild());
        FormulaNode<L, AP> rightChild = visit(node.getRightChild());
        return new OrNode<>(leftChild, rightChild);
    }

    @Override
    public FormulaNode<L, AP> visit(TrueNode<L, AP> node) {
        return new TrueNode<>();
    }

    @Override
    public FormulaNode<L, AP> visit(GfpNode<L, AP> node) {
        return new GfpNode<>(node.getVariable(), visit(node.getChild()));
    }

    @Override
    public FormulaNode<L, AP> visit(LfpNode<L, AP> node) {
        return new LfpNode<>(node.getVariable(), visit(node.getChild()));
    }

    @Override
    public FormulaNode<L, AP> visit(VariableNode<L, AP> node) {
        return new VariableNode<>(node.getVariable());
    }

    private String getFixedPointVar() {
        return "Z" + numFixedPointVars++;
    }

}
