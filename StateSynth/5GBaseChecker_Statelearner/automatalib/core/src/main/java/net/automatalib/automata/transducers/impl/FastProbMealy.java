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

import net.automatalib.automata.base.fast.AbstractFastMutableNondet;
import net.automatalib.automata.transducers.probabilistic.MutableProbabilisticMealy;
import net.automatalib.automata.transducers.probabilistic.ProbabilisticOutput;
import net.automatalib.words.Alphabet;

public class FastProbMealy<I, O>
        extends AbstractFastMutableNondet<FastProbMealyState<O>, I, MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>>, Void, ProbabilisticOutput<O>>
        implements MutableProbabilisticMealy<FastProbMealyState<O>, I, MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>>, O> {

    public FastProbMealy(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public FastProbMealyState<O> getSuccessor(MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>> transition) {
        return transition.getSuccessor();
    }

    @Override
    public O getTransitionOutput(MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>> transition) {
        return transition.getOutput().getOutput();
    }

    @Override
    public Void getStateProperty(FastProbMealyState<O> state) {
        return null;
    }

    @Override
    public ProbabilisticOutput<O> getTransitionProperty(MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>> transition) {
        return transition.getOutput();
    }

    @Override
    public void setTransitionOutput(MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>> transition,
                                    O output) {
        transition.setOutput(new ProbabilisticOutput<>(transition.getOutput().getProbability(), output));
    }

    @Override
    public void setTransitionProbability(MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>> transition,
                                         float probability) {
        transition.setOutput(new ProbabilisticOutput<>(probability, transition.getOutput().getOutput()));
    }

    @Override
    public float getTransitionProbability(MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>> transition) {
        return transition.getOutput().getProbability();
    }

    @Override
    public void setStateProperty(FastProbMealyState<O> state, Void property) {}

    @Override
    public void setTransitionProperty(MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>> transition,
                                      ProbabilisticOutput<O> property) {
        transition.setOutput(property);
    }

    @Override
    public MealyTransition<FastProbMealyState<O>, ProbabilisticOutput<O>> createTransition(FastProbMealyState<O> successor,
                                                                                           ProbabilisticOutput<O> properties) {
        return new MealyTransition<>(successor, properties);
    }

    @Override
    protected FastProbMealyState<O> createState(Void property) {
        return new FastProbMealyState<>(inputAlphabet.size());
    }

}
