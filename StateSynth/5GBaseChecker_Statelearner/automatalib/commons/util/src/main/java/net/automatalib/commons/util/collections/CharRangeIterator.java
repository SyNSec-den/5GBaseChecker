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

public final class CharRangeIterator implements ListIterator<Character> {

    private final IntRangeIterator delegate;

    public CharRangeIterator(char low, int step, int size) {
        this(low, step, size, 0);
    }

    public CharRangeIterator(char low, int step, int size, int curr) {
        this(new IntRangeIterator(low, step, size, curr));
    }

    public CharRangeIterator(IntRangeIterator delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public Character next() {
        return nextChar();
    }

    public char nextChar() {
        return (char) delegate.nextInt();
    }

    @Override
    public boolean hasPrevious() {
        return delegate.hasPrevious();
    }

    @Override
    public Character previous() {
        return previousChar();
    }

    public char previousChar() {
        return (char) delegate.previousInt();
    }

    @Override
    public int nextIndex() {
        return delegate.nextIndex();
    }

    @Override
    public int previousIndex() {
        return delegate.previousIndex();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Character e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Character e) {
        throw new UnsupportedOperationException();
    }

}
