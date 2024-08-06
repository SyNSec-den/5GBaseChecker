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
package net.automatalib.modelcheckers.ltsmin.ltl;

import java.util.HashSet;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public abstract class AbstractLTSminLTLMealyTest
        extends AbstractLTSminLTLTest<MealyMachine<?, String, ?, String>, MealyLasso<String, String>> {

    @Override
    public abstract AbstractLTSminLTLMealy<String, String> getModelChecker();

    @Override
    protected MealyMachine<?, String, ?, String> createAutomaton() {
        return AutomatonBuilders.forMealy(new CompactMealy<String, String>(getAlphabet()))
                                .withInitial("q0")
                                .from("q0")
                                .on("a")
                                .withOutput("1")
                                .loop()
                                .create();
    }

    public abstract String getSkipFormula();

    @Test
    public void testSkipOutputs() {
        final HashSet<String> skip = new HashSet<>();
        skip.add("2");
        getModelChecker().setSkipOutputs(skip);

        final MealyMachine<?, String, ?, String> mealy =
                AutomatonBuilders.forMealy(new CompactMealy<String, String>(getAlphabet()))
                                 .from("q0")
                                 .on("a")
                                 .withOutput("1")
                                 .loop()
                                 .withInitial("q0")
                                 .from("q0")
                                 .on("b")
                                 .withOutput("2")
                                 .loop()
                                 .create();

        // we can test if output 2 is removed with the property in #getSkipFormula().
        Assert.assertNull(getModelChecker().findCounterExample(mealy, getAlphabet(), getSkipFormula()));
    }
}
