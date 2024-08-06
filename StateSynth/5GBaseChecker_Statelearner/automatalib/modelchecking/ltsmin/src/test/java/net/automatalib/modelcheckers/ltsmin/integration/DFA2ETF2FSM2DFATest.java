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
import java.io.InputStream;
import java.util.function.Function;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.modelcheckers.ltsmin.LTSminDFA;
import net.automatalib.serialization.etf.writer.DFA2ETFWriter;
import net.automatalib.serialization.fsm.parser.FSM2DFAParser;
import net.automatalib.serialization.taf.parser.TAFParser;

/**
 * @author Jeroen Meijer
 */
public class DFA2ETF2FSM2DFATest extends AbstractAut2ETF2FSM2AutTest<CompactDFA<String>> {

    @Override
    protected CompactDFA<String> taf2Automaton() throws Exception {
        final InputStream is = DFA2ETF2FSM2DFATest.class.getResourceAsStream("/DFA.taf");
        return TAFParser.parseDFA(is, null);
    }

    @Override
    protected void automaton2ETF(CompactDFA<String> automaton, File etf) throws Exception {
        DFA2ETFWriter.<String>getInstance().writeModel(etf, automaton, automaton.getInputAlphabet());
    }

    @Override
    protected CompactDFA<String> fsm2Automaton(File fsm) throws Exception {
        final Function<String, String> s2s = s -> s;
        return FSM2DFAParser.getParser(s2s, LTSminDFA.LABEL_NAME, LTSminDFA.LABEL_VALUE).readModel(fsm);
    }
}
