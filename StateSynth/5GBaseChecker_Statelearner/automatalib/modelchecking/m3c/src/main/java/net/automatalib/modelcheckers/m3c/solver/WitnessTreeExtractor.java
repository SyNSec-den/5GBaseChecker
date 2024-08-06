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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An implementation for generating witnesses of satisfied µ-calculus formulae based on the tableau algorithm of <a
 * href="https://link.springer.com/chapter/10.1007/3-540-50939-9_144">Colin Stirling &amp; David Walker</a>. This
 * implementation is (currently) restricted to witnesses of negated safety-properties, as for these properties a single
 * witnesses is sufficient and the witnesses are guaranteed to be finite.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author freese
 * @author frohme
 */
final class WitnessTreeExtractor<L, AP> {

    private final WitnessTree<L, AP> wTree = new WitnessTree<>();
    private final Map<L, AbstractDDSolver<?, L, AP>.WorkUnit<?, ?>> units;
    private final DependencyGraph<L, AP> dg;
    private final BitSet initialContext;

    private WitnessTreeExtractor(Map<L, AbstractDDSolver<?, L, AP>.WorkUnit<?, ?>> units,
                                 DependencyGraph<L, AP> dg,
                                 BitSet initialContext) {
        this.units = units;
        this.dg = dg;
        this.initialContext = initialContext;
    }

    static <L, AP> WitnessTree<L, AP> computeWitness(ContextFreeModalProcessSystem<L, AP> cfmps,
                                                     Map<L, AbstractDDSolver<?, L, AP>.WorkUnit<?, ?>> workUnits,
                                                     DependencyGraph<L, AP> dependencyGraph,
                                                     FormulaNode<L, AP> formula,
                                                     BitSet initialContext) {

        // assert that the formula equals the AST (in NNF) so that we can continue to use the AST for its set varNumbers
        assert Objects.equals(dependencyGraph.getAST().toString(), formula.toString());

        final WitnessTreeExtractor<L, AP> extractor =
                new WitnessTreeExtractor<>(workUnits, dependencyGraph, initialContext);
        return extractor.computeWitnessStep(cfmps, dependencyGraph.getAST());
    }

    private WitnessTree<L, AP> computeWitnessStep(ContextFreeModalProcessSystem<L, AP> cfmps,
                                                  FormulaNode<L, AP> formula) {

        final Deque<WitnessTreeState<?, L, ?, AP>> queue = new ArrayDeque<>();
        final AbstractDDSolver<?, L, AP>.WorkUnit<?, ?> mainUnit = units.get(cfmps.getMainProcess());
        assert mainUnit != null;
        final WitnessTreeState<?, L, ?, AP> init = getInitialTreeState(mainUnit, formula);

        queue.add(init);

        while (!queue.isEmpty()) {
            final WitnessTreeState<?, L, ?, AP> queueElement = queue.pop();
            final int currentNode = wTree.addNode(queueElement);

            if (currentNode > 0) {
                wTree.connect(queueElement.parentId, currentNode, queueElement.displayLabel);
            }

            final List<WitnessTreeState<?, L, ?, AP>> nextTreeStates = getNextTreeStates(queueElement);

            if (nextTreeStates.isEmpty()) {
                wTree.computePath(currentNode);
                break;
            } else {
                queue.addAll(nextTreeStates);
            }
        }

        return wTree;
    }

    private List<WitnessTreeState<?, L, ?, AP>> getNextTreeStates(WitnessTreeState<?, L, ?, AP> queueElement) {

        FormulaNode<L, AP> visitedFormula = queueElement.subformula;

        if (visitedFormula instanceof LfpNode) {
            visitedFormula = ((LfpNode<L, AP>) visitedFormula).getChild();
        }
        if (visitedFormula instanceof VariableNode) {
            // fetch fresh child formula because in case of a VariableNode we want the whole formula again
            visitedFormula = dg.getFormulaNodes().get(visitedFormula.getVarNumber());
        }

        if (visitedFormula instanceof OrNode) {
            return exploreOR((OrNode<L, AP>) visitedFormula, queueElement);
        } else if (visitedFormula instanceof DiamondNode) {
            return exploreDia((DiamondNode<L, AP>) visitedFormula, queueElement);
        } else if (visitedFormula instanceof TrueNode) {
            return Collections.emptyList();
        } else if (visitedFormula instanceof AtomicNode) {
            return Collections.emptyList();
        } else {
            throw new IllegalArgumentException("Cannot handle node" + visitedFormula);
        }
    }

