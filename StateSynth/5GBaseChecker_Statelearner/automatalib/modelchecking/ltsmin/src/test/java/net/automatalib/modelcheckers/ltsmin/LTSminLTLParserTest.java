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
package net.automatalib.modelcheckers.ltsmin;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class LTSminLTLParserTest {

    @DataProvider(name = "letter")
    public Object[][] letterFormulae() {
        final List<Object[]> formulae = new ArrayList<>();

        // test DFA/Mealy label checks
        formulae.add(new Object[] {"[] (letter == \"1\" -> X letter == \"2\")", true});
        formulae.add(new Object[] {"[] (output == \"1\" -> X output == \"2\")", false});

        formulae.add(new Object[] {"![] letter == \"1\" && <> letter == \"2\")", true});
        formulae.add(new Object[] {"!([] ((letter == \"1\")) && <> (letter == \"2\"))", true});
        formulae.add(new Object[] {"![] letter == \"1 && <> letter == \"2\")", false});
        formulae.add(new Object[] {"![] letter == 1 && <> letter == \"2\")", false});

        return formulae.toArray(new Object[formulae.size()][]);
    }

    @DataProvider(name = "io")
    public Object[][] ioFormulae() {
        final List<Object[]> formulae = new ArrayList<>();

        // test DFA/Mealy label checks
        formulae.add(new Object[] {"[] (letter == \"1\" -> X letter == \"2\")", false});
        formulae.add(new Object[] {"(output == \"1\" && output == \"2\")", true});

        formulae.add(new Object[] {"((X (output == \"\" <-> input == \"2\")) U (input == \"3\"))", true});
        formulae.add(new Object[] {"output == \"output with \\\" quotes\"", true});
        // test non-ascii labels
        formulae.add(new Object[] {"input == \"привет\" R output == \"عالم\"", true});

        return formulae.toArray(new Object[formulae.size()][]);
    }

    @Test(dataProvider = "letter")
    public void testLetterFormulae(final String formula, boolean expectedValidity) {
        Assert.assertEquals(LTSminLTLParser.isValidLetterFormula(formula), expectedValidity);
    }

    @Test(dataProvider = "io")
    public void testIOFormulae(final String formula, boolean expectedValidity) {
        Assert.assertEquals(LTSminLTLParser.isValidIOFormula(formula), expectedValidity);
    }
}
