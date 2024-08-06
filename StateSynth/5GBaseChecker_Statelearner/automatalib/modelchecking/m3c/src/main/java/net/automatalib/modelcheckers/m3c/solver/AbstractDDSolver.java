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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.visitor.CTLToMuCalc;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.transformer.TransformerSerializer;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base implementation of the model checker which supports different types of property transformers. The
 * {@link ModelChecker} is (currently) implemented on the basis of the {@link WitnessTreeExtractor} including all its
 * restrictions.
 *
 * @param <T>
 *         property transformer type
 * @param <L>
 *         edge label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
abstract class AbstractDDSolver<T extends AbstractPropertyTransformer<T, L, AP>, L, AP>
        implements ModelChecker<L, ContextFreeModalProcessSystem<L, AP>, FormulaNode<L, AP>, WitnessTree<L, AP>> {

    // Attributes that are constant for a given CFMPS
    private final @KeyFor("workUnits") L mainProcess;

    // Attributes that change for each formula
    private TransformerSerializer<T, L, AP> serializer;
    private DependencyGraph<L, AP> dependencyGraph;
    private int currentBlockIndex;
    // Per-procedure attributes
    private final Map<L, WorkUnit<?, ?>> workUnits;
    // Per-action attributes
    private Map<L, T> mustTransformers;
    private Map<L, T> mayTransformers;

    AbstractDDSolver(ContextFreeModalProcessSystem<L, AP> cfmps) {
        final Map<L, ProceduralModalProcessGraph<?, L, ?, AP, ?>> pmpgs = cfmps.getPMPGs();

        this.workUnits = Maps.newHashMapWithExpectedSize(pmpgs.size());
        for (Map.Entry<L, ProceduralModalProcessGraph<?, L, ?, AP, ?>> e : pmpgs.entrySet()) {
            final L label = e.getKey();
            final ProceduralModalProcessGraph<?, L, ?, AP, ?> pmpg = e.getValue();
            checkPMPG(label, pmpg);
            workUnits.put(label, initializeWorkUnits(label, pmpg));
        }

        // TODO handle empty CFMPSs
        final L mainProcess = cfmps.getMainProcess();
        if (mainProcess == null || !workUnits.containsKey(mainProcess)) {
            throw new IllegalArgumentException("The main process is undefined or has no corresponding MPG.");
        }

        this.mainProcess = mainProcess;
    }

    private <N> void checkPMPG(@UnderInitialization AbstractDDSolver<T, L, AP> this,
                               L label,
                               ProceduralModalProcessGraph<N, L, ?, AP, ?> pmpg) {
        final N initialNode = pmpg.getInitialNode();
        if (initialNode == null) {
            throw new IllegalArgumentException("PMPG '" + label + "' has no start node");
        }
        Preconditions.checkNotNull(pmpg.getFinalNode(), "PMPG '%s' has no end node", label);
        Preconditions.checkArgument(isGuarded(pmpg, initialNode),
                                    "PMPG '%s' is not guarded. All initial transitions must be labelled with atomic actions.",
                                    label);
    }

    private <N, E> boolean isGuarded(@UnderInitialization AbstractDDSolver<T, L, AP> this,
                                     ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg,
                                     N initialNode) {
        for (E initialTransition : pmpg.getOutgoingEdges(initialNode)) {
            if (pmpg.getEdgeProperty(initialTransition).isProcess()) {
                return false;
            }
        }
        return true;
    }

    private <N> WorkUnit<N, ?> initializeWorkUnits(@UnderInitialization AbstractDDSolver<T, L, AP> this,
                                                   L label,
                                                   ProceduralModalProcessGraph<N, L, ?, AP, ?> pmpg) {
        return new WorkUnit<>(label, pmpg, initPredecessorsMapping(pmpg));
    }

    private static <N, L, E, AP> Mapping<N, @Nullable Set<N>> initPredecessorsMapping(ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg) {

        final MutableMapping<N, @Nullable Set<N>> nodeToPredecessors = pmpg.createStaticNodeMapping();

        for (N sourceNode : pmpg.getNodes()) {
            for (E outgoingEdge : pmpg.getOutgoingEdges(sourceNode)) {
                final N targetNode = pmpg.getTarget(outgoingEdge);
                Set<N> nodePredecessors = nodeToPredecessors.get(targetNode);

                if (nodePredecessors == null) {
                    nodePredecessors = new HashSet<>();
                    nodeToPredecessors.put(targetNode, nodePredecessors);
                }

                nodePredecessors.add(sourceNode);
            }
        }

        return nodeToPredecessors;
    }

    @Override
    public @Nullable WitnessTree<L, AP> findCounterExample(ContextFreeModalProcessSystem<L, AP> cfmps,
                                                           Collection<? extends L> inputs,
                                                           FormulaNode<L, AP> formulaNode) {
        final NotNode<L, AP> negatedFormula = new NotNode<>(formulaNode);
        final FormulaNode<L, AP> ast = ctlToMuCalc(negatedFormula).toNNF();

        initialize(ast);
        this.solveInternal(false, Collections.emptyList());

        final boolean sat = isSat();

        if (sat) {
            try {
                final Map<L, AbstractDDSolver<?, L, AP>.WorkUnit<?, ?>> units = Collections.unmodifiableMap(workUnits);
                return WitnessTreeExtractor.computeWitness(cfmps,
                                                           units,
                                                           dependencyGraph,
                                                           ast,
                                                           getAllAPDeadlockedNode());
            } finally {
                shutdownDDManager();
            }
        }

        return null;
    }

    public boolean solve(FormulaNode<L, AP> formula) {
        final FormulaNode<L, AP> ast = ctlToMuCalc(formula).toNNF();

        initialize(ast);
        this.solveInternal(false, Collections.emptyList());

        final boolean sat = isSat();
        shutdownDDManager();
        return sat;
    }

    public SolverHistory<T, L, AP> solveAndRecordHistory(FormulaNode<L, AP> formula) {
        final List<SolverState<?, T, L, AP>> history = new ArrayList<>();
        final FormulaNode<L, AP> ast = ctlToMuCalc(formula).toNNF();

        initialize(ast);

        final Map<L, SolverData<?, T, L, AP>> data = Maps.newHashMapWithExpectedSize(this.workUnits.size());
        for (Entry<L, WorkUnit<?, ?>> e : this.workUnits.entrySet()) {
            data.put(e.getKey(), createProcessData(e.getValue()));
        }

        this.solveInternal(true, history);

        final Map<L, List<String>> serializedMustTransformers = serializePropertyTransformerMap(mustTransformers);
        final Map<L, List<String>> serializedMayTransformers = serializePropertyTransformerMap(mayTransformers);
        final boolean isSat = isSat();

        shutdownDDManager();

        return new SolverHistory<>(data, serializedMustTransformers, serializedMayTransformers, history, isSat);
    }

    private <N, E> SolverData<N, T, L, AP> createProcessData(WorkUnit<N, E> unit) {
        return new SolverData<>(unit.pmpg, serializePropertyTransformers(unit), computeSatisfiedSubformulas(unit));
    }

    private Map<L, List<String>> serializePropertyTransformerMap(Map<L, T> transformers) {
        final Map<L, List<String>> serializedTransformers = Maps.newHashMapWithExpectedSize(transformers.size());
        for (Map.Entry<L, T> entry : transformers.entrySet()) {
            serializedTransformers.put(entry.getKey(), serializer.serialize(entry.getValue()));
        }
        return serializedTransformers;
    }

    private void solveInternal(boolean recordHistory, List<SolverState<?, T, L, AP>> history) {
        boolean workSetIsEmpty = false;
        while (!workSetIsEmpty) {
            workSetIsEmpty = true;
            for (Entry<L, WorkUnit<?, ?>> entry : workUnits.entrySet()) {
                workSetIsEmpty &= solveInternal(entry.getValue(), recordHistory, history);
            }
        }
    }

    private <N> boolean solveInternal(WorkUnit<N, ?> unit,
                                      boolean recordHistory,
                                      List<SolverState<?, T, L, AP>> history) {
        if (!unit.workSet.isEmpty()) {
            final Iterator<N> iter = unit.workSet.iterator();
            final N node = iter.next();
            iter.remove();

            final L label = unit.label;
            final List<T> compositions = updateNodeAndGetCompositions(unit, node);

            if (recordHistory) {
                final List<List<String>> serializedCompositions = new ArrayList<>(compositions.size());
                for (T composition : compositions) {
                    serializedCompositions.add(serializer.serialize(composition));
                }
                history.add(new SolverState<>(serializer.serialize(unit.propTransformers.get(node)),
                                              serializedCompositions,
                                              node,
                                              label,
                                              copyWorkSet(),
                                              getSatisfiedSubformulas(unit, node)));
            }

            return false;
        }

        return true;
    }

    private <N> List<T> updateNodeAndGetCompositions(WorkUnit<N, ?> unit, N node) {
        initUpdate(unit, node);
        final T nodeTransformer = getTransformer(unit, node);
        final List<T> compositions = createCompositions(unit, node);
        final T updatedTransformer = getUpdatedPropertyTransformer(unit, node, nodeTransformer, compositions);
        updateTransformerAndWorkSet(unit, node, nodeTransformer, updatedTransformer);
        return compositions;
    }

    private Map<L, Set<?>> copyWorkSet() {
        final Map<L, Set<?>> copy = Maps.newHashMapWithExpectedSize(workUnits.size());
        for (Map.Entry<L, WorkUnit<?, ?>> e : workUnits.entrySet()) {
            copy.put(e.getKey(), copyWorkSet(e.getValue()));
        }
        return copy;
    }

    private <N> Set<N> copyWorkSet(WorkUnit<N, ?> unit) {
        return new HashSet<>(unit.workSet);
    }

    private <N> Mapping<N, List<FormulaNode<L, AP>>> computeSatisfiedSubformulas(WorkUnit<N, ?> unit) {
        final MutableMapping<N, @Nullable List<FormulaNode<L, AP>>> result = unit.pmpg.createStaticNodeMapping();
        for (N node : unit.pmpg.getNodes()) {
            result.put(node, getSatisfiedSubformulas(unit, node));
        }

        // we put a transformer for every node, so it's no longer null
        return (MutableMapping<N, List<FormulaNode<L, AP>>>) result;
    }

    private <N> List<FormulaNode<L, AP>> getSatisfiedSubformulas(WorkUnit<N, ?> unit, N node) {
        final BitSet output =
                unit.propTransformers.get(node).evaluate(dependencyGraph.toBoolArray(getAllAPDeadlockedNode()));
        final List<FormulaNode<L, AP>> satisfiedSubFormulas = new ArrayList<>();
        for (FormulaNode<L, AP> n : dependencyGraph.getFormulaNodes()) {
            if (output.get(n.getVarNumber())) {
                satisfiedSubFormulas.add(n);
            }
        }
        return satisfiedSubFormulas;
    }

    private BitSet getAllAPDeadlockedNode() {
        return getAllAPDeadlockedNode(workUnits.get(mainProcess).pmpg);
    }

    private <N, E, TP extends ProceduralModalEdgeProperty> BitSet getAllAPDeadlockedNode(ProceduralModalProcessGraph<N, L, E, AP, TP> mainMpg) {
        final BitSet satisfiedVariables = new BitSet();
        @SuppressWarnings("nullness") // we have checked non-nullness of final nodes in the constructor
        final @NonNull N finalNode = mainMpg.getFinalNode();
        for (int blockIdx = dependencyGraph.getBlocks().size() - 1; blockIdx >= 0; blockIdx--) {
            final EquationalBlock<L, AP> block = dependencyGraph.getBlock(blockIdx);
            for (FormulaNode<L, AP> node : block.getNodes()) {
                if (node instanceof TrueNode) {
                    satisfiedVariables.set(node.getVarNumber());
                } else if (node instanceof AtomicNode) {
                    final AP atomicProposition = ((AtomicNode<L, AP>) node).getProposition();
                    final Set<AP> finalNodeAPs = mainMpg.getAtomicPropositions(finalNode);
                    if (finalNodeAPs.contains(atomicProposition)) {
                        satisfiedVariables.set(node.getVarNumber());
                    }
                } else if (node instanceof BoxNode) {
                    /* End node has no outgoing edges */
                    satisfiedVariables.set(node.getVarNumber());
                } else if (node instanceof AndNode) {
                    final AndNode<L, AP> andNode = (AndNode<L, AP>) node;
                    if (satisfiedVariables.get(andNode.getVarNumberLeft()) &&
                        satisfiedVariables.get(andNode.getVarNumberRight())) {
                        satisfiedVariables.set(andNode.getVarNumber());
                    }
                } else if (node instanceof OrNode) {
                    final OrNode<L, AP> orNode = (OrNode<L, AP>) node;
                    if (satisfiedVariables.get(orNode.getVarNumberLeft()) ||
                        satisfiedVariables.get(orNode.getVarNumberRight())) {
                        satisfiedVariables.set(orNode.getVarNumber());
                    }
                } else if (node instanceof NotNode) {
                    final NotNode<L, AP> notNode = (NotNode<L, AP>) node;
                    if (!satisfiedVariables.get(notNode.getVarNumberChild())) {
                        satisfiedVariables.set(notNode.getVarNumber());
                    }
                }
            }
        }
        return satisfiedVariables;
    }

    private <N> void initUpdate(WorkUnit<N, ?> unit, N node) {
        assert !Objects.equals(node, unit.pmpg.getFinalNode()) : "End node must not be updated!";
        unit.workSet.remove(node);
    }

    private <N> T getTransformer(WorkUnit<N, ?> unit, N node) {
        return unit.propTransformers.get(node);
    }

    private <N, E> List<T> createCompositions(WorkUnit<N, E> unit, N node) {
        final ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg = unit.pmpg;
        final List<T> compositions = new ArrayList<>();
        for (E edge : pmpg.getOutgoingEdges(node)) {
            final N targetNode = pmpg.getTarget(edge);
            final T edgeTransformer = getEdgeTransformer(unit, edge);
            final T succTransformer = getTransformer(unit, targetNode);
            final T composition = edgeTransformer.compose(succTransformer);
            compositions.add(composition);
        }
        return compositions;
    }

    private <N> T getUpdatedPropertyTransformer(WorkUnit<N, ?> unit, N node, T nodeTransformer, List<T> compositions) {
        final EquationalBlock<L, AP> currentBlock = dependencyGraph.getBlock(currentBlockIndex);
        final Set<AP> atomicPropositions = unit.pmpg.getAtomicPropositions(node);
        return nodeTransformer.createUpdate(atomicPropositions, compositions, currentBlock);
    }

    private <N> void updateTransformerAndWorkSet(WorkUnit<N, ?> unit, N node, T nodeTransformer, T updatedTransformer) {
        if (!nodeTransformer.equals(updatedTransformer)) {
            unit.propTransformers.put(node, updatedTransformer);
            updateWorkSet(unit, node);
        }
        if (workSetIsEmpty() && currentBlockIndex > 0) {
            currentBlockIndex--;
            resetWorkSet();
        }
    }

    private <E> T getEdgeTransformer(WorkUnit<?, E> unit, E edge) {
        final T edgeTransformer;
        final ProceduralModalProcessGraph<?, L, E, AP, ?> pmpg = unit.pmpg;
        final L label = pmpg.getEdgeLabel(edge);
        if (isProcessEdge(pmpg, edge)) {
            final WorkUnit<?, ?> edgeUnit = workUnits.get(label);
            assert edgeUnit != null;
            edgeTransformer = getInitialEdgeTransformer(edgeUnit);
        } else {
            if (isMustEdge(pmpg, edge)) {
                if (mustTransformers.containsKey(label)) {
                    edgeTransformer = mustTransformers.get(label);
                } else {
                    edgeTransformer = createInitTransformerEdge(dependencyGraph, label, pmpg.getEdgeProperty(edge));
                    mustTransformers.put(label, edgeTransformer);
                }
            } else {
                if (mayTransformers.containsKey(label)) {
                    edgeTransformer = mayTransformers.get(label);
                } else {
                    edgeTransformer = createInitTransformerEdge(dependencyGraph, label, pmpg.getEdgeProperty(edge));
                    mayTransformers.put(label, edgeTransformer);
                }
            }
        }
        return edgeTransformer;
    }

    private <E> boolean isMustEdge(ProceduralModalProcessGraph<?, L, E, AP, ?> pmpg, E edge) {
        return pmpg.getEdgeProperty(edge).isMust();
    }

    private <N> void updateWorkSet(WorkUnit<N, ?> unit, N node) {
        if (Objects.equals(unit.pmpg.getInitialNode(), node)) {
            updateWorkSetStartNode(unit.label);
        }
        addPredecessorsToWorkSet(unit, node);
    }

    private boolean workSetIsEmpty() {
        for (WorkUnit<?, ?> unit : workUnits.values()) {
            if (!unit.workSet.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void resetWorkSet() {
        for (WorkUnit<?, ?> value : this.workUnits.values()) {
            resetWorkSet(value);
        }
    }

    private <N> void resetWorkSet(WorkUnit<N, ?> unit) {
        unit.workSet = newWorkSet(unit.pmpg);
    }

    private <E> boolean isProcessEdge(ProceduralModalProcessGraph<?, L, E, AP, ?> pmpg, E edge) {
        return pmpg.getEdgeProperty(edge).isProcess();
    }

    private <N> T getInitialEdgeTransformer(WorkUnit<N, ?> unit) {
        @SuppressWarnings("nullness") // we have checked non-nullness of initial nodes in the constructor
        final @NonNull N initialNode = unit.pmpg.getInitialNode();
        return unit.propTransformers.get(initialNode);
    }

    private void updateWorkSetStartNode(L label) {
        for (WorkUnit<?, ?> unit : workUnits.values()) {
            updateWorkSetStartNode(unit, label);
        }
    }

    private <N, E> void updateWorkSetStartNode(WorkUnit<N, E> unit, L labelOfUpdatedProcess) {
        final ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg = unit.pmpg;
        for (N node : pmpg) {
            for (E outgoingEdge : pmpg.getOutgoingEdges(node)) {
                if (Objects.equals(pmpg.getEdgeLabel(outgoingEdge), labelOfUpdatedProcess)) {
                    addToWorkSet(unit, node);
                }
            }
        }
    }

    private <N> void addPredecessorsToWorkSet(WorkUnit<N, ?> unit, N node) {
        final Set<N> preds = unit.predecessors.get(node);
        if (preds != null) {
            for (N pred : preds) {
                addToWorkSet(unit, pred);
            }
        }
    }

    private <N> void addToWorkSet(WorkUnit<N, ?> unit, N node) {
        unit.workSet.add(node);
    }

    private <N> Set<N> newWorkSet(ProceduralModalProcessGraph<N, L, ?, AP, ?> pmpg) {
        // Add all nodes to work set except final node, which is never updated
        final Set<N> workset = new HashSet<>(pmpg.getNodes());
        workset.remove(pmpg.getFinalNode());

        return workset;
    }

    private FormulaNode<L, AP> ctlToMuCalc(FormulaNode<L, AP> ctlFormula) {
        CTLToMuCalc<L, AP> transformation = new CTLToMuCalc<>();
        return transformation.toMuCalc(ctlFormula);
    }

    private <N> Mapping<N, List<String>> serializePropertyTransformers(WorkUnit<N, ?> unit) {
        final MutableMapping<N, @Nullable List<String>> result = unit.pmpg.createStaticNodeMapping();
        for (N node : unit.pmpg.getNodes()) {
            result.put(node, serializer.serialize(unit.propTransformers.get(node)));
        }
        // we put a transformer for every node, so it's no longer null
        return (MutableMapping<N, List<String>>) result;
    }

    private void initialize(FormulaNode<L, AP> ast) {
        this.dependencyGraph = new DependencyGraph<>(ast);
        this.currentBlockIndex = dependencyGraph.getBlocks().size() - 1;

        initDDManager(this.dependencyGraph);

        this.serializer = getSerializer();
        this.mustTransformers = new HashMap<>();
        this.mayTransformers = new HashMap<>();

        for (WorkUnit<?, ?> unit : workUnits.values()) {
            initialize(unit);
        }
    }

    private <N> void initialize(WorkUnit<N, ?> unit) {
        unit.workSet = newWorkSet(unit.pmpg);
        unit.propTransformers = initTransformers(unit.pmpg);
    }

    private <N> MutableMapping<N, T> initTransformers(ProceduralModalProcessGraph<N, L, ?, AP, ?> pmpg) {
        final MutableMapping<N, @Nullable T> transformers = pmpg.createStaticNodeMapping();
        final N finalNode = pmpg.getFinalNode();

        for (N n : pmpg) {
            if (Objects.equals(n, finalNode)) {
                transformers.put(n, createInitTransformerEndNode(dependencyGraph));
            } else {
                transformers.put(n, createInitTransformerNode(dependencyGraph));
            }
        }
        return (MutableMapping<N, T>) transformers; // we put a transformer for every node, so it's no longer null
    }

    private boolean isSat() {
        return isSat(workUnits.get(mainProcess));
    }

    private <N> boolean isSat(WorkUnit<N, ?> unit) {
        @SuppressWarnings("nullness") // we have checked non-nullness of initial nodes in the constructor
        final @NonNull N initialNode = unit.pmpg.getInitialNode();
        return isSat(unit, initialNode);
    }

    private <N> boolean isSat(WorkUnit<N, ?> unit, N initialNode) {
        final List<FormulaNode<L, AP>> satisfiedFormulas = getSatisfiedSubformulas(unit, initialNode);
        for (FormulaNode<L, AP> node : satisfiedFormulas) {
            if (node.getVarNumber() == 0) {
                return true;
            }
        }
        return false;
    }

    protected abstract void initDDManager(DependencyGraph<L, AP> dependencyGraph);

    protected abstract <TP extends ModalEdgeProperty> T createInitTransformerEdge(DependencyGraph<L, AP> dependencyGraph,
                                                                                  L edgeLabel,
                                                                                  TP edgeProperty);

    protected abstract T createInitTransformerEndNode(DependencyGraph<L, AP> dependencyGraph);

    protected abstract T createInitTransformerNode(DependencyGraph<L, AP> dependencyGraph);

    protected abstract void shutdownDDManager();

    protected abstract TransformerSerializer<T, L, AP> getSerializer();

    class WorkUnit<N, E> {

        final L label;
        final ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg;
        private final Mapping<N, @Nullable Set<N>> predecessors;
        MutableMapping<N, T> propTransformers;
        private Set<N> workSet; // Keeps track of which node's property transformers have to be updated.

        WorkUnit(L label, ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg, Mapping<N, @Nullable Set<N>> predecessors) {
            this.label = label;
            this.pmpg = pmpg;
            this.predecessors = predecessors;
        }
    }

}
