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
package net.automatalib.words;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An immutable word implementation that is based on the idea of a common array storage. This allows a very efficient
 * creation of subwords (e.g., prefixes and suffixes).
 *
 * @param <I>
 *         input symbol class
 *
 * @author Malte Isberner
 */
final class SharedWord<I> extends Word<I> {

    private final I[] storage;
    private final int offset;
    private final int length;

    /**
     * Constructor.
     */
    SharedWord(@Nullable Object[] storage) {
        this(storage, 0, storage.length);
    }

    @SuppressWarnings("unchecked")
    SharedWord(@Nullable Object[] storage, int offset, int length) {
        this.storage = (I[]) storage;
        this.offset = offset;
        this.length = length;
    }

    /**
     * Constructor. Creates a {@link SharedWord} from a {@link List} of input symbols.
     *
     * @param other
     *         the list of input symbols.
     */
    @SuppressWarnings({"unchecked", "nullness"})
    SharedWord(List<? extends I> other) {
        this.storage = other.toArray((I[]) new Object[other.size()]);
        this.offset = 0;
        this.length = other.size();
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public Iterator<I> iterator() {
        return new Iterator<>(this);
    }

    @Override
    public Spliterator<I> spliterator() {
        return Arrays.spliterator(storage, offset, offset + length);
    }

    @Override
    public Word<I> subWordInternal(int fromIndex, int toIndex) {
        int newLen = toIndex - fromIndex;
        if (newLen <= 0) {
            return Word.epsilon();
        }
        int newOfs = offset + fromIndex;
        if (newOfs + newLen > storage.length) {
            return Word.epsilon();
        }
        return new SharedWord<>(storage, newOfs, newLen);
    }

    @Override
    public void writeToArray(int offset, @Nullable Object[] array, int tgtOfs, int num) {
        System.arraycopy(storage, this.offset + offset, array, tgtOfs, num);
    }

    @Override
    public I getSymbol(int index) {
        return storage[offset + index];
    }

    @Override
    public I lastSymbol() {
        return storage[offset + length - 1];
    }

    @Override
    public I firstSymbol() {
        return storage[offset];
    }

    @Override
    public Word<I> flatten() {
        return this;
    }

    @Override
    public Word<I> trimmed() {
        if (offset == 0 && length == storage.length) {
            return this;
        }
        Object[] trimmed = new Object[length];
        System.arraycopy(storage, offset, trimmed, 0, length);
        return new SharedWord<>(trimmed);
    }

    /**
     * Iterator for iterating over {@link SharedWord}s.
     *
     * @param <I>
     *         symbol class.
     *
     * @author Malte Isberner
     */
    private static final class Iterator<I> implements ListIterator<I> {

        private final I[] storage;
        private final int startIdx, endIdx;
        private int currIdx;

        Iterator(SharedWord<I> word) {
            this(word, 0);
        }

        Iterator(SharedWord<I> word, int index) {
            this.storage = word.storage;
            this.startIdx = word.offset;
            this.currIdx = word.offset + index;
            this.endIdx = word.offset + word.length;
        }

        @Override
        public boolean hasNext() {
            return currIdx < endIdx;
        }

        @Override
        public I next() {
            if (currIdx >= endIdx) {
                throw new NoSuchElementException();
            }
            return storage[currIdx++];
        }

        @Override
        public boolean hasPrevious() {
            return currIdx > startIdx;
        }

        @Override
        public I previous() {
            return storage[--currIdx];
        }

        @Override
        public int nextIndex() {
            return currIdx - startIdx;
        }

        @Override
        public int previousIndex() {
            return currIdx - startIdx - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("SharedWord does not support removal of elements");
        }

        @Override
        public void set(I e) {
            throw new UnsupportedOperationException("SharedWord does not support modification");
        }

        @Override
        public void add(I e) {
            throw new UnsupportedOperationException("SharedWord does not support modification");
        }

    }
}
