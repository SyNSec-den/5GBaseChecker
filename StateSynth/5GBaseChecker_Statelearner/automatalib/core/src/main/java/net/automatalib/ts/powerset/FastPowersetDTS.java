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
package net.automatalib.ts.powerset;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.commons.util.nid.NumericID;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.PowersetViewTS;
import net.automatalib.ts.TransitionSystem;

public class FastPowersetDTS<S extends NumericID, I, T>
        implements DeterministicTransitionSystem<FastPowersetState<S>, I, Set<T>>,
                   PowersetViewTS<FastPowersetState<S>, I, Set<T>, S, T> {

    private final TransitionSystem<S, I, T> ts;

    public FastPowersetDTS(TransitionSystem<S, I, T> ts) {
        this.ts = ts;
    }

    @Override
    public FastPowersetState<S> getInitialState() {
        FastPowersetState<S> result = new FastPowersetState<>();
        for (S init : ts.getInitialStates()) {
            result.add(init, init.getId());
        }
        return result;
    }

    @Override
    public FastPowersetState<S> getSuccessor(Set<T> transition) {
        FastPowersetState<S> succ = new FastPowersetState<>();
        for (T t : transition) {
            S succS = ts.getSuccessor(t);
            succ.add(succS, succS.getId());
        }

        return succ;
    }

    @Override
    public FastPowersetState<S> getSuccessor(FastPowersetState<S> state, I input) {
        FastPowersetState<S> succ = new FastPowersetState<>();

        for (S s : state) {
            Collection<S> succs = ts.getSuccessors(s, input);
            for (S succS : succs) {
                succ.add(succS, succS.getId());
            }
        }

        return succ;
    }

    @Override
    public Set<T> getTransition(FastPowersetState<S> state, I input) {
        Set<T> result = new HashSet<>();
        for (S s : state) {
            Collection<T> transitions = ts.getTransitions(s, input);
            result.addAll(transitions);
        }
        return result;
    }

    @Override
    public Collection<S> getOriginalStates(FastPowersetState<S> state) {
        return state;
    }

    @Override
    public Collection<T> getOriginalTransitions(Set<T> transition) {
        return transition;
    }

}
