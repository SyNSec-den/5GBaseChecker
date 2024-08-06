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

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

public final class IntRangeIterator implements ListIterator<Integer>, PrimitiveIterator.OfInt {

    private final int low;
    private final int step;
    private final int size;
    private int curr;

    public IntRangeIterator(int low, int step, int size) {
        this(low, step, size, 0);
    }

    public IntRangeIterator(int low, int step, int size, int startIdx) {
        this.low = low;
        this.size = size;
        this.step = step;
        this.curr = startIdx;
    }

    private int intValue(int idx) {
        return low + step * idx;
    }

    @Override
    public boolean hasNext() {
        return curr < size;
    }

    @Override
    public int nextInt() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return intValue(curr++);
    }

    @Override
    public Integer next() {
        return PrimitiveIterator.OfInt.super.next();
    }

    @Override
    public boolean hasPrevious() {
        return curr > 0;
    }

    @Override
    public Integer previous() {
        return previousInt();
    }

    public int previousInt() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        return intValue(--curr);
    }

    @Override
    public int nextIndex() {
        return hasNext() ? curr : size;
    }

    @Override
    public int previousIndex() {
        return hasPrevious() ? curr - 1 : -1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Integer e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Integer e) {
        throw new UnsupportedOperationException();
    }

}
