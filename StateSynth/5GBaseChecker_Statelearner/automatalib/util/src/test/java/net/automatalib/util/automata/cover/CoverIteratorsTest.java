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
package net.automatalib.util.automata.cover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import com.google.common.collect.Iterators;
import net.automatalib.automata.fsa.DFA;
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
public class CoverIteratorsTest {

    @Test
    public void testStateCoverIterator() {
        compareCovers(Automata::stateCover, Covers::stateCoverIterator);
    }

    @Test
    public void testTransitionCoverIterator() {
        compareCovers(Automata::transitionCover, Covers::transitionCoverIterator);
    }

    @Test
    public void testIncrementalStateCoverIterator() {
        compareIncrementalCovers(Covers::incrementalStateCover, Covers::incrementalStateCoverIterator);
    }

    @Test
    public void testIncrementalTransitionCoverIterator() {
        compareIncrementalCovers(Covers::incrementalTransitionCover, Covers::incrementalTransitionCoverIterator);
    }

    public static void compareCovers(BiFunction<DFA<?, Integer>, Collection<Integer>, Collection<Word<Integer>>> coverGenerator,
                                     BiFunction<DFA<?, Integer>, Collection<Integer>, Iterator<Word<Integer>>> iteratorGenerator) {

        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final DFA<?, Integer> dfa = RandomAutomata.randomDFA(new Random(42), 50, alphabet);

        final List<Word<Integer>> cover = new ArrayList<>(coverGenerator.apply(dfa, alphabet));

        final List<Word<Integer>> iteratorCover = new ArrayList<>(dfa.size());
        final Iterator<Word<Integer>> iter = iteratorGenerator.apply(dfa, alphabet);

        Iterators.addAll(iteratorCover, iter);

        Assert.assertEquals(iteratorCover, cover);
    }

    public static void compareIncrementalCovers(QuadConsumer<DFA<?, Integer>, Collection<Integer>, Collection<Word<Integer>>, Collection<Word<Integer>>> incrementalCoverGenerator,
                                                TriFunction<DFA<?, Integer>, Collection<Integer>, Collection<Word<Integer>>, Iterator<Word<Integer>>> incrementalIteratorGenerator) {
        final Random random = new Random(42);
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 5);
        final DFA<?, Integer> dfa = RandomAutomata.randomDFA(random, 50, alphabet);

        final List<Word<Integer>> fullCover = new ArrayList<>();
        incrementalCoverGenerator.apply(dfa, alphabet, Collections.emptyList(), fullCover);

        Collections.shuffle(fullCover, random);
        final List<Word<Integer>> oldCover = fullCover.subList(0, fullCover.size() / 2);

        final List<Word<Integer>> incrementalCover = new ArrayList<>(fullCover.size() / 2);
        incrementalCoverGenerator.apply(dfa, alphabet, oldCover, incrementalCover);

        final List<Word<Integer>> iteratorCover = new ArrayList<>(fullCover.size() / 2);
        final Iterator<Word<Integer>> iter = incrementalIteratorGenerator.apply(dfa, alphabet, oldCover);

        Iterators.addAll(iteratorCover, iter);

        Assert.assertEquals(iteratorCover, incrementalCover);
    }

    @FunctionalInterface
    public interface TriFunction<R, S, T, U> {

        U apply(R r, S s, T t);
    }

    @FunctionalInterface
    public interface QuadConsumer<R, S, T, U> {

        void apply(R r, S s, T t, U u);
    }
}
