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
package net.automatalib.automata.spa;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class SPATest {

    final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'd');
    final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'U');
    final SPAAlphabet<Character> alphabet = new DefaultSPAAlphabet<>(internalAlphabet, callAlphabet, 'R');

    final Map<Character, DFA<?, Character>> subModels =
            ImmutableMap.of('S', buildSProcedure(alphabet), 'T', buildTProcedure(alphabet));

    @Test
    public void testStackSPA() {
        final SPA<?, Character> spa = new StackSPA<>(alphabet, 'S', subModels);

        // Check getters
        Assert.assertEquals(spa.getInputAlphabet(), alphabet);
        Assert.assertEquals(spa.getInitialProcedure(), (Character) 'S');
        Assert.assertEquals(spa.getProcedures(), subModels);
        Assert.assertEquals(spa.size(), subModels.values().stream().mapToInt(DFA::size).sum());

        // Well-matched palindromes
        Assert.assertTrue(spa.computeOutput(Word.fromCharSequence("SR")));
        Assert.assertTrue(spa.computeOutput(Word.fromCharSequence("SaR")));
        Assert.assertTrue(spa.computeOutput(Word.fromCharSequence("SaSRaR")));
        Assert.assertTrue(spa.computeOutput(Word.fromCharSequence("SbSTcRRbR")));

        // Well-matched but invalid words
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SaaR")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SaTaRaR")));
        Assert.assertFalse(spa.computeOutput(Word.epsilon()));

        // Ill-matched/non-rooted words
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SSS")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("RS")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("aba")));

        // Un-specified symbols
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SdR")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SaUcRaR")));
    }

    @Test
    public void testEmptySPA() {
        final SPA<?, Character> spa = new EmptySPA<>(alphabet);

        // Check getters
        Assert.assertEquals(spa.getInputAlphabet(), alphabet);
        Assert.assertNull(spa.getInitialProcedure());
        Assert.assertTrue(spa.getProcedures().isEmpty());

        // Well-matched palindromes
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SR")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SaR")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SaSRaR")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SbSTcRRbR")));

        // Well-matched but invalid words
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SaaR")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SaTaRaR")));
        Assert.assertFalse(spa.computeOutput(Word.epsilon()));

        // Ill-matched/non-rooted words
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SSS")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("RS")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("aba")));

        // Un-specified symbols
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SdR")));
        Assert.assertFalse(spa.computeOutput(Word.fromCharSequence("SaUcRaR")));
    }

    private static DFA<?, Character> buildSProcedure(SPAAlphabet<Character> alphabet) {
        final CompactDFA<Character> procedure = new CompactDFA<>(alphabet.getProceduralAlphabet());

        final int s0 = procedure.addInitialState(true);
        final int s1 = procedure.addState(true);
        final int s2 = procedure.addState(true);
        final int s3 = procedure.addState(false);
        final int s4 = procedure.addState(false);
        final int s5 = procedure.addState(true);

        procedure.addTransition(s0, 'T', s5);
        procedure.addTransition(s0, 'a', s1);
        procedure.addTransition(s0, 'b', s2);
        procedure.addTransition(s1, 'S', s3);
        procedure.addTransition(s2, 'S', s4);
        procedure.addTransition(s3, 'a', s5);
        procedure.addTransition(s4, 'b', s5);

        return procedure;
    }

    private static DFA<?, Character> buildTProcedure(SPAAlphabet<Character> alphabet) {
        final FastDFA<Character> procedure = new FastDFA<>(alphabet.getProceduralAlphabet());

        final FastDFAState t0 = procedure.addInitialState(false);
        final FastDFAState t1 = procedure.addState(true);
        final FastDFAState t2 = procedure.addState(false);
        final FastDFAState t3 = procedure.addState(true);

        procedure.addTransition(t0, 'S', t3);
        procedure.addTransition(t0, 'c', t1);
        procedure.addTransition(t1, 'T', t2);
        procedure.addTransition(t2, 'c', t3);

        return procedure;
    }
}
