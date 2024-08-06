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

import java.util.AbstractList;
import java.util.RandomAccess;

import net.automatalib.commons.smartcollections.ArrayWritable;
import org.checkerframework.checker.nullness.qual.Nullable;

final class IntRange extends AbstractList<Integer> implements ArrayWritable<Integer>, RandomAccess {

    private final int start;
    private final int step;
    private final int size;

    IntRange(int start, int end) {
        this(start, end, 1);
    }

    IntRange(int start, int end, int step) {
        this.start = start;
        this.step = step;
        this.size = (end - start - 1) / step + 1;
    }

    @Override
    public Integer get(int index) {
        return intGet(index);
    }

    public int intGet(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return intValue(index);
    }

    public int intValue(int i) {
        return start + step * i;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        if (o == null || o.getClass() != Integer.class) {
            return -1;
        }
        int i = (Integer) o;
        return indexOf(i);
    }

    public int indexOf(int i) {
        if (i < start) {
            return -1;
        }
        final int aligned = i - start;
        if (aligned % step != 0) {
            return -1;
        }
        final int normalized = aligned / step;
        if (normalized >= size) {
            return -1;
        }
        return normalized;
    }


    @Override
    public int lastIndexOf(@Nullable Object o) {
        return indexOf(o);
    }

    @Override
    public IntRangeIterator iterator() {
        return new IntRangeIterator(start, step, size);
    }

    @Override
    public IntRangeIterator listIterator() {
        return new IntRangeIterator(start, step, size);
    }

    @Override
    public IntRangeIterator listIterator(int index) {
        return new IntRangeIterator(start, step, size, index);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void writeToArray(int offset, @Nullable Object[] array, int tgtOfs, int num) {
        int x = start + offset * step;
        int ti = tgtOfs;
        for (int i = 0; i < num; i++) {
            array[ti++] = x;
            x += step;
        }
    }

}
