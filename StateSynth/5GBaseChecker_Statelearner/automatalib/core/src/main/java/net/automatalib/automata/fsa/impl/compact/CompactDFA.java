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
package net.automatalib.automata.fsa.impl.compact;

import java.util.BitSet;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.base.compact.AbstractCompactSimpleDeterministic;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.commons.util.WrapperUtil;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactDFA<I> extends AbstractCompactSimpleDeterministic<I, Boolean> implements MutableDFA<Integer, I> {

    private final BitSet acceptance;

    public CompactDFA(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public CompactDFA(Alphabet<I> alphabet, int stateCapacity) {
        this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public CompactDFA(Alphabet<I> alphabet, float resizeFactor) {
        this(alphabet, DEFAULT_INIT_CAPACITY, resizeFactor);
    }

    public CompactDFA(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
        this.acceptance = new BitSet();
    }

    public CompactDFA(CompactDFA<I> other) {
        this(other.getInputAlphabet(), other);
    }

    protected CompactDFA(Alphabet<I> alphabet, CompactDFA<?> other) {
        super(alphabet, other);
        this.acceptance = (BitSet) other.acceptance.clone();
    }

    public <I2> CompactDFA<I2> translate(Alphabet<I2> newAlphabet) {
        if (newAlphabet.size() != numInputs()) {
            throw new IllegalArgumentException(
                    "Alphabet sizes must match, but they do not (old/new): " + numInputs() + " vs. " +
                    newAlphabet.size());
        }
        return new CompactDFA<>(newAlphabet, this);
    }

    @Override
    public void flipAcceptance() {
        acceptance.flip(0, size());
    }

    @Override
    public void setAccepting(Integer state, boolean accepting) {
        setAccepting(state.intValue(), accepting);
    }

    public void setAccepting(int state, boolean accepting) {
        acceptance.set(state, accepting);
    }

    @Override
    public Integer addState(boolean accepting) {
        return addState(Boolean.valueOf(accepting));
    }

    @Override
    public void clear() {
        acceptance.clear();
        super.clear();
    }

    @Override
    public void setStateProperty(int stateId, @Nullable Boolean property) {
        setAccepting(stateId, WrapperUtil.booleanValue(property));
    }

    @Override
    public Boolean getStateProperty(int stateId) {
        return isAccepting(stateId);
    }

    public boolean isAccepting(int stateId) {
        return acceptance.get(stateId);
    }

    @Override
    public boolean isAccepting(Integer state) {
        return isAccepting(state.intValue());
    }

    public static final class Creator<I> implements AutomatonCreator<CompactDFA<I>, I> {

        @Override
        public CompactDFA<I> createAutomaton(Alphabet<I> alphabet, int numStates) {
            return new CompactDFA<>(alphabet, numStates);
        }

        @Override
        public CompactDFA<I> createAutomaton(Alphabet<I> alphabet) {
            return new CompactDFA<>(alphabet);
        }
    }

}
