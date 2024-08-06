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
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.transformer.TransformerSerializer;

/**
 * A class used to store internal information produced by {@link AbstractDDSolver#solveAndRecordHistory} while checking
 * the satisfiability of a formula. The internal information can be used for debugging or visualization purposes.
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
public final class SolverHistory<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final Map<L, SolverData<?, T, L, AP>> data;
    private final Map<L, List<String>> mustTransformers;
    private final Map<L, List<String>> mayTransformers;
    private final List<SolverState<?, T, L, AP>> solverStates;
    private final boolean isSat;

    SolverHistory(Map<L, SolverData<?, T, L, AP>> data,
                  Map<L, List<String>> mustTransformers,
                  Map<L, List<String>> mayTransformers,
                  List<SolverState<?, T, L, AP>> solverStates,
                  boolean isSat) {
        this.data = data;
        this.mustTransformers = mustTransformers;
        this.mayTransformers = mayTransformers;
        this.solverStates = solverStates;
        this.isSat = isSat;
    }

    /**
     * Returns a {@link Map} containing information per procedure name.
     *
     * @return a {@link Map} containing information per procedure name
     */
    public Map<L, SolverData<?, T, L, AP>> getData() {
        return data;
    }

    /**
     * Returns a {@link Map} which maps the edge label of must edges to their property transformer. This method requires
     * a {@link TransformerSerializer} as all property transform are stored as {@link String}s in this class. The
     * returned map is not cached and will be computed on each call. The property transformer of an edge is initialized
     * once and will not be modified which is why this map is only stored once and not in each {@link SolverState}.
     *
     * @param serializer
     *         used to deserialize each property transformer from a {@link String}
     *
     * @return a {@link Map} which maps the edge label of must edges to their property transformer
     */
    public Map<L, T> getMustTransformers(TransformerSerializer<T, L, AP> serializer) {
        return transform(this.mustTransformers, serializer);
    }

    /**
     * Returns a {@link Map} which maps the edge label of may edges to their property transformer. This method requires
     * a {@link TransformerSerializer} as all property transform are stored as {@link String}s in this class. The
     * returned map is not cached and will be computed on each call. The property transformer of an edge is initialized
     * once and will not be modified which is why this map is only stored once and not in each {@link SolverState}.
     *
     * @param serializer
     *         used to deserialize each property transformer from a {@link String}
     *
     * @return a {@link Map} which maps the edge label of may edges to their property transformer
     */
    public Map<L, T> getMayTransformers(TransformerSerializer<T, L, AP> serializer) {
        return transform(this.mayTransformers, serializer);
    }

    /**
     * Returns the list of {@link SolverState}s, one per update of a node.
     *
     * @return the list of {@link SolverState}s
     */
    public List<SolverState<?, T, L, AP>> getSolverStates() {
        return solverStates;
    }

    /**
     * Returns whether the formula put into {@link AbstractDDSolver#solveAndRecordHistory(FormulaNode)} is satisfied.
     *
     * @return {@code true} if formula is satisfied, {@code false} otherwise
     */
    public boolean isSat() {
        return isSat;
    }

    private Map<L, T> transform(Map<L, List<String>> input, TransformerSerializer<T, L, AP> serializer) {
        final Map<L, T> result = Maps.newHashMapWithExpectedSize(input.size());

        for (Entry<L, List<String>> e : input.entrySet()) {
            result.put(e.getKey(), serializer.deserialize(e.getValue()));
        }

        return result;
    }
}
