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

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
import net.automatalib.modelcheckers.m3c.formula.AbstractModalFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A BDDTransformer represents a property transformer for a list of BDDs (Binary Decision Diagrams), one per subformula.
 *
 * @param <L>
 *         edge label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public class BDDTransformer<L, AP> extends AbstractPropertyTransformer<BDDTransformer<L, AP>, L, AP> {

    private final BDDManager bddManager;
    /* One BDD for each lhs of equation system */
    private final BDD[] bdds;

    BDDTransformer(BDDManager bddManager, BDD[] bdds) {
        this.bddManager = bddManager;
        this.bdds = bdds;
    }

    BDDTransformer(BDDManager bddManager, BDD[] bdds, boolean isMust) {
        super(isMust);
        this.bddManager = bddManager;
        this.bdds = bdds;
    }

    /**
     * Constructor used to initialize the property transformer of a node.
     *
     * @param bddManager
     *         used to create the BDDs
     * @param dependencyGraph
     *         of the formula that is currently being solved
     */
    public BDDTransformer(BDDManager bddManager, DependencyGraph<L, AP> dependencyGraph) {
        this(bddManager, new BDD[dependencyGraph.getNumVariables()]);
        for (EquationalBlock<L, AP> block : dependencyGraph.getBlocks()) {
            if (block.isMaxBlock()) {
                for (FormulaNode<L, AP> node : block.getNodes()) {
                    bdds[node.getVarNumber()] = bddManager.readOne();
                }
            } else {
                for (FormulaNode<L, AP> node : block.getNodes()) {
                    bdds[node.getVarNumber()] = bddManager.readLogicZero();
                }
            }
        }
    }

    /**
     * Constructor used to create the property transformer for an edge.
     *
     * @param bddManager
     *         used to create the BDDs
     * @param edgeLabel
     *         of the edge
     * @param edgeProperty
     *         of the edge
     * @param dependencyGraph
     *         of the formula that is currently being solved
     * @param <TP>
     *         edge property type
     */
    public <TP extends ModalEdgeProperty> BDDTransformer(BDDManager bddManager,
                                                         L edgeLabel,
                                                         TP edgeProperty,
                                                         DependencyGraph<L, AP> dependencyGraph) {
        this(bddManager, new BDD[dependencyGraph.getNumVariables()], edgeProperty.isMust());
        for (FormulaNode<L, AP> node : dependencyGraph.getFormulaNodes()) {
            int xi = node.getVarNumber();
            if (node instanceof AbstractModalFormulaNode) {
                final AbstractModalFormulaNode<L, AP> modalNode = (AbstractModalFormulaNode<L, AP>) node;
                final L action = modalNode.getAction();
                /* action matches edgeLabel AND (node instanceof DiamondNode => edge.isMust) */
                if ((action == null || action.equals(edgeLabel)) &&
                    (!(modalNode instanceof DiamondNode) || edgeProperty.isMust())) {
                    int xj = modalNode.getVarNumberChild();
                    bdds[xi] = bddManager.ithVar(xj);
                } else if (modalNode instanceof DiamondNode) {
                    bdds[xi] = bddManager.readLogicZero();
                } else if (modalNode instanceof BoxNode) {
                    bdds[xi] = bddManager.readOne();
                }
            } else {
                bdds[xi] = bddManager.readLogicZero();
            }
        }
    }

    /**
     * The Property Transformer representing the identity function.
     *
     * @param bddManager
     *         used to create the BDDs
     * @param numberOfVars
     *         the number of subformulas
     */
    public BDDTransformer(BDDManager bddManager, int numberOfVars) {
        this(bddManager, new BDD[numberOfVars]);
        for (int var = 0; var < numberOfVars; var++) {
            bdds[var] = bddManager.ithVar(var);
        }
    }

    @Override
    public BitSet evaluate(boolean[] input) {
        final BitSet output = new BitSet();
        for (int i = 0; i < bdds.length; i++) {
            if (bdds[i].eval(input).equals(bddManager.readOne())) {
                output.set(i);
            }
        }
        return output;
    }

    @Override
    public BDDTransformer<L, AP> compose(BDDTransformer<L, AP> other) {
        final BDD[] composedBDDs = new BDD[bdds.length];
        for (int var = 0; var < bdds.length; var++) {
            final BDD composedBDD = bdds[var].vectorCompose(other.bdds);
            composedBDDs[var] = composedBDD;
        }
        return new BDDTransformer<>(bddManager, composedBDDs, this.isMust());
    }

    @Override
    public BDDTransformer<L, AP> createUpdate(Set<AP> atomicPropositions,
                                              List<BDDTransformer<L, AP>> compositions,
                                              EquationalBlock<L, AP> currentBlock) {
        /* Set BDDs of updated transformer to initial bdds as we do not update all bdds
         * but only those for the current block */
        final BDD[] updatedBDDs = bdds.clone();
        for (FormulaNode<L, AP> node : currentBlock.getNodes()) {
            updateFormulaNode(atomicPropositions, compositions, updatedBDDs, node);
        }
        return new BDDTransformer<>(bddManager, updatedBDDs);
    }

    private void updateFormulaNode(Set<AP> atomicPropositions,
                                   List<BDDTransformer<L, AP>> compositions,
                                   BDD[] updatedBDDs,
                                   FormulaNode<L, AP> node) {
        final int varIdx = node.getVarNumber();
        final BDD result;
        if (node instanceof BoxNode) {
            result = andBddList(compositions, varIdx);
        } else if (node instanceof DiamondNode) {
            result = orBddList(compositions, varIdx);
        } else if (node instanceof AndNode) {
            final AndNode<L, AP> andNode = (AndNode<L, AP>) node;
            result = updatedBDDs[andNode.getVarNumberLeft()].and(updatedBDDs[andNode.getVarNumberRight()]);
        } else if (node instanceof OrNode) {
            final OrNode<L, AP> orNode = (OrNode<L, AP>) node;
            result = updatedBDDs[orNode.getVarNumberLeft()].or(updatedBDDs[orNode.getVarNumberRight()]);
        } else if (node instanceof TrueNode) {
            result = bddManager.readOne();
        } else if (node instanceof FalseNode) {
            result = bddManager.readLogicZero();
        } else if (node instanceof NotNode) {
            final NotNode<L, AP> notNode = (NotNode<L, AP>) node;
            result = bdds[notNode.getVarNumberChild()].not();
        } else if (node instanceof AtomicNode) {
            final AP atomicProp = ((AtomicNode<L, AP>) node).getProposition();
            if (atomicPropositions.contains(atomicProp)) {
                result = bddManager.readOne();
            } else {
                result = bddManager.readLogicZero();
            }
        } else {
            throw new IllegalArgumentException();
        }
        updatedBDDs[varIdx] = result;
    }

    BDD andBddList(List<BDDTransformer<L, AP>> compositions, int var) {
        /* Conjunction over the var-th BDDs of compositions */
        final Optional<BDD> result = compositions.stream().map(comp -> comp.getBDD(var)).reduce(BDD::and);
        return result.orElseGet(bddManager::readOne);
    }

    BDD orBddList(List<BDDTransformer<L, AP>> compositions, int var) {
        /* Disjunction over the var-th BDDs of compositions */
        final Optional<BDD> result =
                compositions.stream().filter(BDDTransformer::isMust).map(comp -> comp.getBDD(var)).reduce(BDD::or);
        return result.orElseGet(bddManager::readLogicZero);
    }

    /**
     * Returns the {@link BDD} used to compute the satisfiability of subformula with variable number {@code var}.
     *
     * @param var
     *         index of the BDD to return
     *
     * @return the {@link BDD} used to compute the satisfiability of subformula with variable number {@code var}
     */
    public BDD getBDD(int var) {
        return bdds[var];
    }

    /**
     * Returns the number of subformulas.
     *
     * @return the number of subformulas
     */
    public int getNumberOfVars() {
        return bdds.length;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bdds);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BDDTransformer<?, ?> that = (BDDTransformer<?, ?>) o;

        return Arrays.equals(this.bdds, that.bdds);
    }
}
