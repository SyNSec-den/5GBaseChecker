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
package net.automatalib.incremental.dfa;

import java.io.IOException;
import java.util.List;

import net.automatalib.commons.util.Pair;
import net.automatalib.incremental.IntegrationUtil;
import net.automatalib.incremental.IntegrationUtil.ParsedTraces;
import net.automatalib.incremental.dfa.dag.IncrementalDFADAGBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IncrementalDFADAGBuilderTest extends AbstractIncrementalDFABuilderTest {

    @Override
    protected <I> IncrementalDFABuilder<I> createIncrementalDFABuilder(Alphabet<I> alphabet) {
        return new IncrementalDFADAGBuilder<>(alphabet);
    }

    /**
     * This tests case validates a set of traces from an external system which exposed an issue in confluence
     * propagation.
     */
    @Test
    public void testIntegration() throws IOException {
        validateTraces("/spa/dfa_traces.gz");
    }

    /**
     * Test case based on <a href="https://github.com/LearnLib/learnlib/issues/76">LearnLib issue #76</a>.
     */
    @Test
    public void testLearnLib76() throws IOException {
        validateTraces("/learnlib76/dfa.gz");
    }

    private void validateTraces(String pathToTraces) throws IOException {
        final ParsedTraces<Integer, Boolean> parsedData = IntegrationUtil.parseDFATraces(pathToTraces);
        final Alphabet<Integer> alphabet = parsedData.alphabet;
        final List<Pair<Word<Integer>, Boolean>> traces = parsedData.traces;

        final IncrementalDFABuilder<Integer> cache = createIncrementalDFABuilder(alphabet);

        // test insertion without errors
        for (Pair<Word<Integer>, Boolean> trace : traces) {
            final Word<Integer> input = trace.getFirst();
            final boolean value = trace.getSecond();

            cache.insert(input, value);
            // test direct caching behavior
            Assert.assertEquals(value, cache.lookup(input).toBoolean());
        }

        // test global caching behavior
        for (Pair<Word<Integer>, Boolean> trace : traces) {
            final Word<Integer> input = trace.getFirst();
            final boolean value = trace.getSecond();

            Assert.assertEquals(value, cache.lookup(input).toBoolean());
        }
    }

}
