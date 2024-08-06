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
package net.automatalib.automata.fsa;

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.WrapperUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author fh
 */
public interface MutableFSA<S, I> extends FiniteStateAcceptor<S, I>, MutableAutomaton<S, I, S, Boolean, Void> {

    default void flipAcceptance() {
        for (S state : this) {
            setAccepting(state, !isAccepting(state));
        }
    }

    void setAccepting(S state, boolean accepting);

    S addState(boolean accepting);

    @Override
    default S addState() {
        return addState(false);
    }

    @Override
    default S addState(@Nullable Boolean property) {
        return addState(WrapperUtil.booleanValue(property));
    }

    @Override
    default S addInitialState() {
        return addInitialState(false);
    }

    default S addInitialState(boolean accepting) {
        S init = addState(accepting);
        setInitial(init, true);
        return init;
    }

    @Override
    default S addInitialState(@Nullable Boolean property) {
        return addInitialState(WrapperUtil.booleanValue(property));
    }

    @Override
    default void setStateProperty(S state, Boolean property) {
        setAccepting(state, property.booleanValue());
    }

    @Override
    default void setTransitionProperty(S transition, Void property) {}

    @Override
    default S createTransition(S successor, Void properties) {
        return successor;
    }

    @Override
    default S copyTransition(S trans, S succ) {
        return succ;
    }
}
