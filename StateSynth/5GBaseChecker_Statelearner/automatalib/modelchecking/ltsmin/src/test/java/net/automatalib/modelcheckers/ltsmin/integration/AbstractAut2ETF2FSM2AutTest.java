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
import java.util.HashSet;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.commons.util.process.ProcessUtil;
import net.automatalib.modelcheckers.ltsmin.LTSminUtil;
import net.automatalib.modelcheckers.ltsmin.LTSminVersion;
import net.automatalib.util.automata.equivalence.NearLinearEquivalenceTest;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class is a generic tester for the integration between AutomataLib and LTSmin.
 * This class will:
 *  - read an automaton from a TAF.
 *  - write it to an ETF.
 *  - convert the ETF to a GCF with LTSmin.
 *  - convert the GCF to FSM with LTSmin.
 *  - read the FSM into an automaton.
 *  - perform an equivalence check between the two automata.
 *
 * @author Jeroen Meijer
 *
 * @param <A> the automaton type.
 */
public abstract class AbstractAut2ETF2FSM2AutTest<
        A extends UniversalDeterministicAutomaton<Integer, String, ?, ?, ?> & InputAlphabetHolder<String>> {

    @BeforeClass
    public void setupBeforeClass() {
        if (!LTSminUtil.supports(LTSminVersion.of(3, 1, 0))) {
            throw new SkipException("LTSmin not installed in proper version");
        }
    }

    protected abstract A taf2Automaton() throws Exception;

    protected abstract void automaton2ETF(A automaton, File etf) throws Exception;

    protected abstract A fsm2Automaton(File fsm) throws Exception;

    @Test
    public void test() throws Exception {
        // read the taf
        final A automatonOut = taf2Automaton();

        // write the etf
        final File etf = File.createTempFile("etf", ".etf");
        etf.deleteOnExit();
        automaton2ETF(automatonOut, etf);

        // convert the etf to gcf
        final File gcf = File.createTempFile("gcf", ".gcf");
        gcf.deleteOnExit();

        final String[] ltsmin = new String[] {
                LTSminUtil.ETF2LTS_MC,
                "--threads=1",
                etf.getAbsolutePath(),
                gcf.getAbsolutePath()};

        Assert.assertEquals(ProcessUtil.invokeProcess(ltsmin), 0);

        // convert the gcf to fsm
        final File fsm = File.createTempFile("fsm", ".fsm");
        fsm.deleteOnExit();

        final String[] convert = new String[] {
                LTSminUtil.LTSMIN_CONVERT,
                gcf.getAbsolutePath(),
                fsm.getAbsolutePath(),
                "--rdwr"};

        Assert.assertEquals(ProcessUtil.invokeProcess(convert), 0);

        // read the fsm into an automaton
        final A automatonIn = fsm2Automaton(fsm);

        // find the inputs that are actually used
        final Alphabet<String> inputAlphabet = automatonOut.getInputAlphabet().stream().filter(
                i -> automatonOut.getStates().stream().anyMatch(
                        s -> automatonOut.getSuccessor(s, i) != null)).collect(Alphabets.collector());

        // test we have the same alphabet ignoring order
        Assert.assertEquals(new HashSet<>(inputAlphabet), new HashSet<>(automatonIn.getInputAlphabet()));

        // test we have the same automaton
        Assert.assertNull(new NearLinearEquivalenceTest<>(automatonOut).findSeparatingWord(automatonIn, inputAlphabet));
    }
}
