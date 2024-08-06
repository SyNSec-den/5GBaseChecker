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
package net.automatalib.automata.transducers.impl;

import net.automatalib.automata.base.fast.AbstractFastMutableDet;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.words.Alphabet;

/**
 * A fast implementation of a Mealy machine.
 *
 * @param <I>
 *         input symbol class.
 * @param <O>
 *         output symbol class.
 *
 * @author Malte Isberner
 */
public class FastMealy<I, O>
        extends AbstractFastMutableDet<FastMealyState<O>, I, MealyTransition<FastMealyState<O>, O>, Void, O> implements
                                                                                                             MutableMealyMachine<FastMealyState<O>, I, MealyTransition<FastMealyState<O>, O>, O>,
                                                                                                             StateLocalInputMealyMachine<FastMealyState<O>, I, MealyTransition<FastMealyState<O>, O>, O> {

    /**
     * Constructor. Initializes a new (empty) Mealy machine with the given input alphabet.
     *
     * @param alphabet
     *         the input alphabet.
     */
    public FastMealy(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public FastMealyState<O> getSuccessor(MealyTransition<FastMealyState<O>, O> transition) {
        return transition.getSuccessor();
    }

    @Override
    public O getTransitionOutput(MealyTransition<FastMealyState<O>, O> transition) {
        return transition.getOutput();
    }

    @Override
    public MealyTransition<FastMealyState<O>, O> createTransition(FastMealyState<O> successor, O properties) {
        return new MealyTransition<>(successor, properties);
    }

    @Override
    public void setTransitionOutput(MealyTransition<FastMealyState<O>, O> transition, O output) {
        transition.setOutput(output);
    }

    @Override
    protected FastMealyState<O> createState(Void property) {
        return new FastMealyState<>(inputAlphabet.size());
    }
}
