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
package net.automatalib.automata.fsa.impl;

import net.automatalib.automata.base.fast.AbstractFastMutableNondet;
import net.automatalib.automata.fsa.MutableNFA;
import net.automatalib.commons.util.WrapperUtil;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FastNFA<I> extends AbstractFastMutableNondet<FastNFAState, I, FastNFAState, Boolean, Void>
        implements MutableNFA<FastNFAState, I> {

    public FastNFA(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public boolean isAccepting(FastNFAState state) {
        return state.isAccepting();
    }

    @Override
    public void setAccepting(FastNFAState state, boolean accepting) {
        state.setAccepting(accepting);
    }

    @Override
    public FastNFAState addState(boolean accepting) {
        return addState(Boolean.valueOf(accepting));
    }

    @Override
    protected FastNFAState createState(@Nullable Boolean property) {
        return new FastNFAState(inputAlphabet.size(), WrapperUtil.booleanValue(property));
    }

}
