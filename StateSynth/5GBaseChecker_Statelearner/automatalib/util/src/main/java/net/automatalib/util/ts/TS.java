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
package net.automatalib.util.ts;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.UniversalTransitionSystem;
import net.automatalib.util.ts.iterators.AllDefinedInputsIterator;
import net.automatalib.util.ts.iterators.AllUndefinedInputsIterator;
import net.automatalib.util.ts.iterators.DefinedInputsIterator;
import net.automatalib.util.ts.iterators.UndefinedInputsIterator;
import net.automatalib.util.ts.traversal.BFSOrderIterator;

@SuppressWarnings("PMD.UseUtilityClass") // we want to allow extending for the 'Automata' class
public class TS {

    protected TS() {}

    public static <S, I> Iterable<S> bfsOrder(TransitionSystem<S, I, ?> ts, Collection<? extends I> inputs) {
        return () -> bfsOrderIterator(ts, inputs);
    }

    public static <S, I> Iterator<S> bfsOrderIterator(TransitionSystem<S, I, ?> ts, Collection<? extends I> inputs) {
        return new BFSOrderIterator<>(ts, inputs);
    }

    public static <S, SP> Function<S, SP> stateProperties(final UniversalTransitionSystem<S, ?, ?, SP, ?> uts) {
        return uts::getStateProperty;
    }

    public static <T, TP> Function<T, TP> transitionProperties(final UniversalTransitionSystem<?, ?, T, ?, TP> uts) {
        return uts::getTransitionProperty;
    }

    public static <S, I> Iterable<I> definedInputs(final DeterministicTransitionSystem<S, I, ?> dts,
                                                   final S state,
                                                   final Iterable<? extends I> inputs) {
        return () -> definedInputsIterator(dts, state, inputs.iterator());
    }

    public static <S, I> Iterator<I> definedInputsIterator(TransitionSystem<S, I, ?> ts,
                                                           S state,
                                                           Iterator<? extends I> inputsIt) {
        return new DefinedInputsIterator<>(ts, state, inputsIt);
    }

    public static <S, I> Iterable<TransRef<S, I, ?>> allDefinedInputs(final TransitionSystem<S, I, ?> ts,
                                                                      final Iterable<? extends S> states,
                                                                      final Iterable<? extends I> inputs) {
        return () -> allDefinedInputsIterator(ts, states.iterator(), inputs);
    }

    public static <S, I> Iterator<TransRef<S, I, ?>> allDefinedInputsIterator(TransitionSystem<S, I, ?> ts,
                                                                              Iterator<? extends S> stateIt,
                                                                              Iterable<? extends I> inputs) {
        return new AllDefinedInputsIterator<>(stateIt, ts, inputs);
    }

    public static <S, I> Iterable<I> undefinedInputs(final TransitionSystem<S, I, ?> ts,
                                                     final S state,
                                                     final Iterable<? extends I> inputs) {
        return () -> undefinedInputsIterator(ts, state, inputs.iterator());
    }

    public static <S, I> Iterator<I> undefinedInputsIterator(TransitionSystem<S, I, ?> ts,
                                                             S state,
                                                             Iterator<? extends I> inputsIt) {
        return new UndefinedInputsIterator<>(ts, state, inputsIt);
    }

    public static <S, I> Iterable<TransRef<S, I, ?>> allUndefinedTransitions(final TransitionSystem<S, I, ?> ts,
                                                                             final Iterable<? extends S> states,
                                                                             final Iterable<? extends I> inputs) {
        return () -> allUndefinedTransitionsIterator(ts, states.iterator(), inputs);
    }

    public static <S, I> Iterator<TransRef<S, I, ?>> allUndefinedTransitionsIterator(TransitionSystem<S, I, ?> ts,
                                                                                     Iterator<? extends S> stateIt,
                                                                                     Iterable<? extends I> inputs) {
        return new AllUndefinedInputsIterator<>(stateIt, ts, inputs);
    }

    public static final class TransRef<S, I, T> {

        public final S state;
        public final I input;
        public final T transition;

        public TransRef(S state, I input, T transition) {
            this.state = state;
            this.input = input;
            this.transition = transition;
        }
    }

}
