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
package net.automatalib.commons.smartcollections;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A thin wrapper around a simple {@code Object[]} array. Mainly used (and useful) for heavily generic and array-based
 * data storage. Extends/Implements some convenient classes/interfaces.
 *
 * @param <T>
 *         the type of stored elements
 *
 * @author frohme
 */
public final class ArrayStorage<T> extends AbstractList<T> implements RandomAccess, Cloneable {

    private final @Nullable Object[] storage;

    public ArrayStorage(int size) {
        this.storage = new Object[size];
    }

    public ArrayStorage(int size, Supplier<T> supplier) {
        this.storage = new Object[size];
        for (int i = 0; i < size; i++) {
            storage[i] = supplier.get();
        }
    }

    public ArrayStorage(Collection<? extends T> collection) {
        storage = collection.toArray();
    }

    private ArrayStorage(@Nullable Object[] storage) {
        this.storage = storage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) storage[index];
    }

    @Override
    public T set(int index, T element) {
        final T oldValue = get(index);
        storage[index] = element;
        return oldValue;
    }

    @Override
    public int size() {
        return storage.length;
    }

    @SuppressWarnings("PMD.ProperCloneImplementation") //we want to cut cloning hierarchy here
    @Override
    public ArrayStorage<T> clone() {
        return new ArrayStorage<>(storage.clone());
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayStorage)) {
            return false;
        }

        final ArrayStorage<?> that = (ArrayStorage<?>) o;
        return Arrays.equals(storage, that.storage);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(storage);
    }
}
