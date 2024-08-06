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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.MutableProceduralModalProcessGraph;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.graphs.base.DefaultCFMPS;
import net.automatalib.graphs.base.compact.CompactPMPG;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.util.Examples;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractSolverTest<T extends AbstractPropertyTransformer<T, String, String>> {

    @Test
    void testSolve() throws ParseException {
        final M3CSolver<String> solver = getSolver(Examples.getCfmpsAnBn(Collections.emptySet()));

        final String formula = "mu X.(<b><b>true || <>X)";
        assertSolve(solver, formula, true);

        final String negatedFormula = "!(" + formula + ")";
        assertSolve(solver, negatedFormula, false);

        // use !'a' to simulate true, as no node satisfies 'a'
        final String formulaWithNegatedAP = "mu X.(<b><b>!'a' || <>X)";
        assertSolve(solver, formulaWithNegatedAP, true);
    }

    @Test
    void testSolveWithSingleAP() throws ParseException {
        final ContextFreeModalProcessSystem<String, String> cfmps = Examples.getCfmpsAnBn(Collections.singleton("a"));
        final M3CSolver<String> solver = getSolver(cfmps);
        final String formula = "mu X.(<>X || 'a,b')";

        assertSolve(solver, formula, false);
    }

    @Test
    void testSolveWithMultipleAPs() throws ParseException {
        final ContextFreeModalProcessSystem<String, String> cfmps =
                Examples.getCfmpsAnBn(new HashSet<>(Arrays.asList("a", "b")));
        final M3CSolver<String> solver = getSolver(cfmps);

        final String formula = "mu X.(<>X || 'a' && 'b')";
        assertSolve(solver, formula, true);

        final String formulaWithNegatedAP = "mu X.(<>X || !('a' && 'b'))";
        assertSolve(solver, formulaWithNegatedAP, true);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void testSolveWithInvalidMainProcess() {
        ContextFreeModalProcessSystem<String, String> cfmps = new ContextFreeModalProcessSystem<String, String>() {

            @Override
            public Map<String, ProceduralModalProcessGraph<?, String, ?, String, ?>> getPMPGs() {
                return new HashMap<>();
            }

            @Override
            public @Nullable String getMainProcess() {
                return "P";
            }
        };
        getSolver(cfmps);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void testSolveWithNoMainProcess() {
        ContextFreeModalProcessSystem<String, String> cfmps = new ContextFreeModalProcessSystem<String, String>() {

            @Override
            public Map<String, ProceduralModalProcessGraph<?, String, ?, String, ?>> getPMPGs() {
                return new HashMap<>();
            }

            @Override
            public @Nullable String getMainProcess() {
                return null;
            }
        };
        getSolver(cfmps);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void testSolveWithUnguardedProcess() {
        final CompactPMPG<String, String> pmpg = getUnguardedPMPG(new CompactPMPG<>(""));
        getSolver(new DefaultCFMPS<>("P", Collections.singletonMap("P", pmpg)));
    }

    private static <N, E, AP, M extends MutableProceduralModalProcessGraph<N, String, E, AP, ?>> M getUnguardedPMPG(M pmpg) {
        final N start = pmpg.addNode();
        final N end = pmpg.addNode();

        pmpg.setInitialNode(start);
        pmpg.setFinalNode(end);

        final E e1 = pmpg.connect(start, end);
        pmpg.getEdgeProperty(e1).setMust();
        pmpg.setEdgeLabel(e1, "a");

        final E e2 = pmpg.connect(start, end);
        pmpg.getEdgeProperty(e2).setMust();
        pmpg.getEdgeProperty(e2).setProcess();
        pmpg.setEdgeLabel(e2, "P");

        return pmpg;
    }

    public abstract M3CSolver<String> getSolver(ContextFreeModalProcessSystem<String, String> cfmps);

    protected <P> void assertSolve(M3CSolver<P> solver, P property, boolean expectedIsSat) throws ParseException {
        Assert.assertEquals(solver.solve(property), expectedIsSat);
    }

}
