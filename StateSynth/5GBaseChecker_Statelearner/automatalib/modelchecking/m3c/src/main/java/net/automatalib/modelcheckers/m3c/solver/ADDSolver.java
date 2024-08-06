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

import info.scce.addlib.dd.xdd.XDDManager;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVector;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVectorLogicDDManager;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.transformer.ADDTransformer;
import net.automatalib.modelcheckers.m3c.transformer.ADDTransformerSerializer;
import net.automatalib.modelcheckers.m3c.transformer.TransformerSerializer;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;

/**
 * Implementation based on property transformers being represented by ADDs (Algebraic Decision Diagrams).
 *
 * @param <L>
 *         edge label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public class ADDSolver<L, AP> extends AbstractDDSolver<ADDTransformer<L, AP>, L, AP> {

    private XDDManager<BooleanVector> ddManager;

    public ADDSolver(ContextFreeModalProcessSystem<L, AP> cfmps) {
        super(cfmps);
    }

    @Override
    protected void initDDManager(DependencyGraph<L, AP> dependencyGraph) {
        this.ddManager = new BooleanVectorLogicDDManager(dependencyGraph.getNumVariables());
    }

    @Override
    protected ADDTransformer<L, AP> createInitTransformerEndNode(DependencyGraph<L, AP> dependencyGraph) {
        return new ADDTransformer<>(ddManager);
    }

    @Override
    protected ADDTransformer<L, AP> createInitTransformerNode(DependencyGraph<L, AP> dependencyGraph) {
        return new ADDTransformer<>(ddManager, dependencyGraph);
    }

    @Override
    protected <TP extends ModalEdgeProperty> ADDTransformer<L, AP> createInitTransformerEdge(DependencyGraph<L, AP> dependencyGraph,
                                                                                             L edgeLabel,
                                                                                             TP edgeProperty) {
        return new ADDTransformer<>(ddManager, edgeLabel, edgeProperty, dependencyGraph);
    }

    @Override
    protected void shutdownDDManager() {
        this.ddManager.quit();
    }

    @Override
    protected TransformerSerializer<ADDTransformer<L, AP>, L, AP> getSerializer() {
        return new ADDTransformerSerializer<>(ddManager);
    }
}
