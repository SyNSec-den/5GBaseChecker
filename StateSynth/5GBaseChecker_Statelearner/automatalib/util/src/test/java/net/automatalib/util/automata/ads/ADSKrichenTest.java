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
package net.automatalib.util.automata.ads;

import java.util.Arrays;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.annotations.Test;

/**
 * Tests for the automata of the chapter "State Identification" (in "Model-Based Testing of Reactive Systems") by Moez
 * Krichen.
 *
 * @author frohme
 */
public class ADSKrichenTest extends AbstractADSTest {

    // Examples from the paper
    private static final CompactMealy<Character, Integer> M3, M4, M5, M6;

    static {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'b');

        // @formatter:off
        M3 = AutomatonBuilders.<Character, Integer>newMealy(alphabet)
                .withInitial("s1")
                .from("s1")
                    .on('a').withOutput(0).loop()
                    .on('b').withOutput(1).to("s3")
                .from("s2")
                    .on('a').withOutput(0).to("s1")
                    .on('b').withOutput(0).to("s3")
                .from("s3")
                    .on('a').withOutput(0).to("s2")
                    .on('b').withOutput(0).loop()
                .create();

        M4 = AutomatonBuilders.<Character, Integer>newMealy(alphabet)
                .withInitial("s1")
                .from("s1")
                    .on('a').withOutput(0).to("s3")
                    .on('b').withOutput(0).loop()
                .from("s2")
                    .on('a').withOutput(0).to("s4")
                    .on('b').withOutput(0).to("s1")
                .from("s3")
                    .on('a').withOutput(1).to("s1")
                    .on('b').withOutput(0).loop()
                .from("s4")
                    .on('a').withOutput(1).to("s2")
                    .on('b').withOutput(1).loop()
                .create();

        M5 = AutomatonBuilders.<Character, Integer>newMealy(alphabet)
                .withInitial("s1")
                .from("s1")
                    .on('a').withOutput(0).loop()
                    .on('b').withOutput(1).to("s3")
                .from("s2")
                    .on('a').withOutput(0).loop()
                    .on('b').withOutput(0).to("s1")
                .from("s3")
                    .on('a').withOutput(1).loop()
                    .on('b').withOutput(1).to("s2")
                .create();

        M6 = AutomatonBuilders.<Character, Integer>newMealy(alphabet)
                .withInitial("s1")
                .from("s1")
                    .on('a').withOutput(0).to("s2")
                    .on('b').withOutput(0).loop()
                .from("s2")
                    .on('a').withOutput(1).to("s3")
                    .on('b').withOutput(0).to("s1")
                .from("s3")
                    .on('a', 'b').withOutput(0).to("s4")
                .from("s4")
                    .on('a').withOutput(1).to("s5")
                    .on('b').withOutput(0).to("s5")
                .from("s5")
                    .on('a', 'b').withOutput(0).to("s6")
                .from("s6")
                    .on('a').withOutput(1).to("s1")
                    .on('b').withOutput(0).to("s1")
                .create();
        // @formatter:on
    }

    @Test
    public void testKrichenExampleM3Complete() {
        super.verifyFailure(M3);
    }

    @Test
    public void testKrichenExampleM3S0S1() {
        super.verifySuccess(M3, Arrays.asList(0, 1));
    }

    @Test
    public void testKrichenExampleM3S0S2() {
        super.verifySuccess(M3, Arrays.asList(0, 2));
    }

    @Test
    public void testKrichenExampleM3S1S2() {
        super.verifySuccess(M3, Arrays.asList(1, 2));
    }

    @Test
    public void testKrichenExampleM4Complete() {
        super.verifySuccess(M4);
    }

    @Test
    public void testKrichenExampleM5Complete() {
        super.verifySuccess(M5);
    }

    @Test
    public void testKrichenExampleM6Complete() {
        super.verifySuccess(M6);
    }

    @Test
    public void testKrichenExampleM6CompleteS2S4() {
        super.verifySuccess(M6, Arrays.asList(2, 4));
    }

}
