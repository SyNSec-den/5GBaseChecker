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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A word consisting of a single letter only.
 *
 * @param <I>
 *         symbol class
 *
 * @author Malte Isberner
 * @see Collections#singletonList(Object)
 */
final class LetterWord<I> extends Word<I> {

    private final I letter;

    /**
     * Constructor.
     *
     * @param letter
     *         the letter to represent as a word
     */
    LetterWord(I letter) {
        this.letter = letter;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public java.util.Iterator<I> iterator() {
        return new Iterator<>(letter);
    }

    @Override
    public Spliterator<I> spliterator() {
        return Collections.singleton(letter).spliterator();
    }

    @Override
    public Word<I> subWordInternal(int fromIndex, int toIndex) {
        if (fromIndex > 0 || toIndex == 0) {
            return Word.epsilon();
        }
        return this;
    }

    @Override
    public void writeToArray(int offset, @Nullable Object[] array, int tgtOffset, int length) {
        if (offset == 0 && length > 0) {
            array[tgtOffset] = letter;
        }
    }

    @Override
    public I getSymbol(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }
        return letter;
    }

    @Override
    public List<I> asList() {
        return Collections.singletonList(letter);
    }

    @Override
    public I lastSymbol() {
        return letter;
    }

    @Override
    public Word<I> append(I symbol) {
        @Nullable Object[] array = {letter, symbol};
        return new SharedWord<>(array);
    }

    @Override
    public Word<I> prepend(I symbol) {
        @Nullable Object[] array = {symbol, letter};
        return new SharedWord<>(array);
    }

    @Override
    public boolean isPrefixOf(Word<?> other) {
        return !other.isEmpty() && Objects.equals(letter, other.getSymbol(0));
    }

    @Override
    public Word<I> longestCommonPrefix(Word<?> other) {
        if (isPrefixOf(other)) {
            return this;
        }
        return Word.epsilon();
    }

    @Override
    public boolean isSuffixOf(Word<?> other) {
        return !other.isEmpty() && Objects.equals(letter, other.lastSymbol());
    }

    @Override
    public Word<I> longestCommonSuffix(Word<?> other) {
        if (isSuffixOf(other)) {
            return this;
        }
        return Word.epsilon();
    }

    @Override
    public Word<I> flatten() {
        return this;
    }

    @Override
    public Word<I> trimmed() {
        return this;
    }

    @Override
    public <T> Word<T> transform(Function<? super I, ? extends T> transformer) {
        T transformed = transformer.apply(letter);
        return new LetterWord<>(transformed);
    }

    /*
     * Iterator
     */
    private static final class Iterator<I> implements java.util.Iterator<I> {

        private final I letter;
        private boolean next = true;

        Iterator(I letter) {
            this.letter = letter;
        }

        @Override
        public boolean hasNext() {
            return next;
        }

        @Override
        public I next() {
            if (next) {
                next = false;
                return letter;
            }
            throw new NoSuchElementException();
        }

    }

}
