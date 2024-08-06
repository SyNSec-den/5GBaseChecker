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

import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.solver.M3CSolver.TypedM3CSolver;

/**
 * A factory for constructing {@link M3CSolver}s depending on the given {@link ContextFreeModalProcessSystem}.
 *
 * @author frohme
 */
public final class M3CSolvers {

    private M3CSolvers() {
        // prevent instantiation
    }

    /**
     * Returns a default {@link M3CSolver} solver for string-based modal context-free process systems. This method
     * currently delegates solver construction to {@link #bddSolver(ContextFreeModalProcessSystem)}.
     *
     * @param cfmps
     *         the system to evaluate formulas on
     *
     * @return a default {@link M3CSolver} solver for string-based modal context-free process systems
     *
     * @see #bddSolver(ContextFreeModalProcessSystem)
     */
    public static M3CSolver<String> solver(ContextFreeModalProcessSystem<String, String> cfmps) {
        return bddSolver(cfmps);
    }

    /**
     * Returns a default {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems. This
     * method currently delegates solver construction to {@link #typedBDDSolver(ContextFreeModalProcessSystem)}.
     *
     * @param cfmps
     *         the system to evaluate formulas on
     *
     * @return a default {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems
     *
     * @see #typedBDDSolver(ContextFreeModalProcessSystem)
     */
    public static <L, AP> TypedM3CSolver<FormulaNode<L, AP>> typedSolver(ContextFreeModalProcessSystem<L, AP> cfmps) {
        return typedBDDSolver(cfmps);
    }

    /**
     * Returns an ADD-backed {@link M3CSolver} solver for string-based modal context-free process systems.
     *
     * @param cfmps
     *         the system to evaluate formulas on
     *
     * @return an ADD-backed {@link M3CSolver} solver for string-based modal context-free process systems
     */
    public static M3CSolver<String> addSolver(ContextFreeModalProcessSystem<String, String> cfmps) {
        return new StringADDSolver(cfmps);
    }

    /**
     * Returns an ADD-backed {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems.
     *
     * @param cfmps
     *         the system to evaluate formulas on
     *
     * @return an ADD-backed {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems
     */
    public static <L, AP> TypedM3CSolver<FormulaNode<L, AP>> typedADDSolver(ContextFreeModalProcessSystem<L, AP> cfmps) {
        return new TypedADDSolver<>(cfmps);
    }

    /**
     * Returns a BDD-backed {@link M3CSolver} solver for string-based {@link ContextFreeModalProcessSystem}.
     *
     * @param cfmps
     *         the system to evaluate formulas on
     *
     * @return an ADD-backed {@link M3CSolver} for string-based systems
     */
    public static M3CSolver<String> bddSolver(ContextFreeModalProcessSystem<String, String> cfmps) {
        return new StringBDDSolver(cfmps);
    }

    /**
     * Returns a BDD-backed {@link TypedM3CSolver} solver for strongly-typed {@link ContextFreeModalProcessSystem}.
     *
     * @param cfmps
     *         the system to evaluate formulas on
     *
     * @return a BDD-backed {@link TypedM3CSolver} for strongly-typed systems
     */
    public static <L, AP> TypedM3CSolver<FormulaNode<L, AP>> typedBDDSolver(ContextFreeModalProcessSystem<L, AP> cfmps) {
        return new TypedBDDSolver<>(cfmps);
    }
}
