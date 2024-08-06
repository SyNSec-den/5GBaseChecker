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
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AGNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NNFVisitorTest {

    @Test
    void testBaseCases() throws ParseException {
        NNFVisitor<String, String> nnfVisitor = new NNFVisitor<>();

        FormulaNode<String, String> atomicNode = M3CParser.parse("! \"a\"");
        FormulaNode<String, String> nnfAtomicNode = nnfVisitor.transformToNNF(atomicNode);
        Assert.assertEquals(atomicNode, nnfAtomicNode);

        FormulaNode<String, String> trueNode = M3CParser.parse("! true");
        FormulaNode<String, String> nnfTrueNode = nnfVisitor.transformToNNF(trueNode);
        Assert.assertEquals(new FalseNode<>(), nnfTrueNode);

        FormulaNode<String, String> falseNode = M3CParser.parse("! false");
        FormulaNode<String, String> nnfFalseNode = nnfVisitor.transformToNNF(falseNode);
        Assert.assertEquals(new TrueNode<>(), nnfFalseNode);
    }

    @Test
    void testGfp() throws ParseException {
        FormulaNode<String, String> gfpNode = M3CParser.parse("! (nu X.(false || X))");
        FormulaNode<String, String> nnfGfpNode = gfpNode.toNNF();

        /* Create (mu X.(true & X) */
        LfpNode<String, String> lfpNode = new LfpNode<>("X", new AndNode<>(new TrueNode<>(), new VariableNode<>("X")));
        Assert.assertEquals(lfpNode, nnfGfpNode);
    }

    @Test
    void testLfp() throws ParseException {
        FormulaNode<String, String> lfpNode = M3CParser.parse("! (mu X.(false || !X))");
        FormulaNode<String, String> nnfLfpNode = lfpNode.toNNF();

        /* Create nu X.(true & !X) */
        GfpNode<String, String> gfpNode =
                new GfpNode<>("X", new AndNode<>(new TrueNode<>(), new NotNode<>(new VariableNode<>("X"))));
        Assert.assertEquals(gfpNode, nnfLfpNode);
    }

    @Test
    void testAnd() throws ParseException {
        FormulaNode<String, String> andNode = M3CParser.parse("!(<> false && true)");
        FormulaNode<String, String> nnfAndNode = andNode.toNNF();

        /* Create ([]true | false) */
        OrNode<String, String> orNode = new OrNode<>(new BoxNode<>(new TrueNode<>()), new FalseNode<>());
        Assert.assertEquals(orNode, nnfAndNode);
    }

    @Test
    void testOr() throws ParseException {
        FormulaNode<String, String> orNode = M3CParser.parse("!([a] false || true)");
        FormulaNode<String, String> nnfOrNode = orNode.toNNF();

        /* Create (<a> true & false) */
        AndNode<String, String> andNode = new AndNode<>(new DiamondNode<>("a", new TrueNode<>()), new FalseNode<>());
        Assert.assertEquals(andNode, nnfOrNode);
    }

    @Test
    void testBoxNode() throws ParseException {
        FormulaNode<String, String> boxNode = M3CParser.parse("![a]true");
        FormulaNode<String, String> nnfBoxNode = boxNode.toNNF();

        /* Create (<a>false)*/
        DiamondNode<String, String> diamondNode = new DiamondNode<>("a", new FalseNode<>());
        Assert.assertEquals(diamondNode, nnfBoxNode);
    }

    @Test
    void testDiamondNode() throws ParseException {
        FormulaNode<String, String> diamondNode = M3CParser.parse("!<a>false");
        FormulaNode<String, String> nnfDiamondNode = diamondNode.toNNF();

        /* Create ([a] true) */
        BoxNode<String, String> boxNode = new BoxNode<>("a", new TrueNode<>());
        Assert.assertEquals(boxNode, nnfDiamondNode);
    }

    @Test
    void testDefaultExample() throws ParseException {
        FormulaNode<String, String> ast = M3CParser.parse("!(mu X.(<b><b>true || <>X))");
        FormulaNode<String, String> nnfAst = ast.toNNF();

        /* Create nu X.([b][b]false & []X)*/
        GfpNode<String, String> gfpNode = new GfpNode<>("X",
                                                        new AndNode<>(new BoxNode<>("b",
                                                                                    new BoxNode<>("b",
                                                                                                  new FalseNode<>())),
                                                                      new BoxNode<>(new VariableNode<>("X"))));
        Assert.assertEquals(gfpNode, nnfAst);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void testError() {
        FormulaNode<String, String> ast = new AGNode<>(new TrueNode<>());
        ast.toNNF();
    }

}
