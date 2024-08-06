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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.ts.powerset.DeterministicPowersetView;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract super class that refines {@link AbstractCompact} for deterministic automata. This class provides default
 * implementations for several of the {@link MutableDeterministic.FullIntAbstraction} concepts, such that subclasses
 * only need to care about the primitive-based implementations.
 *
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 *
 * @author frohme
 * @author Malte Isberner
 */
public abstract class AbstractCompactDeterministic<I, T, SP, TP> extends AbstractCompact<I, T, SP, TP> implements
                                                                                                       MutableDeterministic<Integer, I, T, SP, TP>,
                                                                                                       MutableDeterministic.StateIntAbstraction<I, T, SP, TP>,
                                                                                                       MutableDeterministic.FullIntAbstraction<T, SP, TP> {

    private int initial = AbstractCompact.INVALID_STATE;

    public AbstractCompactDeterministic(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
    }

    protected AbstractCompactDeterministic(Alphabet<I> alphabet, AbstractCompactDeterministic<?, ?, ?, ?> other) {
        super(alphabet, other);
        this.initial = other.initial;
    }

    @Override
    public void setInitialState(@Nullable Integer state) {
        setInitialState(toId(state));
    }

    @Override
    public void setInitialState(int stateId) {
        initial = stateId;
    }

    @Override
    public SP getStateProperty(Integer state) {
        return getStateProperty(state.intValue());
    }

    @Override
    public int getIntInitialState() {
        return initial;
    }

    @Override
    public @Nullable Integer getInitialState() {
        return toState(initial);
    }

    @Override
    public @Nullable T getTransition(int state, I input) {
        return getTransition(state, getSymbolIndex(input));
    }

    @Override
    public @Nullable T getTransition(Integer state, I input) {
        return getTransition(state.intValue(), input);
    }

    @Override
    public void setTransition(Integer state, I input, @Nullable T transition) {
        setTransition(state.intValue(), getSymbolIndex(input), transition);
    }

    @Override
    public void setTransition(int state, I input, @Nullable T transition) {
        setTransition(state, getSymbolIndex(input), transition);
    }

    @Override
    public void setTransition(int state, I input, int successor, TP property) {
        setTransition(state, getSymbolIndex(input), successor, property);
    }

    @Override
    public void setStateProperty(Integer state, SP property) {
        setStateProperty(state.intValue(), property);
    }

    @Override
    public T createTransition(Integer successor, TP properties) {
        return createTransition(successor.intValue(), properties);
    }

    @Override
    public int addIntInitialState(@Nullable SP property) {
        setInitial(addIntState(property), true);
        return this.initial;
    }

    @Override
    public Integer getSuccessor(T transition) {
        return getIntSuccessor(transition);
    }

    @Override
    public void clear() {
        this.initial = AbstractCompact.INVALID_STATE;
        super.clear();
    }

    @Override
    public FullIntAbstraction<T, SP, TP> fullIntAbstraction(Alphabet<I> alphabet) {
        if (Objects.equals(getInputAlphabet(), alphabet)) {
            return this;
        }
        return MutableDeterministic.super.fullIntAbstraction(alphabet);
    }

    @Override
    public StateIntAbstraction<I, T, SP, TP> stateIntAbstraction() {
        return this;
    }

    @Override
    public DeterministicPowersetView<Integer, I, T> powersetView() {
        return new DeterministicPowersetView<>(this);
    }

    @Override
    public Collection<I> getLocalInputs(Integer state) {
        final Alphabet<I> alphabet = getInputAlphabet();
        final List<I> result = new ArrayList<>(alphabet.size());
        for (final I i : alphabet) {
            if (getTransition(state, i) != null) {
                result.add(i);
            }
        }

        return result;
    }
}
