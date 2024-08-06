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
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CTLToMuCalcTest {

    private final CTLToMuCalc<String, String> transformer = new CTLToMuCalc<>();

    @Test
    void testTrue() throws ParseException {
        equals("true", new TrueNode<>());
    }

    @Test
    void testFalse() throws ParseException {
        equals("false", new FalseNode<>());
    }

    @Test
    void testAtomicProposition() throws ParseException {
        equals("\"p\"", new AtomicNode<>("p"));
    }

    @Test
    void testNegation() throws ParseException {
        equals("!\"p\"", new NotNode<>(new AtomicNode<>("p")));
    }

    @Test
    void testBox() throws ParseException {
        equals("[abc]true", new BoxNode<>("abc", new TrueNode<>()));
    }

    @Test
    void testDiamond() throws ParseException {
        equals("<>false", new DiamondNode<>(new FalseNode<>()));
    }

    @Test
    void testOr() throws ParseException {
        equals("\"p\" || \"q\"", new OrNode<>(new AtomicNode<>("p"), new AtomicNode<>("q")));
    }

    @Test
    void testAnd() throws ParseException {
        equals("\"p\" && \"q\"", new AndNode<>(new AtomicNode<>("p"), new AtomicNode<>("q")));
    }

    @Test
    void testAF() throws ParseException {
        DiamondNode<String, String> diamond = new DiamondNode<>(new TrueNode<>());
        BoxNode<String, String> box = new BoxNode<>(new VariableNode<>("Z0"));
        AndNode<String, String> and = new AndNode<>(diamond, box);
        LfpNode<String, String> expected = new LfpNode<>("Z0", new OrNode<>(new AtomicNode<>("p"), and));
        equals("AF \"p\"", expected);
    }

    @Test
    void testAG() throws ParseException {
        AndNode<String, String> and = new AndNode<>(new AtomicNode<>("p"), new BoxNode<>(new VariableNode<>("Z0")));
        GfpNode<String, String> expected = new GfpNode<>("Z0", and);
        equals("AG \"p\"", expected);
    }

    @Test
    void testAU() throws ParseException {
        DiamondNode<String, String> diamond = new DiamondNode<>(new TrueNode<>());
        BoxNode<String, String> box = new BoxNode<>(new VariableNode<>("Z0"));
        AndNode<String, String> innerAnd = new AndNode<>(diamond, box);
        AndNode<String, String> outerAnd = new AndNode<>(new AtomicNode<>("p"), innerAnd);
        OrNode<String, String> or = new OrNode<>(new AtomicNode<>("q"), outerAnd);
        LfpNode<String, String> expected = new LfpNode<>("Z0", or);
        equals("A(\"p\" U \"q\")", expected);
    }

    @Test
    void testAWU() throws ParseException {
        /* A[p WU q] = !E[!q U (!p & !q)] */
        /* !E[!q U (!p & !q)] = !(mu X.((!p & !q) | (!q & <>X))) */
        NotNode<String, String> notQ = new NotNode<>(new AtomicNode<>("q"));
        NotNode<String, String> notP = new NotNode<>(new AtomicNode<>("p"));
        AndNode<String, String> notPAndNotQ = new AndNode<>(notP, notQ);
        AndNode<String, String> innerAnd = new AndNode<>(notQ, new DiamondNode<>(new VariableNode<>("Z0")));
        OrNode<String, String> or = new OrNode<>(notPAndNotQ, innerAnd);
        LfpNode<String, String> lfpNode = new LfpNode<>("Z0", or);
        NotNode<String, String> expected = new NotNode<>(lfpNode);
        equals("A(\"p\" W \"q\")", expected);
    }

    @Test
    void testEF() throws ParseException {
        OrNode<String, String> or = new OrNode<>(new AtomicNode<>("p"), new DiamondNode<>(new VariableNode<>("Z0")));
        LfpNode<String, String> expected = new LfpNode<>("Z0", or);
        equals("EF \"p\"", expected);
    }

    @Test
    void testEG() throws ParseException {
        OrNode<String, String> or =
                new OrNode<>(new DiamondNode<>(new VariableNode<>("Z0")), new BoxNode<>(new FalseNode<>()));
        GfpNode<String, String> expected = new GfpNode<>("Z0", new AndNode<>(new AtomicNode<>("p"), or));
        equals("EG \"p\"", expected);
    }

    @Test
    void testEU() throws ParseException {
        AndNode<String, String> and = new AndNode<>(new AtomicNode<>("p"), new DiamondNode<>(new VariableNode<>("Z0")));
        LfpNode<String, String> expected = new LfpNode<>("Z0", new OrNode<>(new TrueNode<>(), and));
        equals("E(\"p\" U true)", expected);
    }

    @Test
    void testEWU() throws ParseException {
        AndNode<String, String> and = new AndNode<>(new AtomicNode<>("p"), new DiamondNode<>(new VariableNode<>("Z0")));
        LfpNode<String, String> lfp = new LfpNode<>("Z0", new OrNode<>(new AtomicNode<>("q"), and));
        OrNode<String, String> or =
                new OrNode<>(new DiamondNode<>(new VariableNode<>("Z1")), new BoxNode<>(new FalseNode<>()));
        GfpNode<String, String> gfp = new GfpNode<>("Z1", new AndNode<>(new AtomicNode<>("p"), or));
        equals("E(\"p\" W \"q\")", new OrNode<>(lfp, gfp));
    }

    private void equals(String inputFormula, FormulaNode<String, String> expectedResult) throws ParseException {
        FormulaNode<String, String> ctlNode = M3CParser.parse(inputFormula);
        Assert.assertEquals(expectedResult, transformer.toMuCalc(ctlNode));
    }
}
