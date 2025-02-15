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

/**
 * Tests whether LTSminMonitorAlternating actually uses regular edge semantics.
 *
 * @author Jeroen Meijer
 * @see LTSminMonitorAlternatingTest
 */
public class LTSminMonitorIOTest extends AbstractLTSminMonitorMealyTest {

    private LTSminMonitorIO<String, String> modelChecker;

    @Override
    public LTSminMonitorIO<String, String> getModelChecker() {
        return modelChecker;
    }

    @Override
    public String getSkipFormula() {
        return "!(input == \"b\")";
    }

    @Override
    public void newModelChecker() {
        modelChecker = new LTSminMonitorIOBuilder<String, String>().withString2Input(s -> s).withString2Output(s -> s).create();
    }

    @Override
    protected String createFalseProperty() {
        return "input == \"b\"";
    }
}