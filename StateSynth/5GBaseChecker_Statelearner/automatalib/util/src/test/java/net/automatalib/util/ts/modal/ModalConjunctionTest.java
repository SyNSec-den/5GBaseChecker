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
package net.automatalib.util.ts.modal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.fixpoint.Worksets;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.MTSTransition;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author msc
 */
public class ModalConjunctionTest {

    private CompactMTS<Character> block0;
    private CompactMTS<Character> block1;
    private ModalConjunction<CompactMTS<Character>, Integer, Integer, Integer, Character, MTSTransition<MutableModalEdgeProperty>, MTSTransition<MutableModalEdgeProperty>, MTSTransition<MutableModalEdgeProperty>, MutableModalEdgeProperty, MutableModalEdgeProperty>
            algo;

    private Integer b0s0;
    private Integer b0s1;
    private Integer b0s2;
    private Integer b0s3;
    private Integer b0s4;
    private Integer b0s5;
    private Integer b0s6;
    private Integer b0s7;
    private Integer b0s8;
    private Integer b0s9;

    private Integer b1s0;
    private Integer b1s1;

    @BeforeMethod
    void setup() {
        block0 = new CompactMTS<>(Alphabets.characters('a', 'd'));
        b0s0 = block0.addInitialState();
        b0s1 = block0.addState();
        b0s2 = block0.addState();
        b0s3 = block0.addState();
        b0s4 = block0.addState();
        b0s5 = block0.addState();
        b0s6 = block0.addState();
        b0s7 = block0.addState();
        b0s8 = block0.addState();
        b0s9 = block0.addState();
        block0.addTransition(b0s0, 'a', b0s1, null);
        block0.addModalTransition(b0s1, 'b', b0s2, ModalType.MAY);
        block0.addTransition(b0s2, 'c', b0s0, null);
        block0.addModalTransition(b0s2, 'b', b0s1, ModalType.MAY);
        block0.addModalTransition(b0s2, 'c', b0s2, ModalType.MAY);
        block0.addModalTransition(b0s3, 'b', b0s0, ModalType.MUST);
        block0.addTransition(b0s3, 'a', b0s4, null);
        block0.addTransition(b0s4, 'c', b0s3, null);
        block0.addModalTransition(b0s1, 'c', b0s6, ModalType.MAY);
        block0.addTransition(b0s8, 'c', b0s7, null);
        block0.addTransition(b0s8, 'c', b0s9, null);

        block1 = new CompactMTS<>(Alphabets.characters('a', 'd'));
        b1s0 = block1.addInitialState();
        b1s1 = block1.addState();

        block1.addModalTransition(b1s0, 'a', b1s1, ModalType.MAY);
        block1.addModalTransition(b1s0, 'c', b1s0, ModalType.MAY);
        block1.addModalTransition(b1s1, 'b', b1s0, ModalType.MUST);

        algo = new ModalConjunction<>(block0, block1, CompactMTS::new);
    }

    @Test
    void expectedElementCount() {
        Assert.assertTrue(algo.expectedElementCount() >= 0);
    }

    @Test
    void updateMay() {
        Map<Pair<Integer, Integer>, Integer> map = new HashMap<>();
        Collection<Pair<Integer, Integer>> discovered;

        algo.initialize(map);
        discovered = algo.update(map, Pair.of(b0s0, b1s0));

        Assert.assertEquals(discovered, Collections.singleton(Pair.of(b0s1, b1s1)));

        discovered = algo.update(map, Pair.of(b0s1, b1s1));

        Assert.assertEquals(discovered, Collections.singleton(Pair.of(b0s2, b1s0)));
    }

