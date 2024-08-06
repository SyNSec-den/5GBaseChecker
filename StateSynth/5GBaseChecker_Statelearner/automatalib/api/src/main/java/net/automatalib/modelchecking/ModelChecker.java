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
package net.automatalib.modelchecking;

import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.exception.ModelCheckingException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A model checker checks whether a given automaton satisfies a given property. If the property can not be satisfied it
 * provides counter examples. In fact, the counter examples is an automaton which language is a subset of the language
 * of the given automaton.
 *
 * @param <I>
 *         the input type
 * @param <A>
 *         the automaton type
 * @param <P>
 *         the property type
 * @param <R>
 *         the type of a counterexample
 *
 * @author Jeroen Meijer
 */
public interface ModelChecker<I, A, P, R> {

    /**
     * Try to find counter examples for the given {@code property} and {@code automaton}.
     *
     * @param automaton
     *         the automaton to check the property on.
     * @param inputs
     *         the alphabet.
     * @param property
     *         the property.
     *
     * @return the counter examples, or {@code null} if no counter examples exist.
     *
     * @throws ModelCheckingException
     *         when this model checker can not check the property.
     */
    @Nullable R findCounterExample(A automaton, Collection<? extends I> inputs, P property);

    interface DFAModelChecker<I, P, R> extends ModelChecker<I, DFA<?, I>, P, R> {}

    /**
     * A model checker for Mealy machines. Key about the {@link MealyMachine} type here is that it may not be
     * input-complete. Implementations of {@link MealyMachine}s should in these cases not return any output for a given
     * input sequence. I.e. {@link MealyMachine#computeOutput(Iterable)} should return null when its argument is not
     * accepted.
     *
     * @see ModelChecker
     */
    interface MealyModelChecker<I, O, P, R> extends ModelChecker<I, MealyMachine<?, I, ?, O>, P, R> {

        /**
         * Returns the outputs for which all transitions should be removed.
         * <p>
         * That is, before the model checker tries to find a counter example to the automaton every transition
         * which output symbol is in the returned collection is removed.
         *
         * @return the outputs.
         */
        Collection<? super O> getSkipOutputs();

        /**
         * Sets the outputs which should be skipped.
         *
         * @param skipOutputs
         *         the outputs.
         *
         * @see #getSkipOutputs()
         */
        void setSkipOutputs(Collection<? super O> skipOutputs);
    }
}
