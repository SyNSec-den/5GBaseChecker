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

import java.util.ArrayList;
import java.util.List;

import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParserMuCalcTest {

    private final List<FormulaNode<String, String>> formulas = new ArrayList<>();

    @Test
    public void testBaseCases() throws ParseException {
        assertEquals("false", new FalseNode<>());
        assertEquals("true", new TrueNode<>());
        assertEquals("true && true", new AndNode<>(new TrueNode<>(), new TrueNode<>()));
        assertEquals("true || false", new OrNode<>(new TrueNode<>(), new FalseNode<>()));
        assertEquals("'a'", new AtomicNode<>("a"));
        assertEquals("\"b\"", new AtomicNode<>("b"));
        assertEquals("\"a\" && \"b\" && \"c\"",
                     new AndNode<>(new AtomicNode<>("a"), new AndNode<>(new AtomicNode<>("b"), new AtomicNode<>("c"))));
        assertEquals("true -> false", new OrNode<>(new NotNode<>(new TrueNode<>()), new FalseNode<>()));
        assertEquals("true <-> false",
                     new AndNode<>(new OrNode<>(new NotNode<>(new TrueNode<>()), new FalseNode<>()),
                                   new OrNode<>(new NotNode<>(new FalseNode<>()), new TrueNode<>())));
        assertEquals("!false", new NotNode<>(new FalseNode<>()));
        assertEquals("[]true", new BoxNode<>(new TrueNode<>()));
        assertEquals("<>false", new DiamondNode<>(new FalseNode<>()));
        assertEquals("<ab>false", new DiamondNode<>("ab", new FalseNode<>()));
        assertEquals("[ab]true", new BoxNode<>("ab", new TrueNode<>()));
        assertEquals("mu XY.(XY || false)",
                     new LfpNode<>("XY", new OrNode<>(new VariableNode<>("XY"), new FalseNode<>())));
        assertEquals("nu ZY.(ZY || false)",
                     new GfpNode<>("ZY", new OrNode<>(new VariableNode<>("ZY"), new FalseNode<>())));
    }

    @Test
    public void testNestedFixPoints() throws ParseException {
        assertEquals("nu X. ([]X && mu Y. (<>Y || (\"AP\" && [] false)))",
                     new GfpNode<>("X",
                                   new AndNode<>(new BoxNode<>(new VariableNode<>("X")),
                                                 new LfpNode<>("Y",
                                                               new OrNode<>(new DiamondNode<>(new VariableNode<>("Y")),
                                                                            new AndNode<>(new AtomicNode<>("AP"),
                                                                                          new BoxNode<>(new FalseNode<>())))))));
        assertEquals("nu X. ([]X && (<S>true -> mu Y. (<S>Y || <R>true)))",
                     new GfpNode<>("X",
                                   new AndNode<>(new BoxNode<>(new VariableNode<>("X")),
                                                 new OrNode<>(new NotNode<>(new DiamondNode<>("S", new TrueNode<>())),
                                                              new LfpNode<>("Y",
                                                                            new OrNode<>(new DiamondNode<>("S",
                                                                                                           new VariableNode<>(
                                                                                                                   "Y")),
                                                                                         new DiamondNode<>("R",
                                                                                                           new TrueNode<>())))))));
    }

    @Test
    public void testPrecedence() throws ParseException {
        assertEquals("true || false && true", "true || (false && true)");
        assertEquals("true -> false || true", "true -> (false || true)");
        assertEquals("true <-> false -> true", "true <-> (false -> true)");
        assertEquals("!true && false", "(!true) && false");
        assertEquals("nu X.(X || true) && false", "(nu X.(X || true)) && false");
        assertEquals("mu X.(X || true) && false", "(mu X.(X || true)) && false");
        assertEquals("[]true && true", "([]true) && true");
        assertEquals("[b]true && true", "([b]true) && true");
        assertEquals("<>true && true", "(<>true) && true");
        assertEquals("<b>true && true", "(<b>true) && true");

    }

    @Test(dependsOnMethods = {"testBaseCases", "testNestedFixPoints", "testPrecedence"})
    public void testEqualities() {
        for (FormulaNode<String, String> n1 : formulas) {
            for (FormulaNode<String, String> n2 : formulas) {
                Assert.assertEquals(n1.equals(n2), n1 == n2, "n1: " + n1 + ", n2: " + n2);
            }
        }
    }

    private void assertEquals(String actualMuCalcFormula, String expectedMuCalcFormula) throws ParseException {
        assertEquals(actualMuCalcFormula, M3CParser.parse(expectedMuCalcFormula));
    }

    private void assertEquals(String muCalcFormula, FormulaNode<String, String> expectedAST) throws ParseException {
        FormulaNode<String, String> actualAST = M3CParser.parse(muCalcFormula);
        Assert.assertEquals(actualAST, expectedAST);
        Assert.assertEquals(actualAST.hashCode(), expectedAST.hashCode());

        this.formulas.add(actualAST);
    }

}
