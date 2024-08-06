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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.Mappings;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.transformer.TransformerSerializer;

/**
 * A class used to store {@link ProceduralModalProcessGraph}-specific data for the {@link SolverHistory}.
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
public final class SolverData<N, T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final ProceduralModalProcessGraph<N, L, ?, AP, ?> pmpg;
    private final NodeIDs<N> nodeIDs;
    private final Mapping<N, List<String>> initialPropertyTransformers;
    private final Mapping<N, List<FormulaNode<L, AP>>> initialSatisfiedSubformulas;

    SolverData(ProceduralModalProcessGraph<N, L, ?, AP, ?> pmpg,
               Mapping<N, List<String>> initialPropertyTransformers,
               Mapping<N, List<FormulaNode<L, AP>>> initialSatisfiedSubformulas) {
        this.pmpg = pmpg;
        this.nodeIDs = pmpg.nodeIDs();
        this.initialPropertyTransformers = initialPropertyTransformers;
        this.initialSatisfiedSubformulas = initialSatisfiedSubformulas;
    }

    /**
     * Returns the {@link ProceduralModalProcessGraph} whose data is stored in an instance of this class.
     *
     * @return the {@link ProceduralModalProcessGraph} whose data is stored in an instance of this class
     */
    public ProceduralModalProcessGraph<N, L, ?, AP, ?> getPmpg() {
        return pmpg;
    }

    /**
     * Returns the {@link NodeIDs} of the {@link ProceduralModalProcessGraph} returned by {@link #getPmpg()}. The
     * nodeIDs have already been computed and cached.
     *
     * @return the nodeIDs
     */
    public NodeIDs<N> getNodeIDs() {
        return nodeIDs;
    }

    /**
     * Returns a {@link Mapping} which maps nodes to their initial property transformer. This methods requires a {@link
     * TransformerSerializer} as all property transform are stored as {@link String}s in this class. The returned map is
     * not cached and will be re-computed on each call.
     *
     * @param serializer
     *         used to deserialize each property transformer from a {@link String}.
     *
     * @return a {@link Mapping} which maps nodes to their initial property transformer
     */
    public Mapping<N, T> getInitialPropertyTransformers(TransformerSerializer<T, L, AP> serializer) {
        final Map<N, T> result = Maps.newHashMapWithExpectedSize(this.pmpg.size());

        for (N n : this.pmpg) {
            result.put(n, serializer.deserialize(this.initialPropertyTransformers.get(n)));
        }

        return Mappings.fromMap(result);
    }

    /**
     * Returns a {@link Mapping} which contains the list of the initial satisfied subformulas for each node of the
     * {@link ProceduralModalProcessGraph} whose data is stored in an instance of this class.
     *
     * @return a {@link Mapping} which contains the list of the initial satisfied subformulas
     */
    public Mapping<N, List<FormulaNode<L, AP>>> getInitialSatisfiedSubformulas() {
        return initialSatisfiedSubformulas;
    }
}
