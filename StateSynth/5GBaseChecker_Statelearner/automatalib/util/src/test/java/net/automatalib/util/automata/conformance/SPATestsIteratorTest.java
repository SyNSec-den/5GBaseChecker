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
package net.automatalib.util.automata.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;

import com.google.common.collect.Iterators;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.spa.SPA;
import net.automatalib.util.automata.random.RandomAutomata;
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
public class SPATestsIteratorTest {

    @Test
    public void testWMethodVariant() {
        testIterator(WMethodTestsIterator::new);
    }

    @Test
    public void testWpMethodVariant() {
        testIterator(WpMethodTestsIterator::new);
    }

    private void testIterator(BiFunction<DFA<?, Character>, Alphabet<Character>, Iterator<Word<Character>>> conformanceTestProvider) {
        final Random random = new Random(42);
        final SPAAlphabet<Character> alphabet =
                new DefaultSPAAlphabet<>(Alphabets.characters('a', 'e'), Alphabets.characters('A', 'C'), 'R');
        final SPA<?, Character> spa = RandomAutomata.randomRedundancyFreeSPA(random, alphabet, 10);

        final List<Word<Character>> testTraces = new ArrayList<>();
        Iterators.addAll(testTraces, new SPATestsIterator<>(spa, conformanceTestProvider));

        for (Entry<Character, DFA<?, Character>> e : spa.getProcedures().entrySet()) {
            verifyProcedure(e.getKey(), e.getValue(), alphabet, testTraces, conformanceTestProvider);
        }
    }

    private <I> void verifyProcedure(I procedure,
                                     DFA<?, I> dfa,
                                     SPAAlphabet<I> alphabet,
                                     Collection<Word<I>> globalTraces,
                                     BiFunction<DFA<?, I>, Alphabet<I>, Iterator<Word<I>>> conformanceTestProvider) {

        final Alphabet<I> proceduralAlphabet = alphabet.getProceduralAlphabet();
        final List<Word<I>> localTraces = new ArrayList<>();
        Iterators.addAll(localTraces, conformanceTestProvider.apply(dfa, proceduralAlphabet));

        for (Word<I> trace : globalTraces) {
            for (int i = 0; i < trace.length(); i++) {
                final I sym = trace.getSymbol(i);

                if (Objects.equals(procedure, sym)) {
                    final int returnIdx = alphabet.findReturnIndex(trace, i + 1);
                    final Word<I> wellMatched = trace.subWord(i + 1, returnIdx);
                    final Word<I> normalized = alphabet.normalize(wellMatched, 0);
                    localTraces.remove(normalized);
                }
            }
        }

        Assert.assertTrue(localTraces.isEmpty());
    }
}
