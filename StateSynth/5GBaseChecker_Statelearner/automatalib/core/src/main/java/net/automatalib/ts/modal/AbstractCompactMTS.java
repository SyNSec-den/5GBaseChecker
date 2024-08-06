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
package net.automatalib.ts.modal;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.automata.base.compact.AbstractCompact;
import net.automatalib.commons.util.collections.PositiveIntSet;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An abstract base-implementation for {@link MutableModalTransitionSystem}s.
 *
 * @param <I>
 *         input symbol type
 * @param <TP>
 *         (specific) transition property type
 *
 * @author msc
 */
public abstract class AbstractCompactMTS<I, TP extends MutableModalEdgeProperty>
        extends AbstractCompact<I, MTSTransition<TP>, Void, TP>
        implements MutableModalTransitionSystem<Integer, I, MTSTransition<TP>, TP> {

    private final BitSet initialStates;
    private @Nullable Set<MTSTransition<TP>>[] transitions;

    public AbstractCompactMTS(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public AbstractCompactMTS(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);

        this.initialStates = new BitSet();
        this.transitions = new Set[stateCapacity * numInputs()];
    }

    @Override
    public void setInitial(Integer state, boolean initial) {
        if (initial) {
            this.initialStates.set(state);
        } else {
            this.initialStates.clear(state);
        }
    }

    @Override
    public void setStateProperty(Integer state, Void property) {}

    @Override
    public void setStateProperty(int state, Void property) {}

    @Override
    public void setTransitionProperty(MTSTransition<TP> transition, TP property) {
        transition.setProperty(property);
    }

    @Override
    public void setTransitions(Integer state, I input, Collection<? extends MTSTransition<TP>> transitions) {
        this.transitions[toMemoryIndex(state, getSymbolIndex(input))] = new HashSet<>(transitions);
    }

    @Override
    public void removeAllTransitions(Integer state, I input) {
        transitions[toMemoryIndex(state, getSymbolIndex(input))] = null;
    }

    @Override
    public void removeAllTransitions(Integer state) {
        for (final I i : getInputAlphabet()) {
            removeAllTransitions(state, i);
        }
    }

    @Override
    public MTSTransition<TP> createTransition(Integer successor) {
        return createTransition(successor, null);
    }

    @Override
    public MTSTransition<TP> createTransition(Integer successor, @Nullable TP properties) {
        return new MTSTransition<>(successor, properties == null ? getDefaultTransitionProperty() : properties);
    }

    @Override
    public MTSTransition<TP> addModalTransition(Integer src, I input, Integer tgt, ModalType modalType) {
        return this.addTransition(src, input, tgt, buildModalProperty(modalType));
    }

    @Override
    public Void getStateProperty(Integer state) {
        return null;
    }

    @Override
    public TP getTransitionProperty(MTSTransition<TP> transition) {
        return transition.getProperty();
    }

    @Override
    public Collection<MTSTransition<TP>> getTransitions(Integer state, I input) {
        final Set<MTSTransition<TP>> trans = transitions[toMemoryIndex(state, getSymbolIndex(input))];
        return trans == null ? Collections.emptySet() : Collections.unmodifiableCollection(trans);
    }

    @Override
    public Integer getSuccessor(MTSTransition<TP> transition) {
        return transition.getTarget();
    }

    @Override
    public Set<Integer> getInitialStates() {
        return new PositiveIntSet(this.initialStates);
    }

    @Override
    public void clear() {
        Arrays.fill(transitions, 0, size() * numInputs(), null);
        initialStates.clear();

        super.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateTransitionStorage(Payload payload) {
        this.transitions =
                (Set<MTSTransition<TP>>[]) updateTransitionStorage(this.transitions, Set[]::new, null, payload);
        super.updateTransitionStorage(payload);
    }

    protected abstract TP getDefaultTransitionProperty();

    protected abstract TP buildModalProperty(ModalType type);

}
