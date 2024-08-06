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
package net.automatalib.util.automata;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.ts.TS.TransRef;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class AutomataTest {

    final Alphabet<Character> alphabet;
    final CompactDFA<Character> partial;
    final CompactDFA<Character> complete;

    public AutomataTest() {
        this.alphabet = Alphabets.characters('a', 'c');
        // @formatter:off
        this.partial = AutomatonBuilders.newDFA(alphabet)
                                        .withInitial("s0")
                                        .from("s0").on('a').to("s1")
                                        .from("s0").on('b').to("s2")
                                        .from("s1").on('a').to("s3")
                                        .from("s2").on('b').to("s3")
                                        .from("s0").on('c').to("s3")
                                        .withAccepting("s3")
                                        .create();
        // @formatter:on
        this.complete = DFAs.complete(partial, alphabet);
    }

    @Test
    public void testAllDefinedInputsPartial() {
        final Iterable<TransRef<Integer, Character, ?>> trans = Automata.allDefinedInputs(this.partial, this.alphabet);

        int definedCount = 0;
        for (TransRef<Integer, Character, ?> t : trans) {
            Assert.assertNotNull(t.transition);
            Assert.assertNotNull(this.partial.getSuccessor(t.state, t.input));

            definedCount++;
        }
        Assert.assertEquals(definedCount, 5);
    }

    @Test
    public void testAllDefinedInputsComplete() {
        final Iterable<TransRef<Integer, Character, ?>> trans = Automata.allDefinedInputs(this.complete, this.alphabet);

        int definedCount = 0;
        for (TransRef<Integer, Character, ?> t : trans) {
            Assert.assertNotNull(t.transition);
            Assert.assertNotNull(this.complete.getSuccessor(t.state, t.input));

            definedCount++;
        }

        Assert.assertEquals(definedCount, this.complete.size() * this.alphabet.size());
    }

    @Test
    public void testAllUndefinedInputsPartial() {
        final Iterable<TransRef<Integer, Character, ?>> trans =
                Automata.allUndefinedInputs(this.partial, this.alphabet);

        int undefinedCount = 0;
        for (TransRef<Integer, Character, ?> t : trans) {
            Assert.assertNull(t.transition);
            Assert.assertNull(this.partial.getSuccessor(t.state, t.input));

            undefinedCount++;
        }

        Assert.assertEquals(undefinedCount, (this.partial.size() * this.alphabet.size()) - 5);
        Assert.assertTrue(Automata.hasUndefinedInput(this.partial, this.alphabet));
    }

    @Test
    public void testAllUndefinedInputsComplete() {
        Iterable<?> trans = Automata.allUndefinedInputs(this.complete, this.alphabet);

        Assert.assertFalse(trans.iterator().hasNext());
        Assert.assertFalse(Automata.hasUndefinedInput(this.complete, this.alphabet));
    }
}
