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
package net.automatalib.modelcheckers.ltsmin.monitor;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public class LTSminMonitorDFATest extends AbstractLTSminMonitorTest<DFA<?, String>, DFA<?, String>> {

    private LTSminMonitorDFA<String> modelChecker;

    @Override
    public LTSminMonitorDFA<String> getModelChecker() {
        return modelChecker;
    }

    @Override
    public void newModelChecker() {
        modelChecker = new LTSminMonitorDFABuilder<String>().withString2Input(s -> s).create();
    }

    @Override
    protected DFA<?, String> createCounterExample() {
        return AutomatonBuilders.newDFA(getAlphabet())
                                .withInitial("q0")
                                .withAccepting("q1")
                                .from("q0")
                                .on("a")
                                .to("q1")
                                .create();
    }

    @Override
    protected DFA<?, String> createAutomaton() {
        return AutomatonBuilders.newDFA(getAlphabet())
                                .withInitial("q0")
                                .withAccepting("q0")
                                .from("q0")
                                .on("a")
                                .loop()
                                .create();
    }

    @Override
    protected String createFalseProperty() {
        return "letter == \"b\"";
    }

    /**
     * Test that a {@link ModelCheckingException} is thrown when a {@link DFA} is not prefix-closed.
     */
    @Test(expectedExceptions = ModelCheckingException.class)
    public void testPrefixClosed() {
        final DFA<?, String> dfa = AutomatonBuilders.newDFA(getAlphabet()).
                withInitial("q0").withAccepting("q1").from("q0").on("a").to("q1").create();

        getModelChecker().findCounterExample(dfa, getAlphabet(), "true");
    }

    /**
     * Test that a {@link ModelCheckingException} is thrown when a {@link DFA} accepts the empty language.
     */
    @Test(expectedExceptions = ModelCheckingException.class)
    public void testEmptyLanguage() {
        final DFA<?, String> dfa = AutomatonBuilders.newDFA(getAlphabet()).
                withInitial("q0").from("q0").on("a").loop().from("q0").on("b").loop().create();

        getModelChecker().findCounterExample(dfa, getAlphabet(), "true");
    }
}