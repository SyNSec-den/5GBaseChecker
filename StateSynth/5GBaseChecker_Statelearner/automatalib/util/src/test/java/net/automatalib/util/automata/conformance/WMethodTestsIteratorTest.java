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
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
@Test
public class WMethodTestsIteratorTest {

    private final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
    private final DFA<?, Integer> dfa = RandomAutomata.randomDFA(new Random(42), 5, alphabet);

    @Test
    public void testVanillaIterator() {
        final List<Word<Integer>> transCover = Automata.transitionCover(dfa, alphabet);
        final List<Word<Integer>> characterizingSet = Automata.characterizingSet(dfa, alphabet);

        final List<Word<Integer>> expectedWords =
                Streams.stream(CollectionsUtil.cartesianProduct(transCover, characterizingSet))
                       .map(Word::fromWords)
                       .collect(Collectors.toList());

        this.verifyIterator(new WMethodTestsIterator<>(dfa, alphabet, 0), expectedWords);
    }

    @Test
    public void testIteratorWithLookahead2() {
        final int lookahead = 2;

        final List<Word<Integer>> transCover = Automata.transitionCover(dfa, alphabet);
        final Iterable<Word<Integer>> middleTuples =
                Iterables.transform(CollectionsUtil.allTuples(alphabet, 0, lookahead), Word::fromList);
        final List<Word<Integer>> characterizingSet = Automata.characterizingSet(dfa, alphabet);

        final List<Word<Integer>> expectedWords =
                Streams.stream(CollectionsUtil.cartesianProduct(transCover, middleTuples, characterizingSet))
                       .map(Word::fromWords)
                       .collect(Collectors.toList());

        this.verifyIterator(new WMethodTestsIterator<>(dfa, alphabet, lookahead), expectedWords);
    }

    private void verifyIterator(WMethodTestsIterator<Integer> iter, Collection<Word<Integer>> expectedTests) {
        final List<Word<Integer>> expectedWMethodWords = new ArrayList<>(expectedTests);
        final List<Word<Integer>> wMethodWords = new ArrayList<>(expectedTests.size());
        Iterators.addAll(wMethodWords, iter);

        // Order may be different, but that is ok
        expectedWMethodWords.sort(Word.canonicalComparator(Integer::compare));
        wMethodWords.sort(Word.canonicalComparator(Integer::compare));

        Assert.assertEquals(wMethodWords, expectedWMethodWords);
    }
}
