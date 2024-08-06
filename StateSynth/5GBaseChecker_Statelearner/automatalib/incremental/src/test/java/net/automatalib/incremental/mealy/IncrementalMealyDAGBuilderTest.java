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
package net.automatalib.incremental.mealy;

import java.io.IOException;
import java.util.List;

import net.automatalib.commons.util.Pair;
import net.automatalib.incremental.IntegrationUtil;
import net.automatalib.incremental.IntegrationUtil.ParsedTraces;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IncrementalMealyDAGBuilderTest extends AbstractIncrementalMealyBuilderTest {

    @Override
    protected <I, O> IncrementalMealyBuilder<I, O> createIncrementalMealyBuilder(Alphabet<I> alphabet) {
        return new IncrementalMealyDAGBuilder<>(alphabet);
    }

    /**
     * This tests case validates a set of traces from an external system which exposed an issue in confluence
     * propagation.
     */
    @Test
    public void testIntegration() throws IOException {
        validateTraces("/spa/mealy_traces.gz");
    }

    /**
     * Test case based on <a href="https://github.com/LearnLib/learnlib/issues/76">LearnLib issue #76</a>.
     */
    @Test
    public void testLearnLib76() throws IOException {
        validateTraces("/learnlib76/mealy.gz");
    }

    private void validateTraces(String pathToTraces) throws IOException {
        final ParsedTraces<Integer, Word<Integer>> parsedData = IntegrationUtil.parseMealyTraces(pathToTraces);
        final Alphabet<Integer> alphabet = parsedData.alphabet;
        final List<Pair<Word<Integer>, Word<Integer>>> traces = parsedData.traces;

        final IncrementalMealyBuilder<Integer, Integer> cache = createIncrementalMealyBuilder(alphabet);

        // test insertion without errors
        for (Pair<Word<Integer>, Word<Integer>> trace : traces) {
            final Word<Integer> input = trace.getFirst();
            final Word<Integer> value = trace.getSecond();

            cache.insert(input, value);
            // test direct caching behavior
            Assert.assertEquals(value, cache.lookup(input));
        }

        // test global caching behavior
        for (Pair<Word<Integer>, Word<Integer>> trace : traces) {
            final Word<Integer> input = trace.getFirst();
            final Word<Integer> value = trace.getSecond();

            Assert.assertEquals(value, cache.lookup(input));
        }
    }
}
