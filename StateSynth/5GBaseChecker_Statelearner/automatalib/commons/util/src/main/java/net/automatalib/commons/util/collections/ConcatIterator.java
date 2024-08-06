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
import java.util.NoSuchElementException;

public class ConcatIterator<T> implements Iterator<T> {

    private final Iterator<? extends T>[] iterators;
    private int currentIndex;

    @SafeVarargs
    public ConcatIterator(Iterator<? extends T>... iterators) {
        int numIts = iterators.length;
        int i = 0;
        while (i < numIts) {
            Iterator<? extends T> it = iterators[i];
            if (it.hasNext()) {
                break;
            }
            i++;
        }
        if (i == numIts) {
            this.iterators = null;
            this.currentIndex = -1;
        } else {
            this.iterators = iterators;
            this.currentIndex = i;
        }
    }

    @Override
    public boolean hasNext() {
        return iterators != null && currentIndex < iterators.length;
    }

    @Override
    public T next() {
        if (iterators == null || currentIndex >= iterators.length) {
            throw new NoSuchElementException();
        }
        Iterator<? extends T> curr = iterators[currentIndex];
        T nxt = curr.next();
        if (!curr.hasNext()) {
            while (++currentIndex < iterators.length && !iterators[currentIndex].hasNext()) {
            }
        }
        return nxt;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
