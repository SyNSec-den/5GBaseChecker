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
package net.automatalib.words;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A {@link Collector} that collects individual symbols and aggregates them in a {@link Word}. Convenience class to use
 * a {@link WordBuilder} in stream-reduction operations.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
final class WordCollector<I> implements Collector<I, WordBuilder<I>, Word<I>> {

    @Override
    public Supplier<WordBuilder<I>> supplier() {
        return WordBuilder::new;
    }

    @Override
    public BiConsumer<WordBuilder<I>, I> accumulator() {
        return WordBuilder::append;
    }

    @Override
    public BinaryOperator<WordBuilder<I>> combiner() {
        return WordBuilder::append;
    }

    @Override
    public Function<WordBuilder<I>, Word<I>> finisher() {
        return WordBuilder::toWord;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
