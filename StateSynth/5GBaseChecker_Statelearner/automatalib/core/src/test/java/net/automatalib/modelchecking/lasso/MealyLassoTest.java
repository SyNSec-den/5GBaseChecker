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
package net.automatalib.modelchecking.lasso;

import java.util.Collection;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.MealyTransition;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public class MealyLassoTest extends AbstractLassoTest<MealyLassoImpl<String, String>> {

    @Override
    protected MealyLassoImpl<String, String> getLasso(Word<String> prefix, Word<String> loop, int unfoldTimes) {
        return new MealyLassoImpl<>(new MealyMachineMock(prefix, loop), getAlphabet(), 1);
    }

    @Test
    public void testGetOutput() {
        final MealyLassoImpl<String, String> lasso = getLasso(Word.epsilon(), Word.fromSymbols("a"), 1);
        Assert.assertEquals(lasso.getOutput(), Word.fromSymbols(MealyMachineMock.OUTPUT));
    }

    private static class MealyMachineMock
            implements MealyMachine<Integer, String, MealyTransition<Integer, String>, String> {

        public static final String OUTPUT = "test";

        private final Word<String> prefix;
        private final Word<String> word;

        MealyMachineMock(Word<String> prefix, Word<String> loop) {
            this.prefix = prefix;
            word = prefix.concat(loop);
        }

        @Override
        public String getTransitionOutput(MealyTransition<Integer, String> transition) {
            return OUTPUT;
        }

        @Override
        public Collection<Integer> getStates() {
            return CollectionsUtil.intRange(0, word.length());
        }

        @Override
        public @Nullable MealyTransition<Integer, String> getTransition(Integer state, String input) {
            final MealyTransition<Integer, String> result;
            if (word.getSymbol(state).equals(input)) {
                if (state < word.length() - 1) {
                    result = new MealyTransition<>(state + 1, OUTPUT);
                } else {
                    result = new MealyTransition<>(prefix.length(), OUTPUT);
                }
            } else {
                result = null;
            }

            return result;
        }

        @Override
        public Integer getSuccessor(MealyTransition<Integer, String> transition) {
            return transition.getSuccessor();
        }

        @Override
        public Integer getInitialState() {
            return 0;
        }
    }
}
