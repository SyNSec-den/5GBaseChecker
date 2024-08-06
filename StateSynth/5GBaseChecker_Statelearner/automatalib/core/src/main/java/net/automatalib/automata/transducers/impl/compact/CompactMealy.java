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
package net.automatalib.automata.transducers.impl.compact;

import java.util.Arrays;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.base.compact.AbstractCompact;
import net.automatalib.automata.base.compact.AbstractCompactDeterministic;
import net.automatalib.automata.base.compact.CompactTransition;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactMealy<I, O> extends AbstractCompactDeterministic<I, CompactTransition<O>, Void, O> implements
                                                                                                       MutableMealyMachine<Integer, I, CompactTransition<O>, O>,
                                                                                                       StateLocalInputMealyMachine<Integer, I, CompactTransition<O>, O> {

    private int[] transitions;
    private @Nullable Object[] outputs;

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);

        final int size = stateCapacity * numInputs();

        this.transitions = new int[size];
        this.outputs = new Object[size];

        Arrays.fill(transitions, AbstractCompact.INVALID_STATE);
    }

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity) {
        this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public CompactMealy(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public CompactMealy(CompactMealy<I, O> other) {
        this(other.getInputAlphabet(), other);
    }

    protected CompactMealy(Alphabet<I> alphabet, CompactMealy<?, O> other) {
        super(alphabet, other);
        this.transitions = other.transitions.clone();
        this.outputs = other.outputs.clone();
    }

    @Override
    protected void updateTransitionStorage(Payload payload) {
        this.transitions = updateTransitionStorage(this.transitions, AbstractCompact.INVALID_STATE, payload);
        this.outputs = updateTransitionStorage(this.outputs, null, payload);
    }

    @Override
    public O getTransitionOutput(CompactTransition<O> transition) {
        return transition.getProperty();
    }

    @Override
    public O getTransitionProperty(CompactTransition<O> transition) {
        return transition.getProperty();
    }

    @Override
    public void setTransitionProperty(CompactTransition<O> transition, O property) {
        transition.setProperty(property);

        if (transition.isAutomatonTransition()) {
            outputs[transition.getMemoryIdx()] = property;
        }
    }

    @Override
    public void setTransitionOutput(CompactTransition<O> transition, O output) {
        setTransitionProperty(transition, output);
    }

    @Override
    public void removeAllTransitions(Integer state) {
        final int lower = state * numInputs();
        final int upper = lower + numInputs();
        Arrays.fill(transitions, lower, upper, AbstractCompact.INVALID_STATE);
        Arrays.fill(outputs, lower, upper, null);
    }

    @Override
    public int getIntSuccessor(CompactTransition<O> transition) {
        return transition.getSuccId();
    }

    @Override
    public CompactTransition<O> createTransition(int succId, O property) {
        return new CompactTransition<>(succId, property);
    }

    @Override
    public void setStateProperty(int state, Void property) {}

    @Override
    public Void getStateProperty(int stateId) {
        return null;
    }

    @Override
    public void setTransition(int state, int input, @Nullable CompactTransition<O> transition) {
        if (transition == null) {
            setTransition(state, input, AbstractCompact.INVALID_STATE, null);
        } else {
            setTransition(state, input, transition.getSuccId(), transition.getProperty());
            transition.setMemoryIdx(toMemoryIndex(state, input));
        }
    }

    @Override
    public void setTransition(int state, int input, int successor, @Nullable O property) {
        final int idx = toMemoryIndex(state, input);
        transitions[idx] = successor;
        outputs[idx] = property;
    }

    @Override
    public void clear() {
        int endIdx = size() * numInputs();
        Arrays.fill(transitions, 0, endIdx, AbstractCompact.INVALID_STATE);
        Arrays.fill(outputs, 0, endIdx, null);

        super.clear();
    }

    @Override
    public @Nullable CompactTransition<O> getTransition(int state, int input) {
        final int idx = toMemoryIndex(state, input);
        final int succ = transitions[idx];

        if (succ == AbstractCompact.INVALID_STATE) {
            return null;
        }

        @SuppressWarnings("unchecked")
        final O output = (O) outputs[idx];

        return new CompactTransition<>(idx, succ, output);
    }

    public static final class Creator<I, O> implements AutomatonCreator<CompactMealy<I, O>, I> {

        @Override
        public CompactMealy<I, O> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return new CompactMealy<>(alphabet, sizeHint);
        }

        @Override
        public CompactMealy<I, O> createAutomaton(Alphabet<I> alphabet) {
            return new CompactMealy<>(alphabet);
        }
    }

}
