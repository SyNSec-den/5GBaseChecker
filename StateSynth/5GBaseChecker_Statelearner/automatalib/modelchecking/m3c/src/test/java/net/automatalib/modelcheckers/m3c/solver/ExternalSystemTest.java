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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import net.automatalib.commons.util.IOUtil;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * Test for a set of external systems that have been model-checked externally. Used to assert our model-checker behaves
 * similarly.
 */
public class ExternalSystemTest {

    private final Function<ContextFreeModalProcessSystem<String, String>, M3CSolver<String>> solverProvider;

    @Factory(dataProvider = "solvers")
    public ExternalSystemTest(Function<ContextFreeModalProcessSystem<String, String>, M3CSolver<String>> solverProvider) {
        this.solverProvider = solverProvider;
    }

    @DataProvider
    public static Object[] solvers() {
        Function<ContextFreeModalProcessSystem<String, String>, M3CSolver<String>> addSolver = M3CSolvers::addSolver;
        Function<ContextFreeModalProcessSystem<String, String>, M3CSolver<String>> bddSolver = M3CSolvers::bddSolver;
        return new Function<?, ?>[] {addSolver, bddSolver};
    }

    @Test
    public void testSetting1System1() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting1/system1/");
    }

    @Test
    public void testSetting1System2() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting1/system2/");
    }

    @Test
    public void testSetting1System3() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting1/system3/");
    }

    @Test
    public void testSetting2System1() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting2/system1/");
    }

    @Test
    public void testSetting2System2() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting2/system2/");
    }

    @Test
    public void testSetting2System3() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting2/system3/");
    }

    @Test
    public void testSetting2System4() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting2/system4/");
    }

    @Test
    public void testSetting2System5() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting2/system5/");
    }

    @Test
    public void testSetting2System6() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting2/system6/");
    }

    @Test
    public void testSetting2System7() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting2/system7/");
    }

    @Test
    public void testSetting2System8() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting2/system8/");
    }

    @Test
    public void testSetting2System9() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/setting2/system9/");
    }

    @Test
    public void testPalindrome() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testBenchmark("/cfmps/palindrome/");
    }

    private void testBenchmark(String id)
            throws IOException, ParserConfigurationException, SAXException, ParseException {

        try (InputStream seed = ExternalSystemTest.class.getResourceAsStream(id + "seed.xml");
             InputStream properties = ExternalSystemTest.class.getResourceAsStream(id + "properties.txt");
             InputStream solutions = ExternalSystemTest.class.getResourceAsStream(id + "solutions.txt")) {

            final ContextFreeModalProcessSystem<String, String> cfmps = ExternalSystemDeserializer.parse(seed);
            final M3CSolver<String> solver = this.solverProvider.apply(cfmps);

            try (BufferedReader propertiesReader = new BufferedReader(IOUtil.asUTF8Reader(properties));
                 BufferedReader solutionsReader = new BufferedReader(IOUtil.asUTF8Reader(solutions))) {

                String prop = propertiesReader.readLine();
                String sol = solutionsReader.readLine();

                while (prop != null || sol != null) {
                    Assert.assertNotNull(prop, "Unequal number of properties and solutions");
                    Assert.assertNotNull(sol, "Unequal number of properties and solutions");

                    Assert.assertEquals(solver.solve(prop),
                                        Boolean.parseBoolean(sol),
                                        "Error solving: \"" + prop + "\" with " + solver);

                    prop = propertiesReader.readLine();
                    sol = solutionsReader.readLine();
                }
            }
        }
    }

}
