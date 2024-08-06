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
package net.automatalib.modelcheckers.m3c.formula.visitor;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FormulaNodeToStringTest {

    @Test
    void testBaseCases() throws ParseException {
        // CTL
        testCorrectness("true");
        testCorrectness("false");
        testCorrectness("!true");
        testCorrectness("\"a,b,c\"");
        testCorrectness("AF true");
        testCorrectness("AG true");
        testCorrectness("A(false U true)");
        testCorrectness("A(false W true)");
        testCorrectness("EF true");
        testCorrectness("EG true");
        testCorrectness("E(false U true)");
        testCorrectness("E(false W true)");
        testCorrectness("true || false");
        testCorrectness("true && false");

        // mu-calc
        testCorrectness("mu X.(\"a\" || <>X)");
        testCorrectness("nu X.(\"a\" || <>X)");
        testCorrectness("mu X.(\"a\" || [b]X)");
        testCorrectness("nu X.(\"a\" || [b]X)");
    }

    private void testCorrectness(String formula) throws ParseException {
        FormulaNode<String, String> ast = M3CParser.parse(formula);
        String astToString = ast.toString();
        FormulaNode<String, String> backToAst = M3CParser.parse(astToString);
        Assert.assertEquals(ast, backToAst);
    }
}