    private <N, E> List<WitnessTreeState<?, L, ?, AP>> exploreOR(OrNode<L, AP> formula,
                                                                 WitnessTreeState<N, L, E, AP> queueElement) {
        final FormulaNode<L, AP> leftFormula = formula.getLeftChild();
        final FormulaNode<L, AP> rightFormula = formula.getRightChild();

        final List<WitnessTreeState<?, L, ?, AP>> result = new ArrayList<>();

        if (queueElement.getSatisfiedSubformulae(dg, queueElement.state).get(leftFormula.getVarNumber())) {
            result.add(new WitnessTreeState<>(queueElement.stack,
                                              queueElement.unit,
                                              queueElement.state,
                                              leftFormula,
                                              queueElement.context,
                                              leftFormula.toString(),
                                              null,
                                              wTree.size() - 1));
        }
        if (queueElement.getSatisfiedSubformulae(dg, queueElement.state).get(rightFormula.getVarNumber())) {
            result.add(new WitnessTreeState<>(queueElement.stack,
                                              queueElement.unit,
                                              queueElement.state,
                                              rightFormula,
                                              queueElement.context,
                                              rightFormula.toString(),
                                              null,
                                              wTree.size() - 1));
        }

        return result;
    }

    private <N> List<WitnessTreeState<?, L, ?, AP>> exploreDia(DiamondNode<L, AP> formula,
                                                               WitnessTreeState<N, L, ?, AP> queueElement) {
        if (Objects.equals(queueElement.state, queueElement.pmpg.getFinalNode())) {
            assert queueElement.stack != null;
            return findDiaMoveEndNodeReturn(queueElement);
        } else if (formula.getAction() == null) {
            return findDiaMoveWithEmpty(queueElement, formula);
        } else {
            return findDiaMoveRegularStep(queueElement, formula);
        }
    }

    private <N, E> List<WitnessTreeState<?, L, ?, AP>> findDiaMoveWithEmpty(WitnessTreeState<N, L, E, AP> queueElement,
                                                                            DiamondNode<L, AP> formula) {

        final List<WitnessTreeState<?, L, ?, AP>> result = new ArrayList<>();
        final ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg = queueElement.pmpg;

        for (E edge : pmpg.getOutgoingEdges(queueElement.state)) {
            final N target = pmpg.getTarget(edge);
            final L label = pmpg.getEdgeLabel(edge);

            if (pmpg.getEdgeProperty(edge).isInternal()) {
                if (queueElement.getSatisfiedSubformulae(dg, target).get(formula.getChild().getVarNumber())) {
                    final WitnessTreeState<?, L, ?, AP> toAdd = new WitnessTreeState<>(queueElement.stack,
                                                                                       queueElement.unit,
                                                                                       target,
                                                                                       formula.getChild(),
                                                                                       queueElement.context,
                                                                                       Objects.toString(label),
                                                                                       label,
                                                                                       wTree.size() - 1);
                    result.add(toAdd);
                }
            } else {
                final AbstractDDSolver<?, L, AP>.WorkUnit<?, ?> unit = units.get(label);
                assert unit != null;
                final WitnessTreeState<?, L, ?, AP> toAdd =
                        buildProcessNode(queueElement, unit, label, target, formula);

                if (toAdd != null) {
                    result.add(toAdd);
                }
            }
        }

        return result;
    }

    private <N, E> List<WitnessTreeState<?, L, ?, AP>> findDiaMoveEndNodeReturn(WitnessTreeState<N, L, E, AP> queueElement) {

        assert queueElement.stack != null;
        final WitnessTreeState<?, L, ?, AP> toAdd = buildReturnNode(queueElement, queueElement.stack);

        return Collections.singletonList(toAdd);
    }

