/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.commons.util.collections;



import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class IterableUtil {

    private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object>() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    };

    // Prevent instantiation
    private IterableUtil() {}

    @SafeVarargs
    public static <T> Iterable<T> concat(final Iterable<? extends T>... iterables) {
        return () -> {
            @SuppressWarnings("unchecked")
            Iterator<? extends T>[] iterators = new Iterator[iterables.length];
            for (int i = 0; i < iterables.length; i++) {
                iterators[i] = iterables[i].iterator();
            }
            return concat(iterators);
        };
    }

    @SafeVarargs
    public static <T> Iterator<T> concat(Iterator<? extends T>... iterators) {
        return new ConcatIterator<>(iterators);
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EMPTY_ITERATOR;
    }

    public static <T> Iterable<T> unmodifiableIterable(final Iterable<T> iterable) {
        return () -> unmodifiableIterator(iterable.iterator());
    }

    public static <T> Iterator<T> unmodifiableIterator(Iterator<T> iterator) {
        return new UnmodifiableIterator<>(iterator);
    }

    public static <T> Iterator<List<T>> allCombinationsIterator(List<? extends Iterable<? extends T>> iterables) {
        @SuppressWarnings("unchecked")
        final Iterable<? extends T>[] target = (Iterable<? extends T>[]) new Iterable[iterables.size()];
        return allCombinationsIterator(iterables.toArray(target));
    }

    @SafeVarargs
    public static <T> Iterator<List<T>> allCombinationsIterator(Iterable<? extends T>... iterables) {
        return new AllCombinationsIterator<>(iterables);
    }

    public static <T> Iterable<List<T>> allCombinations(List<? extends Iterable<? extends T>> iterables) {
        return () -> allCombinationsIterator(iterables);
    }

    @SafeVarargs
    public static <T> Iterable<List<T>> allCombinations(Iterable<? extends T>... iterables) {
        return () -> allCombinationsIterator(iterables);
    }

}
