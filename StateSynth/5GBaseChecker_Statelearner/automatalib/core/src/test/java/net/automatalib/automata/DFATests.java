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
package net.automatalib.automata;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class DFATests {

    @Test
    public void testOutputOfUndefinedTransitions() {
        final Alphabet<Character> sigma = Alphabets.characters('a', 'b');
        final CompactDFA<Character> dfa = new CompactDFA<>(sigma);

        final int q0 = dfa.addIntInitialState(true);
        final int q1 = dfa.addIntState(false);

        dfa.setTransition(q0, sigma.getSymbolIndex('a'), q1);
        dfa.setTransition(q1, sigma.getSymbolIndex('b'), q0);

        SharedTestUtils.checkOutput(dfa, Word.fromCharSequence("ababab"), true);
        SharedTestUtils.checkOutput(dfa, Word.fromCharSequence("aabb"), false);
        SharedTestUtils.checkOutput(dfa, Word.fromCharSequence("baba"), false);
    }
}
