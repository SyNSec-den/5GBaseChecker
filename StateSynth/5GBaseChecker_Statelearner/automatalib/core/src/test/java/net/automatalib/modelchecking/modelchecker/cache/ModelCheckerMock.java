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
package net.automatalib.modelchecking.modelchecker.cache;

import java.util.Collection;
import java.util.Collections;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.modelchecking.ModelCheckerLasso;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;

/**
 * @author frohme
 */
class ModelCheckerMock<I, A, P, R extends Lasso<I, ?>> implements ModelCheckerLasso<I, A, P, R>, ModelCheckCounter {

    static final double DEFAULT_MULTIPLIER = Math.PI;
    static final int DEFAULT_UNFOLDS = 42;

    private final A automaton;
    private final P property;
    private final R counterexample;

    private int checks;
    private double multiplier = DEFAULT_MULTIPLIER;
    private int unfolds = DEFAULT_UNFOLDS;

    ModelCheckerMock(A automaton, P property, R counterexample) {
        this.automaton = automaton;
        this.property = property;
        this.counterexample = counterexample;
    }

    @Override
    public int getChecks() {
        return checks;
    }

    @Override
    public @Nullable R findCounterExample(A automaton, Collection<? extends I> inputs, P property) {
        Assert.assertSame(automaton, this.automaton);
        Assert.assertSame(property, this.property);
        checks++;
        return this.counterexample;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public int getMinimumUnfolds() {
        return unfolds;
    }

    @Override
    public void setMinimumUnfolds(int minimumUnfolds) {
        this.unfolds = minimumUnfolds;
    }

    static class DFAModelCheckerMock<I> extends ModelCheckerMock<I, DFA<?, I>, Object, DFALasso<I>>
            implements DFAModelCheckerLasso<I, Object> {

        DFAModelCheckerMock(DFA<?, I> automaton, Object property, DFALasso<I> counterexample) {
            super(automaton, property, counterexample);
        }
    }

    static class MealyModelCheckerMock<I, O>
            extends ModelCheckerMock<I, MealyMachine<?, I, ?, O>, Object, MealyLasso<I, O>>
            implements MealyModelCheckerLasso<I, O, Object> {

        MealyModelCheckerMock(MealyMachine<?, I, ?, O> automaton, Object property, MealyLasso<I, O> counterexample) {
            super(automaton, property, counterexample);
        }

        @Override
        public Collection<? super O> getSkipOutputs() {
            return Collections.emptyList();
        }

        @Override
        public void setSkipOutputs(Collection<? super O> skipOutputs) {}
    }
}