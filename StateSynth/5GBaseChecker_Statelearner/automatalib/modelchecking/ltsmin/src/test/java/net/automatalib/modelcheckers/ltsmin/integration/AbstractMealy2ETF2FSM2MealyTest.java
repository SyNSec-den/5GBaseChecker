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
package net.automatalib.modelcheckers.ltsmin.integration;

import java.io.InputStream;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.taf.parser.TAFParser;

/**
 * @author Jeroen Meijer
 */
public abstract class AbstractMealy2ETF2FSM2MealyTest extends AbstractAut2ETF2FSM2AutTest<CompactMealy<String, String>> {

    @Override
    protected CompactMealy<String, String> taf2Automaton() throws Exception {
        final InputStream is = AbstractMealy2ETF2FSM2MealyTest.class.getResourceAsStream("/Mealy.taf");
        return TAFParser.parseMealy(is, null);
    }
}
