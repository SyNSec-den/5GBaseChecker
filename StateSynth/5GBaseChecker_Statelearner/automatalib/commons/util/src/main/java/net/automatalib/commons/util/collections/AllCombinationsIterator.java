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

/**
 * An iterator that iterates over the cartesian product of its given source domains. Each intermediate combination of
 * elements is computed lazily.
 * <p>
 * <b>Note:</b> Subsequent calls to the {@link #next()} method return a reference to the same list, and only update the
 * contents of the list. If you plan to reuse intermediate results, you'll need to explicitly copy them.
 *
 * @param <T>
 *         type of elements
 */
final class AllCombinationsIterator<T> implements Iterator<List<T>> {

    private final Iterable<? extends T>[] iterables;
    private final Iterator<? extends T>[] iterators;
    private final List<T> current;
    private boolean first = true;
    private boolean empty;

    @SuppressWarnings("unchecked")
    @SafeVarargs
    AllCombinationsIterator(Iterable<? extends T>... iterables) {
        this.iterables = iterables;
        this.iterators = new Iterator[iterables.length];
        this.current = new ArrayList<>(iterables.length);
        for (int i = 0; i < iterators.length; i++) {
            Iterator<? extends T> it = iterables[i].iterator();
            if (!it.hasNext()) {
                empty = true;
                break;
            }
            this.iterators[i] = it;
            this.current.add(it.next());
        }
    }

    @Override
    public boolean hasNext() {
        if (empty) {
            return false;
        }

        for (Iterator<? extends T> it : iterators) {
            if (it.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<T> next() {
        if (empty) {
            throw new NoSuchElementException();
        } else if (first) {
            first = false;
            return current;
        }

        for (int i = 0; i < iterators.length; i++) {
            Iterator<? extends T> it = iterators[i];

            if (iterators[i].hasNext()) {
                current.set(i, it.next());
                return current;
            }

            it = iterables[i].iterator();
            iterators[i] = it;
            current.set(i, it.next());
        }

        throw new NoSuchElementException();
    }

}
