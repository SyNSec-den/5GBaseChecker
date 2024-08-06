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
package net.automatalib.automata.base.compact;

import java.util.Arrays;
import java.util.Iterator;

import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract super class that refines {@link AbstractCompactDeterministic} for transition-property-less automata. As a
 * result, transitions may be represented as integers (where a transition object effectively <i>is</i> the successor).
 * <p>
 * Provides further default implementations for {@link FullIntAbstraction} concepts.
 *
 * @param <I>
 *         input symbol type
 * @param <SP>
 *         state property type
 *
 * @author frohme
 * @author Malte Isberner
 */
public abstract class AbstractCompactSimpleDeterministic<I, SP>
        extends AbstractCompactDeterministic<I, Integer, SP, Void> {

    protected int[] transitions;

    public AbstractCompactSimpleDeterministic(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
        this.transitions = new int[stateCapacity * numInputs()];
        Arrays.fill(this.transitions, AbstractCompact.INVALID_STATE);
    }

    protected AbstractCompactSimpleDeterministic(Alphabet<I> alphabet, AbstractCompactSimpleDeterministic<?, SP> other) {
        super(alphabet, other);
        this.transitions = other.transitions.clone();
    }

    @Override
    public Integer createTransition(int successor, Void property) {
        return successor;
    }

    @Override
    // Overridden for performance reasons (to prevent autoboxing of default implementation)
    public @Nullable Integer getState(Iterable<? extends I> input) {
        return toState(getIntSuccessor(getIntInitialState(), input));
    }

    @Override
    // Overridden for performance reasons (to prevent autoboxing of default implementation)
    public @Nullable Integer getSuccessor(Integer state, Iterable<? extends I> input) {
        return toState(getIntSuccessor(state.intValue(), input));
    }

    @Override
    public @Nullable Integer getTransition(int state, int input) {
        return toState(transitions[toMemoryIndex(state, input)]);
    }

    @Override
    protected void updateTransitionStorage(Payload payload) {
        this.transitions = updateTransitionStorage(this.transitions, AbstractCompact.INVALID_STATE, payload);
    }

    @Override
    public void setTransitionProperty(Integer transition, Void property) {}

    @Override
    public void removeAllTransitions(Integer state) {
        final int lower = state * numInputs();
        final int upper = lower + numInputs();
        Arrays.fill(transitions, lower, upper, AbstractCompact.INVALID_STATE);
    }

    @Override
    public void setTransition(int state, int input, @Nullable Integer transition) {
        setTransition(state, input, toId(transition));
    }

    @Override
    public void setTransition(int state, int input, int successor, Void property) {
        setTransition(state, input, successor);
    }

    public void setTransition(int state, int inputIdx, int succ) {
        transitions[toMemoryIndex(state, inputIdx)] = succ;
    }

    @Override
    public Void getTransitionProperty(Integer transition) {
        return null;
    }

    @Override
    public void clear() {
        Arrays.fill(transitions, 0, size() * numInputs(), AbstractCompact.INVALID_STATE);
        super.clear();
    }

    @Override
    public int getIntSuccessor(Integer transition) {
        return toId(transition);
    }

    private int getIntSuccessor(int state, Iterable<? extends I> input) {
        int current = state;

        Iterator<? extends I> inputIt = input.iterator();

        while (current >= 0 && inputIt.hasNext()) {
            current = transitions[toMemoryIndex(current, getSymbolIndex(inputIt.next()))];
        }

        return current;
    }
}