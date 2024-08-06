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
package net.automatalib.serialization.fsm.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Function;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.io.UnclosableInputStream;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests forFSM2MealyParserIO.
 * <p>
 * This tests will involve parsing Mealy machines where transitions in the FSM can be directly mapped to transitions in
 * the Mealy machine (opposed to alternating edge semantics).
 *
 * @author Jeroen Meijer
 * @see FSM2MealyParserAlternatingTest
 */
public class FSM2MealyParserIOTest extends AbstractFSM2ParserTest {

    @Test
    public void testParse() throws Exception {
        try (InputStream is = FSM2MealyParserIOTest.class.getResourceAsStream("/MealyIO.fsm")) {

            final Function<String, Character> ep = s -> s.charAt(0);
            final CompactMealy<Character, Character> actualMealy = FSM2MealyParserIO.getParser(ep).readModel(is);

            final Alphabet<Character> alphabet = Alphabets.characters('a', 'a');

            CompactMealy<Character, ?> expectedMealy = AutomatonBuilders.newMealy(alphabet)
                                                                        .from("q0").on('a').withOutput('1').to("q1")
                                                                        .from("q1").on('a').withOutput('2').to("q0")
                                                                        .withInitial("q0")
                                                                        .create();

            Assert.assertTrue(Automata.testEquivalence(actualMealy, expectedMealy, alphabet));
        }
    }

    @Override
    protected CompactMealy<Character, Character> getParsedAutomaton(Collection<Character> requiredInputs)
            throws IOException {
        try (InputStream is = FSM2MealyParserIOTest.class.getResourceAsStream("/MealyIO.fsm")) {
            final Function<String, Character> ep = s -> s.charAt(0);
            return FSM2MealyParserIO.getParser(requiredInputs, ep).readModel(is);
        }
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException {
        try (InputStream is = FSM2MealyParserIOTest.class.getResourceAsStream("/MealyIO.fsm")) {
            FSM2MealyParserIO.getParser(s -> s.charAt(0)).readModel(new UnclosableInputStream(is));
        }
    }
}