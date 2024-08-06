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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The empty word.
 * <p>
 * This class has no type parameter, as there are no non-{@code null} instances of the symbol class involved. Hence,
 * Java's generic mechanism allows to maintain only a single instance of this class.
 *
 * @author Malte Isberner
 * @see Collections#emptyList()
 */
final class EmptyWord extends Word<Object> {

    public static final EmptyWord INSTANCE = new EmptyWord();

    @Override
    public int length() {
        return 0;
    }

    @Override
    public Spliterator<Object> spliterator() {
        return Spliterators.emptySpliterator();
    }

    @Override
    public Word<Object> subWordInternal(int fromIndex, int toIndex) {
        return this;
    }

    @Override
    public void writeToArray(int offset, @Nullable Object[] array, int tgtOffset, int length) {}

    @Override
    public Object getSymbol(int index) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
    }

    @Override
    public List<Object> asList() {
        return Collections.emptyList();
    }

    @Override
    public Word<Object> canonicalNext(Alphabet<Object> sigma) {
        return new LetterWord<>(sigma.getSymbol(0));
    }

    @Override
    public Object lastSymbol() {
        throw new NoSuchElementException();
    }

    @Override
    public Word<Object> append(Object symbol) {
        return new LetterWord<>(symbol);
    }

    @Override
    public Word<Object> prepend(Object symbol) {
        return append(symbol);
    }

    @Override
    public boolean isPrefixOf(Word<?> other) {
        return true;
    }

    @Override
    public Word<Object> longestCommonPrefix(Word<?> other) {
        return this;
    }

    @Override
    public boolean isSuffixOf(Word<?> other) {
        return true;
    }

    @Override
    public Word<Object> longestCommonSuffix(Word<?> other) {
        return this;
    }

    @Override
    public Word<Object> flatten() {
        return this;
    }

    @Override
    public Word<Object> trimmed() {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Word<T> transform(Function<? super Object, ? extends T> transformer) {
        return (Word<T>) this;
    }
}
