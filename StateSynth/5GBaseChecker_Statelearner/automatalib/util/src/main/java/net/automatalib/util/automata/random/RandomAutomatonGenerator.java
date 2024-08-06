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
package net.automatalib.util.automata.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.random.RandomUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RandomAutomatonGenerator<S, I, T, SP, TP, A extends MutableAutomaton<S, I, T, SP, TP>> {

    protected final RandomUtil random;
    protected final List<? extends I> inputs;
    protected final List<? extends SP> spList;
    protected final List<? extends TP> tpList;
    @SuppressWarnings("PMD.LooseCoupling") // ArrayList#ensureCapacity is required
    protected final ArrayList<S> states;
    protected final A automaton;

    public RandomAutomatonGenerator(Random random,
                                    Collection<? extends I> inputs,
                                    Collection<? extends SP> stateProps,
                                    Collection<? extends TP> transProps,
                                    A automaton) {
        this.random = new RandomUtil(random);

        this.spList = CollectionsUtil.randomAccessList(stateProps);
        this.tpList = CollectionsUtil.randomAccessList(transProps);

        this.inputs = CollectionsUtil.randomAccessList(inputs);
        this.states = new ArrayList<>();
        this.automaton = automaton;
    }

    public A getAutomaton() {
        return automaton;
    }

    protected @Nullable TP randomTransProperty() {
        return random.choose(tpList);
    }

    protected @Nullable S randomState() {
        return random.choose(states);
    }

    protected @Nullable S randomDistinctState(int stateIdx) {
        if (states.size() == 1) {
            return null;
        }

        int idx = random.getRandom().nextInt(states.size() - 1);

        if (idx >= stateIdx) {
            idx++;
        }

        return states.get(idx);
    }

    protected @Nullable I randomInput() {
        return random.choose(inputs);
    }

    public void addStates(int numStates) {
        states.ensureCapacity(states.size() + numStates);

        for (int i = 0; i < numStates; i++) {
            S state = automaton.addState(randomStateProperty());
            states.add(state);
        }
    }

    protected @Nullable SP randomStateProperty() {
        return random.choose(spList);
    }

    public void chooseInitial() {
        S init = random.choose(states);
        automaton.setInitial(init, true);
    }

    public void chooseInitials(int num) {
        List<S> inits = random.sampleUnique(states, num);

        for (S init : inits) {
            automaton.setInitial(init, true);
        }
    }

}
