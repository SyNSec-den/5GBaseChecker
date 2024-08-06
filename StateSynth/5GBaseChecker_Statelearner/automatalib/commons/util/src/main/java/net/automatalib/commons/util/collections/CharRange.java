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

public class CharRange extends AbstractList<Character> implements ArrayWritable<Character>, RandomAccess {

    private final IntRange delegate;

    public CharRange(char low, char high) {
        this(low, high, 1);
    }

    public CharRange(char low, char high, int step) {
        this(new IntRange(low, high, step));
    }

    public CharRange(IntRange delegate) {
        this.delegate = delegate;
    }

    @Override
    public Character get(int index) {
        return charGet(index);
    }

    public char charGet(int index) {
        return (char) delegate.intGet(index);
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        if (o == null || o.getClass() != Character.class) {
            return -1;
        }
        return indexOf(((Character) o).charValue());
    }

    public int indexOf(char c) {
        return delegate.indexOf(c);
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        return indexOf(o);
    }

    @Override
    public CharRangeIterator iterator() {
        return new CharRangeIterator(delegate.iterator());
    }

    @Override
    public CharRangeIterator listIterator() {
        return new CharRangeIterator(delegate.listIterator());
    }

    @Override
    public CharRangeIterator listIterator(int index) {
        return new CharRangeIterator(delegate.listIterator(index));
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void writeToArray(int offset, @Nullable Object[] array, int tgtOfs, int num) {
        int si = offset;
        int ti = tgtOfs;
        for (int i = 0; i < num; i++) {
            array[ti++] = charGet(si++);
        }
    }

    public char charValue(int i) {
        return (char) delegate.intValue(i);
    }

}