    @Test
    void result() {
        final Pair<Map<Pair<Integer, Integer>, Integer>, CompactMTS<Character>> result = Worksets.map(algo);
        final Map<Pair<Integer, Integer>, Integer> stateMapping = result.getFirst();
        final CompactMTS<Character> mts = result.getSecond();

        Assert.assertEquals(stateMapping.size(), 3);
        Assert.assertEquals(mts.size(), 3);
        Assert.assertNotNull(stateMapping.get(Pair.of(b0s0, b1s0)));
        Assert.assertNotNull(stateMapping.get(Pair.of(b0s1, b1s1)));
        Assert.assertNotNull(stateMapping.get(Pair.of(b0s2, b1s0)));

        final Integer s0 = stateMapping.get(Pair.of(b0s0, b1s0));
        final Integer s1 = stateMapping.get(Pair.of(b0s1, b1s1));
        final Integer s2 = stateMapping.get(Pair.of(b0s2, b1s0));

        Assert.assertNotNull(getSingleTransition(mts, s0, 'a', s1));
        Assert.assertNotNull(getSingleTransition(mts, s1, 'b', s2));
        Assert.assertNotNull(getSingleTransition(mts, s2, 'c', s0));
        Assert.assertNotNull(getSingleTransition(mts, s2, 'c', s2));
        Assert.assertNull(getSingleTransition(mts, s2, 'b', s1));
        Assert.assertTrue(mts.getTransitionProperty(getSingleTransition(mts, s0, 'a', s1)).isMust());
        Assert.assertTrue(mts.getTransitionProperty(getSingleTransition(mts, s1, 'b', s2)).isMust());
        Assert.assertTrue(mts.getTransitionProperty(getSingleTransition(mts, s2, 'c', s0)).isMust());
        Assert.assertTrue(mts.getTransitionProperty(getSingleTransition(mts, s2, 'c', s2)).isMayOnly());
    }

    @Test
    void errorMustWithoutPartner() {
        block1.getTransitionProperty(getSingleTransition(block1, b1s0, 'c', b1s0)).setMust();
        Assert.assertThrows(IllegalArgumentException.class, () -> Worksets.map(algo));
    }

    @Test
    void errorMustWithoutPartner2() {
        block1.getTransitionProperty(getSingleTransition(block1, b1s0, 'a', b1s1)).setMust();
        Assert.assertThrows(IllegalArgumentException.class, () -> Worksets.map(algo));
    }

    @Test
    void reverseRefinement() {
        block1.getTransitionProperty(getSingleTransition(block1, b1s1, 'b', b1s0)).setMayOnly();
        // simple execute to check that no exception is thrown
        Worksets.map(algo);
    }

