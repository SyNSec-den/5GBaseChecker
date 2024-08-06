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

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.base.compact.UniversalCompactSimpleDet;
import net.automatalib.automata.transducers.MutableMooreMachine;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactMoore<I, @Nullable O> extends UniversalCompactSimpleDet<I, O>
        implements MutableMooreMachine<Integer, I, Integer, O> {

    public CompactMoore(Alphabet<I> alphabet) {
        super(alphabet);
    }

    public CompactMoore(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
    }

    public CompactMoore(CompactMoore<I, O> other) {
        this(other.getInputAlphabet(), other);
    }

    protected CompactMoore(Alphabet<I> alphabet, CompactMoore<?, O> other) {
        super(alphabet, other);
    }

    @Override
    public void setStateOutput(Integer state, O output) {
        setStateProperty(state, output);
    }

    @Override
    public O getStateOutput(Integer state) {
        return getStateProperty(state);
    }

    public static final class Creator<I, @Nullable O> implements AutomatonCreator<CompactMoore<I, O>, I> {

        @Override
        public CompactMoore<I, O> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return new CompactMoore<>(alphabet, sizeHint, DEFAULT_RESIZE_FACTOR);
        }

        @Override
        public CompactMoore<I, O> createAutomaton(Alphabet<I> alphabet) {
            return new CompactMoore<>(alphabet);
        }
    }

}
