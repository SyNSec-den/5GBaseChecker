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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class ADSRandomMealyTest extends AbstractADSTest {

    final boolean completeExpected, partialExpected;

    final CompactMealy<Integer, Character> target;

    @Factory(dataProvider = "randomMealies")
    public ADSRandomMealyTest(final boolean completeExpected,
                              final boolean partialExpected,
                              final CompactMealy<Integer, Character> target) {
        this.completeExpected = completeExpected;
        this.partialExpected = partialExpected;
        this.target = target;
    }

    @DataProvider(name = "randomMealies")
    public static Iterator<Object[]> getParameters() {

        final List<Object[]> result = new LinkedList<>();

        final Random r = new Random(1337);
        final Alphabet<Integer> input = Alphabets.integers(1, 5);
        final Alphabet<Character> output = Alphabets.characters('a', 'f');

        result.add(new Object[] {true, true, RandomAutomata.randomMealy(r, 10, input, output)});
        result.add(new Object[] {true, true, RandomAutomata.randomMealy(r, 20, input, output)});
        result.add(new Object[] {true, true, RandomAutomata.randomMealy(r, 30, input, output)});
        result.add(new Object[] {false, true, RandomAutomata.randomMealy(r, 40, input, output)});
        result.add(new Object[] {false, true, RandomAutomata.randomMealy(r, 50, input, output)});
        result.add(new Object[] {false, false, RandomAutomata.randomMealy(r, 60, input, output)});
        result.add(new Object[] {false, true, RandomAutomata.randomMealy(r, 70, input, output)});
        result.add(new Object[] {false, true, RandomAutomata.randomMealy(r, 80, input, output)});
        result.add(new Object[] {false, true, RandomAutomata.randomMealy(r, 90, input, output)});
        result.add(new Object[] {false, true, RandomAutomata.randomMealy(r, 100, input, output)});

        return result.iterator();
    }

    @Test
    public void testTarget() {
        if (this.completeExpected) {
            super.verifySuccess(target);
        } else {
            super.verifyFailure(target);
        }
    }

    @Test
    public void testTargetWithSingleton() {

        final List<Integer> targetStates = new ArrayList<>(this.target.getStates());
        Collections.shuffle(targetStates, new Random(42));

        super.verifySuccess(target, targetStates.subList(0, 1));
    }

    @Test
    public void testTargetWithHalfSubset() {

        final List<Integer> targetStates = new ArrayList<>(this.target.getStates());
        Collections.shuffle(targetStates, new Random(42));

        if (this.partialExpected) {
            super.verifySuccess(target, targetStates.subList(0, this.target.size() / 2));
        } else {
            super.verifyFailure(target, targetStates.subList(0, this.target.size() / 2));
        }
    }
}
