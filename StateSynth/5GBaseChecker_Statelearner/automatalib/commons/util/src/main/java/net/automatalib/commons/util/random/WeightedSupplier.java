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
package net.automatalib.commons.util.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class implements a {@link Supplier} that randomly delegates to one of several (sub-)suppliers. Each sub-supplier
 * is assigned a weight, which determines the probability of it being chosen upon calls to {@link #get()}.
 * <p>
 * The {@link #add(Object, int)} and {@link #add(Supplier, int)} methods return a reference to {@code this}, so calls
 * can be chained.
 * <p>
 * <b>Usage example:</b>
 * <pre>
 * {@code
 * Supplier<String> mySupplier = ...;
 * String str = new WeightedSupplier<String>()
 *  .add("foo", 5)
 *  .add(mySupplier, 10)
 *  .get();
 * }
 * </pre>
 * With a one-third chance, the value {@code "foo"} will be assigned to {@code str}. Otherwise (i.e., with a two-thirds
 * chance), the result of {@code mySupplier.get()} will be assigned to {@code str}. Note that in the former case, {@code
 * mySupplier.get()} will not even be invoked.
 *
 * @param <T>
 *         the supplied type
 *
 * @author Malte Isberner
 */
public class WeightedSupplier<T> implements Supplier<T>, Function<Random, T> {

    private static final Random RANDOM = new Random();
    private final List<SubSupplier<T>> subSuppliers = new ArrayList<>();
    private int totalWeight;

    /**
     * Adds an object to be supplied with a given weight.
     *
     * @param obj
     *         the object to be supplied
     * @param weight
     *         the weight
     *
     * @return {@code this}
     */
    public WeightedSupplier<T> add(T obj, int weight) {
        return add(() -> obj, weight);
    }

    /**
     * Adds a sub-supplier with a given weight.
     *
     * @param supplier
     *         the sub-supplier
     * @param weight
     *         the weight
     *
     * @return {@code this}
     */
    public WeightedSupplier<T> add(Supplier<? extends T> supplier, int weight) {
        if (weight < 0) {
            return this;
        }
        int low = totalWeight;
        totalWeight += weight;
        SubSupplier<T> ss = new SubSupplier<>(low, totalWeight, supplier);
        subSuppliers.add(ss);
        return this;
    }

    public Supplier<T> forRandom(Random r) {
        return () -> apply(r);
    }

    @Override
    public T apply(Random r) {
        int val = r.nextInt(totalWeight);
        int l = 0, h = subSuppliers.size();
        while (l < h) {
            int mid = l + (h - l) / 2;
            SubSupplier<T> ss = subSuppliers.get(mid);
            if (ss.lowIdx <= val) {
                if (ss.highIdx > val) {
                    return ss.get();
                }
                l = mid + 1;
            } else {
                h = mid;
            }
        }
        throw new AssertionError();
    }

    @Override
    public T get() {
        return apply(RANDOM);
    }

    private static final class SubSupplier<T> implements Supplier<T> {

        private final int lowIdx;
        private final int highIdx;
        private final Supplier<? extends T> supplier;

        SubSupplier(int lowIdx, int highIdx, Supplier<? extends T> supplier) {
            this.lowIdx = lowIdx;
            this.highIdx = highIdx;
            this.supplier = supplier;
        }

        @Override
        public T get() {
            return supplier.get();
        }
    }

}