    @Test
    void errorAlphabet() {
        CompactMTS<Character> block2 = new CompactMTS<>(Alphabets.characters('a', 'c'));
        block2.addInitialState();
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> new ModalConjunction<>(block0, block2, CompactMTS::new));
    }

    @Test
    void sameTarget() {
        block1.addTransition(b1s1, 'b', b1s0, new ModalEdgePropertyImpl(ModalType.MAY));

        final Pair<Map<Pair<Integer, Integer>, Integer>, CompactMTS<Character>> result = Worksets.map(algo);
        final Map<Pair<Integer, Integer>, Integer> stateMapping = result.getFirst();
        final CompactMTS<Character> mts = result.getSecond();

        Assert.assertEquals(mts.size(), 3);
        Assert.assertNotNull(stateMapping.get(Pair.of(b0s0, b1s0)));
        Assert.assertNotNull(stateMapping.get(Pair.of(b0s1, b1s1)));
        Assert.assertNotNull(stateMapping.get(Pair.of(b0s2, b1s0)));

        final Integer s0 = stateMapping.get(Pair.of(b0s0, b1s0));
        final Integer s1 = stateMapping.get(Pair.of(b0s1, b1s1));
        final Integer s2 = stateMapping.get(Pair.of(b0s2, b1s0));

        Assert.assertNotNull(getSingleTransition(mts, s0, 'a', s1));
        Assert.assertNotNull(getSingleTransition(mts, s1, 'b', s2));
        Assert.assertNotNull(getSingleTransition(mts, s2, 'c', s0));
        Assert.assertNull(getSingleTransition(mts, s2, 'b', s1));
    }

    @Test
    void errorSameTarget() {
        block1.addTransition(b1s0, 'a', b1s1, new ModalEdgePropertyImpl(ModalType.MUST));
        Assert.assertThrows(IllegalArgumentException.class, () -> Worksets.map(algo));
    }

    @Test
    void noSuitableTransitionsInPartner() {
        CompactMTS<Character> block2 = new CompactMTS<>(Alphabets.characters('a', 'd'));
        block2.addInitialState();

        // simple execute to check that no exception is thrown
        Worksets.map(new ModalConjunction<>(block1, block2, CompactMTS::new));
    }

    @Test
    void errorNoSuitableTransitionsInPartner() {
        CompactMTS<Character> block2 = new CompactMTS<>(Alphabets.characters('a', 'd'));
        int s0 = block2.addInitialState();
        int s1 = block2.addState();
        block2.addModalTransition(s0, 'a', s1, ModalType.MAY);

        Assert.assertThrows(IllegalArgumentException.class,
                            () -> Worksets.map(new ModalConjunction<>(block1, block2, CompactMTS::new)));
    }

    @Test(dataProvider = "randomSource")
    void random(CompactMTS<Character> a, CompactMTS<Character> b, int K) {

        algo = new ModalConjunction<>(a, b, CompactMTS::new);

        final Pair<Map<Pair<Integer, Integer>, Integer>, CompactMTS<Character>> result = Worksets.map(algo);
        final CompactMTS<Character> mts = result.getSecond();

        Assert.assertTrue(mts.size() >= 1);
        Assert.assertTrue(mts.size() <= a.size() * b.size());

        boolean onlyMayA = true, onlyMayB = true;
        for (MTSTransition<MutableModalEdgeProperty> t : a.getTransitions(a.getInitialStates().iterator().next())) {
            if (t.getProperty().isMust()) {
                onlyMayA = false;
            }
        }

        for (MTSTransition<MutableModalEdgeProperty> t : b.getTransitions(b.getInitialStates().iterator().next())) {
            if (t.getProperty().isMust()) {
                onlyMayB = false;
            }
        }

        if (onlyMayA && onlyMayB) {
            // wrong, since may transitions are allowed:
            //softly.assertThat(result.second.size()).isEqualTo(1);

            // works, but is hard to read:
            Assert.assertTrue(mts.getStates()
                                 .stream()
                                 .map(mts::getTransitions)
                                 .flatMap(Collection::stream)
                                 .noneMatch(t -> mts.getTransitionProperty(t).isMust()));
        }

    }

    @DataProvider(name = "randomSource")
    private static Object[][] randomSource() {
        Alphabet<Character> alph = Alphabets.characters('a', 'k');

        Object[][] arg = new Object[32][];

        for (int i = 0; i < arg.length; i++) {
            arg[i] = new Object[] {randomAutomaton(alph), randomAutomaton(alph), i};
        }

        return arg;
    }

    private static <I> CompactMTS<I> randomAutomaton(Alphabet<I> alphabet) {

        final Random rnd = new Random(42);
        final int stateCount = rnd.nextInt(64) + 1;

        final int[] states = new int[stateCount];
        final CompactMTS<I> res = new CompactMTS<>(alphabet);

        states[0] = res.addInitialState();

        for (int i = 1; i < stateCount; i++) {
            states[i] = res.addState();
        }

        final int transitionCount = rnd.nextInt(Math.min(64, stateCount * stateCount));

        for (int i = 0; i < transitionCount; i++) {
            final int src = states[rnd.nextInt(stateCount)];
            final int dest = states[rnd.nextInt(stateCount)];
            final I symbol = alphabet.getSymbol(rnd.nextInt(alphabet.size()));
            final ModalType m = (rnd.nextBoolean() ? ModalType.MUST : ModalType.MAY);

            res.addModalTransition(src, symbol, dest, m);
        }

        return res;
    }

    private static <S, I, T, TP extends ModalEdgeProperty> T getSingleTransition(ModalTransitionSystem<S, I, T, TP> mts,
                                                                                 S source,
                                                                                 I input,
                                                                                 S target) {

        final Collection<T> transitions = mts.getTransitions(source, input);

        for (final T t : transitions) {
            if (target.equals(mts.getSuccessor(t))) {
                return t;
            }
        }

        return null;
    }

}