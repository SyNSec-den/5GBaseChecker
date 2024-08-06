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

import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A default implementation for {@link AbstractCompactDeterministic} that uses {@link CompactTransition} as transition
 * type and supports various types of state and transition properties.
 *
 * @param <I>
 *         input symbol type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 *
 * @author frohme
 */
public class UniversalCompactDet<I, SP, TP> extends AbstractCompactDeterministic<I, CompactTransition<TP>, SP, TP> {

    private int[] transitions;
    private @Nullable Object[] stateProperties;
    private @Nullable Object[] transitionProperties;

    public UniversalCompactDet(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public UniversalCompactDet(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);

        final int numTrans = stateCapacity * numInputs();
        this.transitions = new int[numTrans];
        this.stateProperties = new Object[stateCapacity];
        this.transitionProperties = new Object[numTrans];

        Arrays.fill(this.transitions, AbstractCompact.INVALID_STATE);
    }

    protected UniversalCompactDet(Alphabet<I> alphabet, UniversalCompactDet<?, SP, TP> other) {
        super(alphabet, other);
        this.transitions = other.transitions.clone();
        this.stateProperties = other.stateProperties.clone();
        this.transitionProperties = other.transitionProperties.clone();
    }

    @Override
    public @Nullable CompactTransition<TP> getTransition(int state, int input) {
        final int idx = toMemoryIndex(state, input);
        final int succ = transitions[idx];

        if (succ == AbstractCompact.INVALID_STATE) {
            return null;
        }

        @SuppressWarnings("unchecked")
        final TP output = (TP) transitionProperties[idx];

        return new CompactTransition<>(idx, succ, output);
    }

    @Override
    public int getIntSuccessor(CompactTransition<TP> transition) {
        return transition.getSuccId();
    }

    @Override
    public void setStateProperty(int state, @Nullable SP property) {
        this.stateProperties[state] = property;
    }

    @Override
    public void setTransitionProperty(CompactTransition<TP> transition, TP property) {
        transition.setProperty(property);

        if (transition.isAutomatonTransition()) {
            transitionProperties[transition.getMemoryIdx()] = property;
        }
    }

    @Override
    public CompactTransition<TP> createTransition(int successor, TP property) {
        return new CompactTransition<>(successor, property);
    }

    @Override
    public void removeAllTransitions(Integer state) {
        final int lower = state * numInputs();
        final int upper = lower + numInputs();
        Arrays.fill(transitions, lower, upper, AbstractCompact.INVALID_STATE);
        Arrays.fill(transitionProperties, lower, upper, null);

    }

    @Override
    public void setTransition(int state, int input, @Nullable CompactTransition<TP> transition) {
        if (transition == null) {
            setTransition(state, input, AbstractCompact.INVALID_STATE, null);
        } else {
            setTransition(state, input, transition.getSuccId(), transition.getProperty());
            transition.setMemoryIdx(toMemoryIndex(state, input));
        }
    }

    @Override
    public void setTransition(int state, int input, int successor, @Nullable TP property) {
        final int idx = toMemoryIndex(state, input);
        transitions[idx] = successor;
        transitionProperties[idx] = property;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SP getStateProperty(int state) {
        return (SP) stateProperties[state];
    }

    @Override
    public TP getTransitionProperty(CompactTransition<TP> transition) {
        return transition.getProperty();
    }

    @Override
    public void clear() {
        int endIdx = size() * numInputs();
        Arrays.fill(stateProperties, 0, size(), null);
        Arrays.fill(transitions, 0, endIdx, AbstractCompact.INVALID_STATE);
        Arrays.fill(transitionProperties, 0, endIdx, null);

        super.clear();
    }

    @Override
    protected void updateStateStorage(Payload payload) {
        this.stateProperties = updateStateStorage(this.stateProperties, null, payload);
        super.updateStateStorage(payload);
    }

    @Override
    protected void updateTransitionStorage(Payload payload) {
        this.transitions = updateTransitionStorage(this.transitions, AbstractCompact.INVALID_STATE, payload);
        this.transitionProperties = updateTransitionStorage(this.transitionProperties, null, payload);
    }
}
