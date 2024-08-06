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
package net.automatalib.automata.base.fast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.ShrinkableDeterministic;
import net.automatalib.ts.powerset.DeterministicPowersetView;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractFastMutableDet<S extends AbstractFastState<T>, I, T, SP, TP>
        extends AbstractFastMutable<S, I, T, SP, TP> implements ShrinkableDeterministic<S, I, T, SP, TP> {

    private @Nullable S initialState;

    public AbstractFastMutableDet(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public void setTransition(S state, I input, @Nullable T transition) {
        int inputIdx = inputAlphabet.getSymbolIndex(input);
        state.setTransitionObject(inputIdx, transition);
    }

    @Override
    public @Nullable S getInitialState() {
        return initialState;
    }

    @Override
    public void setInitialState(@Nullable S state) {
        this.initialState = state;
    }

    @Override
    public @Nullable T getTransition(S state, I input) {
        int inputIdx = inputAlphabet.getSymbolIndex(input);
        return state.getTransitionObject(inputIdx);
    }

    @Override
    public void removeState(S state, @Nullable S replacement) {
        super.removeState(state, replacement);

        if (state.equals(initialState)) {
            this.initialState = replacement;
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.initialState = null;
    }

    @Override
    public DeterministicPowersetView<S, I, T> powersetView() {
        return new DeterministicPowersetView<>(this);
    }

    @Override
    public Collection<I> getLocalInputs(S state) {
        final Alphabet<I> alphabet = getInputAlphabet();
        final int alphabetSize = alphabet.size();
        final List<I> result = new ArrayList<>(alphabetSize);

        for (int i = 0; i < alphabetSize; i++) {
            if (state.getTransitionObject(i) != null) {
                result.add(alphabet.getSymbol(i));
            }
        }

        return result;
    }
}
