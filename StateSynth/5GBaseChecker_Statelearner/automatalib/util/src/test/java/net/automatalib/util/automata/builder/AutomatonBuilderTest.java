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
package net.automatalib.util.automata.builder;

import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class AutomatonBuilderTest {

    @Test
    public void testBuilder() {
        final Alphabet<Integer> alphabet = Alphabets.integers(1, 6);

        // @formatter:off
        final CompactNFA<Integer> nfa = AutomatonBuilders.newNFA(alphabet)
                .withInitial("i0", "i1", "i2")
                .withAccepting("s0")
                .from("i0").on(1, 2).to("s0")
                .from("i1", "i2").on(3, 4, 5, 6).to("s0")
                .from("s0").on(1, 2, 3, 4, 5, 6).to("sink")
                .create();
        // @formatter:on

        Assert.assertFalse(nfa.accepts(Word.epsilon()));

        for (final Integer i1 : alphabet) {
            Assert.assertTrue(nfa.accepts(Word.fromLetter(i1)));
            for (final Integer i2 : alphabet) {
                Assert.assertFalse(nfa.accepts(Word.fromSymbols(i1, i2)));
            }
        }
    }
}
