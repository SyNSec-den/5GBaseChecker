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
package net.automatalib.examples.spa;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.spa.SPA;
import net.automatalib.automata.spa.StackSPA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A small example for constructing a (context-free) palindrome example over the letters {@code a, b, c} using two
 * separate procedures (non-terminals) {@code S} and {@code T}.
 *
 * @author frohme
 */
public final class PalindromeExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(PalindromeExample.class);

    private PalindromeExample() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        final SPA<?, Character> spa = buildSPA();

        LOGGER.info("Well-matched palindromes");
        checkWord(spa, Word.fromCharSequence("SR"));
        checkWord(spa, Word.fromCharSequence("SaR"));
        checkWord(spa, Word.fromCharSequence("SaSRaR"));
        checkWord(spa, Word.fromCharSequence("SbSTcRRbR"));

        LOGGER.info("");
        LOGGER.info("Well-matched but invalid words");
        checkWord(spa, Word.fromCharSequence("SaaR"));
        checkWord(spa, Word.fromCharSequence("SaTaRaR"));
        checkWord(spa, Word.epsilon());

        LOGGER.info("");
        LOGGER.info("Ill-matched/non-rooted words");
        checkWord(spa, Word.fromCharSequence("SSS"));
        checkWord(spa, Word.fromCharSequence("RS"));
        checkWord(spa, Word.fromCharSequence("aba"));

        Visualization.visualize(spa);
    }

    private static <S, I> void checkWord(SPA<S, I> spa, Word<I> input) {
        final boolean accepted = spa.accepts(input);
        LOGGER.info("Word '{}' is {}accepted by the SPA", input, accepted ? "" : "not ");
    }

    public static SPA<?, Character> buildSPA() {
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
        final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
        final SPAAlphabet<Character> alphabet = new DefaultSPAAlphabet<>(internalAlphabet, callAlphabet, 'R');

        final DFA<?, Character> sProcedure = buildSProcedure(alphabet);
        final DFA<?, Character> tProcedure = buildTProcedure(alphabet);

        final Map<Character, DFA<?, Character>> subModels = new HashMap<>();
        subModels.put('S', sProcedure);
        subModels.put('T', tProcedure);

        // explicit type variable declaration to make checker-framework happy
        return new StackSPA<@Nullable Object, Character>(alphabet, 'S', subModels);
    }

    /**
     * Utility method for building a procedure based on a {@link CompactDFA} that emits terminal symbols 'a', 'b' or
     * delegates to procedure {@link #buildTProcedure(SPAAlphabet) 'T'}.
     */
    private static DFA<?, Character> buildSProcedure(SPAAlphabet<Character> alphabet) {
        // @formatter:off
        return AutomatonBuilders.forDFA(new CompactDFA<>(alphabet.getProceduralAlphabet()))
                                .withInitial("s0")
                                .from("s0").on('T').to("s5")
                                .from("s0").on('a').to("s1")
                                .from("s0").on('b').to("s2")
                                .from("s1").on('S').to("s3")
                                .from("s2").on('S').to("s4")
                                .from("s3").on('a').to("s5")
                                .from("s4").on('b').to("s5")
                                .withAccepting("s0", "s1", "s2", "s5")
                                .create();
        // @formatter:on
    }

    /**
     * Utility method for building a procedure based on a {@link FastDFA} that emits the terminal symbol 'c' or
     * delegates to procedure {@link #buildSProcedure(SPAAlphabet) 'S'}.
     */
    private static DFA<?, Character> buildTProcedure(SPAAlphabet<Character> alphabet) {
        // @formatter:off
        return AutomatonBuilders.forDFA(new FastDFA<>(alphabet.getProceduralAlphabet()))
                                .withInitial("t0")
                                .from("t0").on('S').to("t3")
                                .from("t0").on('c').to("t1")
                                .from("t1").on('T').to("t2")
                                .from("t2").on('c').to("t3")
                                .withAccepting("t1", "t3")
                                .create();
        // @formatter:on
    }

}
