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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.ts.powerset.FastPowersetDTS;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractFastMutableNondet<S extends AbstractFastState<Collection<T>>, I, T, SP, TP>
        extends AbstractFastMutable<S, I, T, SP, TP> {

    private final Set<S> initialStates = new HashSet<>();

    public AbstractFastMutableNondet(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public Set<S> getInitialStates() {
        return initialStates;
    }

    @Override
    public Collection<T> getTransitions(S state, I input) {
        int inputIdx = inputAlphabet.getSymbolIndex(input);
        final Collection<T> result = state.getTransitionObject(inputIdx);
        return result == null ? Collections.emptySet() : result;
    }

    @Override
    public void clear() {
        super.clear();
        initialStates.clear();
    }

    @Override
    public void setInitial(S state, boolean initial) {
        if (initial) {
            initialStates.add(state);
        } else {
            initialStates.remove(state);
        }
    }

    @Override
    public void setTransitions(S state, I input, Collection<? extends T> transitions) {
        int inputIdx = inputAlphabet.getSymbolIndex(input);
        state.setTransitionObject(inputIdx, new HashSet<>(transitions));
    }

    @Override
    public void removeState(S state, @Nullable S replacement) {
        super.removeState(state, replacement);

        if (initialStates.remove(state) && replacement != null) {
            initialStates.add(replacement);
        }
    }

    @Override
    public FastPowersetDTS<S, I, T> powersetView() {
        return new FastPowersetDTS<>(this);
    }

    @Override
    public Collection<I> getLocalInputs(S state) {
        final Alphabet<I> alphabet = getInputAlphabet();
        final int alphabetSize = alphabet.size();
        final List<I> result = new ArrayList<>(alphabetSize);

        for (int i = 0; i < alphabetSize; i++) {
            final Collection<T> trans = state.getTransitionObject(i);
            if (trans != null && !trans.isEmpty()) {
                result.add(alphabet.getSymbol(i));
            }
        }

        return result;
    }
}