    private <N, E> List<WitnessTreeState<?, L, ?, AP>> findDiaMoveRegularStep(WitnessTreeState<N, L, E, AP> queueElement,
                                                                              DiamondNode<L, AP> formula) {
        final List<WitnessTreeState<?, L, ?, AP>> result = new ArrayList<>();
        final L moveLabel = formula.getAction();
        final ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg = queueElement.pmpg;

        for (E edge : pmpg.getOutgoingEdges(queueElement.state)) {
            final N target = pmpg.getTarget(edge);
            final L label = pmpg.getEdgeLabel(edge);

            if (pmpg.getEdgeProperty(edge).isInternal()) {
                if (Objects.equals(label, moveLabel) &&
                    queueElement.getSatisfiedSubformulae(dg, target).get(formula.getChild().getVarNumber())) {
                    final WitnessTreeState<?, L, ?, AP> toAdd = new WitnessTreeState<>(queueElement.stack,
                                                                                       queueElement.unit,
                                                                                       target,
                                                                                       formula.getChild(),
                                                                                       queueElement.context,
                                                                                       Objects.toString(label),
                                                                                       label,
                                                                                       wTree.size() - 1);
                    result.add(toAdd);
                }
            } else {
                final AbstractDDSolver<?, L, AP>.WorkUnit<?, ?> unit = units.get(label);
                assert unit != null;
                final WitnessTreeState<?, L, ?, AP> toAdd =
                        buildProcessNode(queueElement, unit, label, target, formula);

                if (toAdd != null) {
                    result.add(toAdd);
                }
            }
        }

        return result;
    }

    private <N1, N2, E1, E2> @Nullable WitnessTreeState<N2, L, E2, AP> buildProcessNode(WitnessTreeState<N1, L, E1, AP> queueElement,
                                                                                        AbstractDDSolver<?, L, AP>.WorkUnit<N2, E2> unit,
                                                                                        L label,
                                                                                        N1 target,
                                                                                        DiamondNode<L, AP> formula) {
        final WitnessTreeState<N1, L, ?, AP> succ = new WitnessTreeState<>(queueElement.stack,
                                                                           queueElement.unit,
                                                                           target,
                                                                           queueElement.subformula,
                                                                           queueElement.context,
                                                                           Objects.toString(label),
                                                                           null,
                                                                           queueElement.parentId);

        @SuppressWarnings("nullness") // we have checked non-nullness of initial nodes in the model checker
        final @NonNull N2 initialNode = unit.pmpg.getInitialNode();
        final BitSet finalFormulae = queueElement.getSatisfiedSubformulae(dg, target);

        WitnessTreeState<N2, L, E2, AP> result = new WitnessTreeState<>(succ,
                                                                        unit,
                                                                        initialNode,
                                                                        formula,
                                                                        finalFormulae,
                                                                        Objects.toString(label),
                                                                        null,
                                                                        wTree.size() - 1);

        if (result.getSatisfiedSubformulae(dg, result.state).get(formula.getVarNumber())) {
            return result;
        } else {
            return null;
        }
    }

    private <N1, N2, E1, E2> WitnessTreeState<N2, L, E2, AP> buildReturnNode(WitnessTreeState<N1, L, E1, AP> queueElement,
                                                                             WitnessTreeState<N2, L, E2, AP> prev) {
        return new WitnessTreeState<>(prev.stack,
                                      prev.unit,
                                      prev.state,
                                      queueElement.subformula,
                                      prev.context,
                                      "return",
                                      null,
                                      wTree.size() - 1);
    }

    private <N, E> WitnessTreeState<?, L, ?, AP> getInitialTreeState(AbstractDDSolver<?, L, AP>.WorkUnit<N, E> unit,
                                                                     FormulaNode<L, AP> formula) {
        @SuppressWarnings("nullness") // we have checked non-nullness of initial nodes in the model checker
        final @NonNull N initialNode = unit.pmpg.getInitialNode();
        return new WitnessTreeState<>(null, unit, initialNode, formula, initialContext, "", null, -1);
    }
}
