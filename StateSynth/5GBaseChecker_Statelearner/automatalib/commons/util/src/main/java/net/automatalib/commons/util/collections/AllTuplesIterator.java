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
package net.automatalib.commons.util.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.checkerframework.checker.nullness.qual.Nullable;

final class AllTuplesIterator<T> implements Iterator<List<T>> {

    private final Iterable<? extends T> iterable;
    private final List<T> current;
    private final @Nullable Iterator<? extends T>[] iterators;
    private boolean firstEmpty;

    @SuppressWarnings("unchecked")
    AllTuplesIterator(Iterable<? extends T> iterable, int minLength, int maxLength) {
        if (maxLength < minLength || minLength < 0) {
            throw new IllegalArgumentException();
        }

        this.current = new ArrayList<>(maxLength);
        this.iterators = new Iterator[maxLength];
        this.iterable = iterable;

        for (int i = 1; i < minLength; i++) {
            Iterator<? extends T> it = iterable.iterator();
            iterators[i] = it;
            current.add(it.next());
        }

        firstEmpty = minLength == 0;
    }

    @Override
    public boolean hasNext() {
        if (firstEmpty) {
            return true;
        }

        for (Iterator<? extends T> it : iterators) {
            if (it == null || it.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<T> next() {
        if (firstEmpty) {
            firstEmpty = false;
            return current;
        }

        for (int i = 0; i < iterators.length; i++) {
            Iterator<? extends T> it = iterators[i];
            if (it == null) {
                it = iterable.iterator();
                iterators[i] = it;
                current.add(it.next());
                return current;
            }

            if (it.hasNext()) {
                current.set(i, it.next());
                return current;
            }

            it = iterable.iterator();
            iterators[i] = it;
            current.set(i, it.next());
        }

        throw new NoSuchElementException();
    }

}
