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
package net.automatalib.modelcheckers.m3c.formula.parser;

import java.io.StringReader;
import java.util.function.Function;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;

/**
 * This class can be used to parse formulas in CTL and the mu-calculus.
 *
 * <ul>
 *     <li>
 * CTL grammar:
 * <pre>
 * f := true | false | AP | (f) |
 *      !f | f &amp;&amp; f | f || f | f -&gt; f | f &lt;-&gt; f |
 *      &lt;&gt;f | &lt;ID&gt;f | []f | [ID]f |
 *      AF f | AG f | A(f U f) | A(f W f) | EF f | EG f | E(f U f) | E(f W f)
 * </pre>
 *     </li>
 *     <li>
 * mu-calculus grammar:
 * <pre>
 * f := true | false | AP | (f) |
 *      !f | f &amp;&amp; f | f || f | f -&gt; f | f &lt;-&gt; f |
 *      &lt;&gt;f | &lt;ID&gt;f | []f | [ID]f |
 *      mu ID.(f) | nu ID.(f) | ID
 * </pre>
 *     </li>
 * </ul>
 * <pre>
 * AP := "arbitrary string not containing double quotation marks" | 'arbitrary string not containing single quotation marks'
 * ID := ["a"-"z","A"-"Z"] (["a"-"z","A"-"Z"] | ["0"-"9"] | "_")*
 * </pre>
 */
public final class M3CParser {

    private M3CParser() {
        // prevent initialization
    }

    /**
     * Returns the abstract syntax tree of a given formula. Each label is transformed to a {@code String}. Each atomic
     * proposition is transformed to a {@code String}.
     *
     * @param formula
     *         ctl or mu-calculus formula to be parsed
     *
     * @return {@code formula}'s abstract syntax tree.
     *
     * @throws ParseException
     *         if {@code formula} is not a valid formula.
     */
    public static FormulaNode<String, String> parse(String formula) throws ParseException {
        return parse(formula, Function.identity(), Function.identity());
    }

    /**
     * Returns the abstract syntax tree of a given formula. Each label and atomic proposition is transformed according
     * to the given parsers.
     *
     * @param formula
     *         ctl or mu-calculus formula to be parsed
     * @param labelParser
     *         parser to transform the labels of the formula
     * @param apParser
     *         parser to transform the atomic propositions of the formula
     * @param <L>
     *         edge label type
     * @param <AP>
     *         atomic proposition type
     *
     * @return {@code formula}'s abstract syntax tree.
     *
     * @throws ParseException
     *         if {@code formula} is not a valid formula.
     */
    public static <L, AP> FormulaNode<L, AP> parse(String formula,
                                                   Function<String, L> labelParser,
                                                   Function<String, AP> apParser) throws ParseException {
        try (StringReader reader = new StringReader(formula)) {
            final InternalM3CParser<L, AP> parser = new InternalM3CParser<>(reader);
            return parser.parse(labelParser, apParser);
        }
    }

}
