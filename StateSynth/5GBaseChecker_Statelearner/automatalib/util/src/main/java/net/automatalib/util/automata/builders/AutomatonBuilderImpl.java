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
package net.automatalib.util.automata.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.misberner.duzzt.annotations.DSLAction;
import com.github.misberner.duzzt.annotations.GenerateEmbeddedDSL;
import net.automatalib.automata.MutableAutomaton;

@GenerateEmbeddedDSL(name = "AutomatonBuilder",
                     enableAllMethods = false,
                     syntax = "((from (on (withProperty? <<to* loop? to*>>)+)+)|withStateProperty|withInitial)* create")
@SuppressWarnings("nullness") // nullness correctness guaranteed by states of regular expression
class AutomatonBuilderImpl<S, I, T, SP, TP, A extends MutableAutomaton<S, ? super I, T, ? super SP, ? super TP>> {

    protected final A automaton;
    private final Map<Object, S> stateMap = new HashMap<>();

    protected List<S> currentStates;
    protected List<I> currentInputs;
    protected TP currentTransProp;

    AutomatonBuilderImpl(A automaton) {
        this.automaton = automaton;
    }

    @DSLAction
    public void from(Object stateId) {
        this.currentStates = getStates(stateId);
        this.currentInputs = null;
    }

    @DSLAction
    public void from(Object firstStateId, Object... otherStateIds) {
        this.currentStates = getStates(firstStateId, otherStateIds);
    }

    protected List<S> getStates(Object firstStateId, Object... otherStateIds) {
        if (otherStateIds.length == 0) {
            return Collections.singletonList(getState(firstStateId));
        }
        List<S> result = new ArrayList<>(1 + otherStateIds.length);
        result.add(getState(firstStateId));
        for (Object otherId : otherStateIds) {
            result.add(getState(otherId));
        }
        return result;
    }

    protected S getState(Object stateId) {
        if (stateMap.containsKey(stateId)) {
            return stateMap.get(stateId);
        }
        S state = automaton.addState();
        stateMap.put(stateId, state);
        return state;
    }

    @DSLAction
    public void on(I input) {
        this.currentInputs = Collections.singletonList(input);
        this.currentTransProp = null;
    }

    @DSLAction
    @SafeVarargs
    public final void on(I firstInput, I... otherInputs) {
        this.currentInputs = new ArrayList<>(1 + otherInputs.length);
        this.currentInputs.add(firstInput);
        Collections.addAll(this.currentInputs, otherInputs);
        this.currentTransProp = null;
    }

    @DSLAction
    public void withProperty(TP transProp) {
        this.currentTransProp = transProp;
    }

    @DSLAction
    public void to(Object stateId) {
        S tgt = getState(stateId);
        for (S src : currentStates) {
            for (I input : currentInputs) {
                automaton.addTransition(src, input, tgt, currentTransProp);
            }
        }
    }

    @DSLAction
    public void loop() {
        for (S src : currentStates) {
            for (I input : currentInputs) {
                automaton.addTransition(src, input, src, currentTransProp);
            }
        }
    }

    @DSLAction(terminator = true)
    public A create() {
        return automaton;
    }

    @DSLAction
    public void withInitial(Object stateId) {
        S state = getState(stateId);
        automaton.setInitial(state, true);
    }

    @DSLAction
    public void withStateProperty(SP stateProperty, Object stateId) {
        S state = getState(stateId);
        automaton.setStateProperty(state, stateProperty);
    }
}
