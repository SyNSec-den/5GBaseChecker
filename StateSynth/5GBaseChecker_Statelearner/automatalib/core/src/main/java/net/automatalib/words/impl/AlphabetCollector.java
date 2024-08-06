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
package net.automatalib.words.impl;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;

/**
 * A {@link Collector} that collects individual symbols and aggregates them in a {@link Alphabet}. Convenience class to
 * use a {@link GrowingMapAlphabet} in stream-reduction operations.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
final class AlphabetCollector<I> implements Collector<I, GrowingAlphabet<I>, GrowingAlphabet<I>> {

    @Override
    public Supplier<GrowingAlphabet<I>> supplier() {
        return GrowingMapAlphabet::new;
    }

    @Override
    public BiConsumer<GrowingAlphabet<I>, I> accumulator() {
        return GrowingAlphabet::addSymbol;
    }

    @Override
    public BinaryOperator<GrowingAlphabet<I>> combiner() {
        return (a1, a2) -> {
            a1.addAll(a2);
            return a1;
        };
    }

    @Override
    public Function<GrowingAlphabet<I>, GrowingAlphabet<I>> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.singleton(Characteristics.IDENTITY_FINISH);
    }
}
