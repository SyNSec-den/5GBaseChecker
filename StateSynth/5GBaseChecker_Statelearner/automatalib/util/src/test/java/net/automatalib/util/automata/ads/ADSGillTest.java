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
import net.automatalib.words.impl.Alphabets;
import org.testng.annotations.Test;

/**
 * Tests for the automata of the paper "State-Identification Experiments in Finite Automata" by Arthur Gill.
 *
 * @author frohme
 */
public class ADSGillTest extends AbstractADSTest {

    private static final CompactMealy<Character, Integer> A;

    static {
        // @formatter:off
        A = AutomatonBuilders.<Character, Integer>newMealy(Alphabets.characters('a', 'b'))
                .withInitial("s1")
                .from("s1")
                    .on('a').withOutput(0).loop()
                    .on('b').withOutput(1).to("s4")
                .from("s2")
                    .on('a').withOutput(0).to("s1")
                    .on('b').withOutput(1).to("s5")
                .from("s3")
                    .on('a').withOutput(0).to("s5")
                    .on('b').withOutput(1).to("s1")
                .from("s4")
                    .on('a').withOutput(1).to("s3")
                    .on('b').withOutput(1).loop()
                .from("s5")
                    .on('a').withOutput(1).to("s2")
                    .on('b').withOutput(1).loop()
                .create();
        // @formatter:on
    }

    @Test
    public void testGillExampleAComplete() {
        super.verifyFailure(A);
    }

    @Test
    public void testGillExampleAS2S3S4S5() {
        super.verifySuccess(A, Arrays.asList(1, 4));
    }
}
