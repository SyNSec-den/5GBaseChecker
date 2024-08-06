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
package net.automatalib.automata.spa;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.SPAAlphabet;

/**
 * A stack-based implementation for {@link SPA}s.
 *
 * @param <S>
 *         unified super type of procedure states
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class StackSPA<S, I> implements SPA<StackSPAState<I, S>, I>, SimpleDTS<StackSPAState<I, S>, I> {

    private final SPAAlphabet<I> alphabet;
    private final I initialCall;
    private final Map<I, DFA<S, I>> procedures;

    // cast is fine, because we make sure to only query states belonging to the respective procedures
    @SuppressWarnings("unchecked")
    public StackSPA(SPAAlphabet<I> alphabet, I initialCall, Map<I, ? extends DFA<? extends S, I>> procedures) {
        this.alphabet = alphabet;
        this.initialCall = initialCall;
        this.procedures = (Map<I, DFA<S, I>>) procedures;
    }

    @Override
    public StackSPAState<I, S> getTransition(StackSPAState<I, S> state, I input) {
        if (state.isSink() || state.isTerm()) {
            return StackSPAState.sink();
        } else if (alphabet.isInternalSymbol(input)) {
            if (state.isInit()) {
                return StackSPAState.sink();
            }

            final DFA<S, I> model = state.getProcedure();
            final S next = model.getTransition(state.getCurrentState(), input);

            // undefined internal transition
            if (next == null) {
                return StackSPAState.sink();
            }

            return state.updateState(next);
        } else if (alphabet.isCallSymbol(input)) {
            if (state.isInit() && !Objects.equals(this.initialCall, input)) {
                return StackSPAState.sink();
            }

            final DFA<S, I> model = this.procedures.get(input);

            if (model == null) {
                return StackSPAState.sink();
            }

            final S next = model.getInitialState();

            if (next == null) {
                return StackSPAState.sink();
            }

            // store the procedural successor in the stack so that we don't need to look it up on return symbols
            final StackSPAState<I, S> returnState;
            if (state.isInit()) {
                returnState = StackSPAState.term();
            } else {
                final S succ = state.getProcedure().getSuccessor(state.getCurrentState(), input);
                if (succ == null) {
                    return StackSPAState.sink();
                }
                returnState = state.updateState(succ);
            }

            return returnState.push(model, next);
        } else if (alphabet.isReturnSymbol(input)) {
            if (state.isInit()) {
                return StackSPAState.sink();
            }

            // if we returned the state before, we checked that a procedure is available
            final DFA<S, I> model = state.getProcedure();

            // cannot return, reject word
            if (!model.isAccepting(state.getCurrentState())) {
                return StackSPAState.sink();
            }

            return state.pop();
        } else {
            return StackSPAState.sink();
        }
    }

    @Override
    public boolean isAccepting(StackSPAState<I, S> state) {
        return state.isTerm();
    }

    @Override
    public StackSPAState<I, S> getInitialState() {
        return StackSPAState.init();
    }

    @Override
    public I getInitialProcedure() {
        return initialCall;
    }

    @Override
    public SPAAlphabet<I> getInputAlphabet() {
        return this.alphabet;
    }

    @Override
    public Map<I, DFA<?, I>> getProcedures() {
        return Collections.unmodifiableMap(procedures);
    }
}