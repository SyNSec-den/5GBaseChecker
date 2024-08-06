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

import java.io.File;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.etf.writer.Mealy2ETFWriterIO;
import net.automatalib.serialization.fsm.parser.FSM2MealyParserIO;

/**
 * @author Jeroen Meijer
 */
public class IO2ETF2FSM2IOTest extends AbstractMealy2ETF2FSM2MealyTest {

    @Override
    protected void automaton2ETF(CompactMealy<String, String> automaton, File etf) throws Exception {
        Mealy2ETFWriterIO.<String, String>getInstance().writeModel(etf, automaton, automaton.getInputAlphabet());
    }

    @Override
    protected CompactMealy<String, String> fsm2Automaton(File fsm) throws Exception {
        return FSM2MealyParserIO.getParser(s -> s).readModel(fsm);
    }
}
