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
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.transformer.TransformerSerializer;

/**
 * Stores internal information produced during the update of a node in {@link AbstractDDSolver}.
 *
 * @param <N>
 *         node type
 * @param <T>
 *         property transformer type
 * @param <L>
 *         edge label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public final class SolverState<N, T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final List<String> updatedPropTransformer;
    private final List<List<String>> compositions;
    private final List<FormulaNode<L, AP>> updatedNodeSatisfiedSubformula;
    private final N updatedNode;
    private final L updatedNodePMPG;
    private final Map<L, Set<?>> workSet;

    SolverState(List<String> updatedPropTransformer,
                List<List<String>> compositions,
                N updatedNode,
                L updatedNodePMPG,
                Map<L, Set<?>> workSet,
                List<FormulaNode<L, AP>> updatedNodeSatisfiedSubformula) {
        this.updatedPropTransformer = updatedPropTransformer;
        this.compositions = compositions;
        this.updatedNode = updatedNode;
        this.updatedNodePMPG = updatedNodePMPG;
        this.workSet = workSet;
        this.updatedNodeSatisfiedSubformula = updatedNodeSatisfiedSubformula;
    }

    /**
     * Returns the updated property transformer. This method requires a {@link TransformerSerializer} as all property
     * transform are stored as {@link String}s in this class.
     *
     * @param serializer
     *         used to deserialize a property transformer from a {@link String}.
     *
     * @return the updated property transformer
     */
    public T getUpdatedPropTransformer(TransformerSerializer<T, L, AP> serializer) {
        return serializer.deserialize(this.updatedPropTransformer);
    }

    /**
     * Returns a {@link List} of the property transformers representing the compositions of the property transformer of
     * the outgoing edges and their target nodes. This method requires a {@link TransformerSerializer} as all property
     * transform are stored as {@link String}s in this class.
     *
     * @param serializer
     *         used to deserialize a property transformer from a {@link String}.
     *
     * @return the property transformers representing the compositions of the property transformer of the outgoing edges
     * and their target nodes.
     */
    public List<T> getCompositions(TransformerSerializer<T, L, AP> serializer) {
        final List<T> result = new ArrayList<>(this.compositions.size());

        for (List<String> c : this.compositions) {
            result.add(serializer.deserialize(c));
        }

        return result;
    }

    /**
     * Returns the list of satisfied subformulas the node updated in this step satisfies after the update.
     *
     * @return the list of satisfied subformulas
     */
    public List<FormulaNode<L, AP>> getUpdatedNodeSatisfiedSubformula() {
        return updatedNodeSatisfiedSubformula;
    }

    /**
     * Returns the node updated in this step.
     *
     * @return the node updated in this step
     */
    public N getUpdatedNode() {
        return updatedNode;
    }

    /**
     * Returns the name of the {@link ProceduralModalProcessGraph} which contains the node updated in this step.
     *
     * @return the name of the {@link ProceduralModalProcessGraph} which contains the node updated in this step
     */
    public L getUpdatedNodePMPG() {
        return updatedNodePMPG;
    }

    /**
     * Returns a {@link Map} which returns the set of nodes which are in the work set for each procedure after the
     * update.
     *
     * @return a {@link Map} which returns the set of nodes which are in the work set for each procedure after the
     * update
     */
    public Map<L, Set<?>> getWorkSet() {
        return workSet;
    }

}
