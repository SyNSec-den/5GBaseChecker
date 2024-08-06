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

import java.util.BitSet;

import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A utility class that represents a current configuration (node property) in the {@link WitnessTree}.
 *
 * @param <N>
 *         node type of the referenced {@link ProceduralModalProcessGraph}
 * @param <L>
 *         label type
 * @param <E>
 *         edge type of the referenced {@link ProceduralModalProcessGraph}
 * @param <AP>
 *         atomic proposition type
 *
 * @author freese
 * @author frohme
 */
public class WitnessTreeState<N, L, E, AP> {

    public final @Nullable WitnessTreeState<?, L, ?, AP> stack;
    public final AbstractDDSolver<?, L, AP>.WorkUnit<N, E> unit;
    public final L procedure;
    public final ProceduralModalProcessGraph<N, L, E, AP, ?> pmpg;
    public final N state;
    public final FormulaNode<L, AP> subformula;
    public final BitSet context;
    public final String displayLabel;
    public final @Nullable L edgeLabel;
    public final int parentId;
    public boolean isPartOfResult;

    WitnessTreeState(@Nullable WitnessTreeState<?, L, ?, AP> stack,
                     AbstractDDSolver<?, L, AP>.WorkUnit<N, E> unit,
                     N state,
                     FormulaNode<L, AP> subformula,
                     BitSet context,
                     String displayLabel,
                     @Nullable L edgeLabel,
                     int parentId) {
        this.stack = stack;
        this.unit = unit;
        this.procedure = unit.label;
        this.pmpg = unit.pmpg;
        this.state = state;
        this.subformula = subformula;
        this.context = context;

        this.edgeLabel = edgeLabel;
        this.parentId = parentId;
        this.displayLabel = displayLabel;
        this.isPartOfResult = false;
    }

    BitSet getSatisfiedSubformulae(DependencyGraph<L, AP> dependencyGraph, N node) {
        return unit.propTransformers.get(node).evaluate(dependencyGraph.toBoolArray(context));
    }
}
