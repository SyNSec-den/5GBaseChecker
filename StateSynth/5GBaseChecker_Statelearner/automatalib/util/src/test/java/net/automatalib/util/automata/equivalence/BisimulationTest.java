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
package net.automatalib.util.automata.equivalence;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.automata.Automaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author msc
 */
public class BisimulationTest {

    @Test
    public void bisimTestLoop() {

        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'd');
        final CompactMTS<String> a = new CompactMTS<>(alphabet);
        final CompactMTS<String> b = new CompactMTS<>(alphabet);

        final Integer as0 = a.addInitialState();
        final Integer as1 = a.addState();
        final Integer as2 = a.addState();

        final Integer bs0 = b.addInitialState();

        a.addTransition(as0, "a", as1, null);
        a.addTransition(as1, "a", as2, null);
        a.addTransition(as2, "a", as2, null);

        b.addTransition(bs0, "a", bs0, null);

        Assert.assertTrue(testBisimulationEquivalence(a, b, alphabet));
    }

    @Test
    public void bisimTestDiff() {

        final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'd');
        final CompactMTS<String> a = new CompactMTS<>(alphabet);
        final CompactMTS<String> b = new CompactMTS<>(alphabet);

        final Integer as0 = a.addInitialState();
        final Integer as1 = a.addState();
        final Integer as2 = a.addState();

        final Integer bs0 = b.addInitialState();

        a.addTransition(as0, "a", as1, null);
        a.addTransition(as1, "a", as2, null);
        a.addTransition(as2, "a", as2, null);

        b.addTransition(bs0, "a", bs0, null);
        b.addTransition(bs0, "b", bs0, null);

        final Set<Pair<Integer, Integer>> equivalentStates =
                Bisimulation.bisimulationEquivalenceRelation(a, b, alphabet);
        Assert.assertFalse(equivalentStates.contains(Pair.of(as0, bs0)));
    }

    private static <AS, I, AT, A extends Automaton<AS, I, AT>, BS, BT, B extends Automaton<BS, I, BT>> boolean testBisimulationEquivalence(
            A a,
            B b,
            Collection<I> inputs) {

        Set<Pair<AS, BS>> bisim = Bisimulation.bisimulationEquivalenceRelation(a, b, inputs);

        Set<AS> statesA = new HashSet<>(a.getStates());
        Set<BS> statesB = new HashSet<>(b.getStates());

        for (Pair<AS, BS> p : bisim) {
            statesA.remove(p.getFirst());
            statesB.remove(p.getSecond());
        }

        return statesA.isEmpty() && statesB.isEmpty();
    }

}