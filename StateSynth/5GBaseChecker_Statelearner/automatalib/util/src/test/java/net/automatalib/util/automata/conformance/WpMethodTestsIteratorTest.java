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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.util.automata.equivalence.CharacterizingSets;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class WpMethodTestsIteratorTest {

    private final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
    private final DFA<?, Integer> dfa = RandomAutomata.randomDFA(new Random(42), 10, alphabet);

    @Test
    public void testIteratorWithoutMiddleParts() {
        final List<Word<Integer>> iteratorWords = Lists.newArrayList(new WpMethodTestsIterator<>(dfa, alphabet, 0));
        final List<Word<Integer>> wpMethodWords =
                generateWpMethodTest(dfa, alphabet, Collections.singletonList(Word.epsilon()));

        checkTestWords(iteratorWords, wpMethodWords);
    }

    @Test
    public void testIteratorWithMiddleParts() {

        final int depth = 3;

        final List<Word<Integer>> iteratorWords = Lists.newArrayList(new WpMethodTestsIterator<>(dfa, alphabet, depth));
        final List<Word<Integer>> wpMethodWords = generateWpMethodTest(dfa,
                                                                       alphabet,
                                                                       Streams.stream(CollectionsUtil.allTuples(alphabet,
                                                                                                                0,
                                                                                                                depth))
                                                                              .map(Word::fromList)
                                                                              .collect(Collectors.toList()));

        checkTestWords(iteratorWords, wpMethodWords);
    }

    private <I extends Comparable<I>> void checkTestWords(List<Word<I>> actual, List<Word<I>> expected) {
        actual.sort(CmpUtil.lexComparator());
        expected.sort(CmpUtil.lexComparator());

        Assert.assertEquals(actual, expected);

    }

    private <S, I> List<Word<I>> generateWpMethodTest(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                      Collection<? extends I> inputs,
                                                      List<Word<I>> middleParts) {

        final List<Word<I>> stateCover = new ArrayList<>(automaton.size());
        final List<Word<I>> transitionCover = new ArrayList<>(automaton.size() * inputs.size());
        final List<Word<I>> characterizingSet = new ArrayList<>();

        Covers.cover(automaton, inputs, stateCover, transitionCover);
        CharacterizingSets.findCharacterizingSet(automaton, inputs, characterizingSet);

        assert !stateCover.isEmpty();
        assert !transitionCover.isEmpty();
        assert !characterizingSet.isEmpty();

        final List<Word<I>> result = new ArrayList<>();

        // Phase 1: state cover * middle part * global suffixes
        Lists.cartesianProduct(stateCover, middleParts, characterizingSet)
             .stream()
             .map(Word::fromWords)
             .forEach(result::add);

        // Phase 2: transitions (not in state cover) * middle part * local suffixes
        transitionCover.removeAll(stateCover);

        for (final Word<I> prefix : transitionCover) {
            for (final Word<I> middle : middleParts) {
                final Word<I> prefixWithMiddle = prefix.concat(middle);
                final S s = automaton.getState(prefixWithMiddle);

                final List<Word<I>> suffixes = new ArrayList<>();
                CharacterizingSets.findCharacterizingSet(automaton, inputs, s, suffixes);
                assert !suffixes.isEmpty();

                for (final Word<I> suffix : suffixes) {
                    result.add(prefixWithMiddle.concat(suffix));
                }
            }
        }

        return result;
    }
}
