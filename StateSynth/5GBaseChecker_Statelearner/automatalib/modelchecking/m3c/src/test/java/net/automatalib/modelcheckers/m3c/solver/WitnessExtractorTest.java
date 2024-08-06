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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;

import com.google.common.io.CharStreams;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.visualization.ColorVisualizationHelper;
import net.automatalib.modelcheckers.m3c.visualization.EdgeVisualizationHelper;
import net.automatalib.modelcheckers.m3c.visualization.HTMLVisualizationHelper;
import net.automatalib.modelcheckers.m3c.visualization.NodeVisualizationHelper;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * @author freese
 * @author frohme
 */
public class WitnessExtractorTest {

    @DataProvider
    public static Object[][] formulasOnAnCBn() {
        // @formatter:off
        return new Object[][] {{"mu X.((<b>true) || <>X)", Word.fromSymbols("a", "c", "b")},
                               {"mu X.((<c>true) || <>X || (<b><b>true))", Word.fromLetter("c")},
                               {"mu X.((<c><c>true) || <>X || (<b><b>true))", Word.fromSymbols("a", "a", "c", "b", "b")},
                               {"mu X.(mu Y.(<c><b>true) || <>X || (<b><b>true))", Word.fromSymbols("a", "c", "b")},
                               {"mu X.(<b><b>true || <>X) || mu Y. (<a><a>true || <>Y)", Word.fromSymbols("a", "a")},
                               {"mu X.(<c><b>true || <b>X) || mu Y. (<c><b><b>true || <a>Y)", Word.fromSymbols("a", "a", "c", "b", "b")},
                               {"mu X.((<a>true) || <>X )", Word.fromLetter("a")},
                               {"true || false", Word.epsilon()}};
        // @formatter:on
    }

    @Test(dataProvider = "formulasOnAnCBn")
    public void checkFormulasOnAnCBn(String formula, Word<String> expectedWitness)
            throws IOException, ParserConfigurationException, SAXException, ParseException {

        final ContextFreeModalProcessSystem<String, Void> cfmps = parseCFMPS("/cfmps/witness/an_c_bn.xml");
        final BDDSolver<String, Void> m3c = new BDDSolver<>(cfmps);

        final FormulaNode<String, Void> f = M3CParser.parse(formula, l -> l, ap -> null);
        final WitnessTree<String, Void> tree = m3c.findCounterExample(cfmps, Collections.emptyList(), new NotNode<>(f));

        Assert.assertNotNull(tree);
        Assert.assertEquals(tree.getWitness(), expectedWitness);
    }

    @Test
    public void checkPalindrome() throws ParseException, IOException, ParserConfigurationException, SAXException {

        final ContextFreeModalProcessSystem<String, Void> cfmps = parseCFMPS("/cfmps/palindrome/seed.xml");
        final BDDSolver<String, Void> m3c = new BDDSolver<>(cfmps);

        final FormulaNode<String, Void> f1 = M3CParser.parse("mu X. (<>X || [] false)", l -> l, ap -> null);
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> m3c.findCounterExample(cfmps, Collections.emptyList(), new NotNode<>(f1)));

        final FormulaNode<String, Void> f2 = M3CParser.parse("<S><T><b>true", l -> l, ap -> null);
        Assert.assertNull(m3c.findCounterExample(cfmps, Collections.emptyList(), new NotNode<>(f2)));

        final FormulaNode<String, Void> f3 = M3CParser.parse("<S><><S><T><c>true", l -> l, ap -> null);
        final WitnessTree<String, Void> tree =
                m3c.findCounterExample(cfmps, Collections.emptyList(), new NotNode<>(f3));

        Assert.assertNotNull(tree);
        Assert.assertEquals(tree.getWitness(), Word.fromSymbols("S", "a", "S", "T", "c"));

        final StringWriter sw = new StringWriter();
        GraphDOT.write(tree,
                       sw,
                       new ColorVisualizationHelper(tree),
                       new EdgeVisualizationHelper(tree),
                       new NodeVisualizationHelper(tree),
                       new HTMLVisualizationHelper(tree));
        final String expectedDOT = parseDOT("palindrome.dot");

        Assert.assertEquals(sw.toString(), expectedDOT);
    }

    @Test
    public void checkLoopSystem() throws ParseException, IOException, ParserConfigurationException, SAXException {

        final ContextFreeModalProcessSystem<String, Void> cfmps = parseCFMPS("/cfmps/witness/loop.xml");
        final BDDSolver<String, Void> m3c = new BDDSolver<>(cfmps);

        // good
        final FormulaNode<String, Void> f1 = M3CParser.parse("<a><b><b><c>true", l -> l, ap -> null);
        final WitnessTree<String, Void> t1 = m3c.findCounterExample(cfmps, Collections.emptyList(), new NotNode<>(f1));

        Assert.assertNotNull(t1);
        Assert.assertEquals(t1.getWitness(), Word.fromSymbols("a", "b", "b", "c"));

        // good
        final FormulaNode<String, Void> f2 = M3CParser.parse("<a><b><b><b><c><a><b><b><c>true", l -> l, ap -> null);
        final WitnessTree<String, Void> t2 = m3c.findCounterExample(cfmps, Collections.emptyList(), new NotNode<>(f2));

        Assert.assertNotNull(t2);
        Assert.assertEquals(t2.getWitness(), Word.fromSymbols("a", "b", "b", "b", "c", "a", "b", "b", "c"));

        // should fail on single b in second iteration
        final FormulaNode<String, Void> f3 = M3CParser.parse("<a><b><b><c><a><b><c>true", l -> l, ap -> null);
        final WitnessTree<String, Void> t3 = m3c.findCounterExample(cfmps, Collections.emptyList(), new NotNode<>(f3));

        Assert.assertNull(t3);

        // should fail on single b
        final FormulaNode<String, Void> f4 = M3CParser.parse("<a><b><c>true", l -> l, ap -> null);
        final WitnessTree<String, Void> t4 = m3c.findCounterExample(cfmps, Collections.emptyList(), new NotNode<>(f4));

        Assert.assertNull(t4);
    }

    private ContextFreeModalProcessSystem<String, Void> parseCFMPS(String name)
            throws IOException, ParserConfigurationException, SAXException {
        try (InputStream is = WitnessExtractorTest.class.getResourceAsStream(name)) {
            return ExternalSystemDeserializer.parse(is);
        }
    }

    private String parseDOT(String name) throws IOException {
        try (InputStream is = WitnessExtractorTest.class.getResourceAsStream("/dot/witness/" + name);
             Reader r = IOUtil.asBufferedUTF8Reader(is)) {
            return CharStreams.toString(r);
        }
    }

}
