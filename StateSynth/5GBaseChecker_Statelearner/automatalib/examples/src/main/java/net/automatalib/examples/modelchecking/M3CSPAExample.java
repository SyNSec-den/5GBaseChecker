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
package net.automatalib.examples.modelchecking;

import java.util.function.Function;

import net.automatalib.automata.spa.CFMPSView;
import net.automatalib.automata.spa.SPA;
import net.automatalib.examples.spa.PalindromeExample;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.solver.M3CSolver;
import net.automatalib.modelcheckers.m3c.solver.M3CSolver.TypedM3CSolver;
import net.automatalib.modelcheckers.m3c.solver.M3CSolvers;
import net.automatalib.visualization.Visualization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example in which we transform the {@link PalindromeExample palindrome SPA} into a {@link
 * ContextFreeModalProcessSystem} and use the {@link M3CSolver} to evaluate properties on the system.
 *
 * @author frohme
 */
public final class M3CSPAExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(M3CSPAExample.class);

    private M3CSPAExample() {
        // prevent initialization
    }

    public static void main(String[] args) throws ParseException {
        final SPA<?, Character> spa = PalindromeExample.buildSPA();
        final CFMPSView<Character> view = new CFMPSView<>(spa);

        //@formatter:off
        final String[] formulas = {"EF <b><b>true", // there exists a path with "bb"
                                   "EF <b><S><b>true", // there exists a path with "bSb"
                                   "EF <b><T><b>true", // there exists a path with "bTb"
                                   "<S><T><a>true", // There exists a starting sequence of STa
                                   "<S><T><b>true", // There exists a starting sequence of STb
                                   "<S><T><c>true", // There exists a starting sequence of STc
                                   "<S><a><T><c>true", // There exists a starting sequence of SaTc
                                   "<S><a><S><T><c>true", // There exists a starting sequence of SaSTc
                                   "EF [] false", // there exists a path to the final state
                                   "AF [] false", // all paths reach the final state
                                   "AG EF [] false", // on all paths there exists a path to the final state
                                   "AG ([S] (AF <R>true))", // globally, every S path must be followed by an R eventually
        };
        //@formatter:on

        final Function<String, Character> labelParser = s -> s.charAt(0);
        final Function<String, Void> apParser = s -> null;
        final TypedM3CSolver<FormulaNode<Character, Void>> solver = M3CSolvers.typedSolver(view);

        for (String f : formulas) {
            final FormulaNode<Character, Void> formula = M3CParser.parse(f, labelParser, apParser);
            LOGGER.info("Is '{}' satisfied? {}", f, solver.solve(formula));
        }

        Visualization.visualize(view);
    }
}
