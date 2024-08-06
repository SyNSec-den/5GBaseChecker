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

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Class that provides a resizable array storage of a certain type.
 *
 * @param <T>
 *         element class.
 *
 * @author Malte Isberner
 */
public final class ResizingArrayStorage<T> implements CapacityManagement {

    /**
     * The default initial capacity of the array storage.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 10;

    public T[] array;
    private int nextCapacityHint;

    /**
     * Constructor. Creates an array storage with a default initial capacity of {@link
     * ResizingArrayStorage#DEFAULT_INITIAL_CAPACITY}.
     *
     * @param arrayClazz
     *         the class of the storage array.
     */
    public ResizingArrayStorage(Class<? super T> arrayClazz) {
        this(arrayClazz, DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructor. Creates an array with the specified initial capacity.
     *
     * @param arrayClazz
     *         the class of the storage array.
     * @param initialCapacity
     *         the initial capacity.
     */
    @SuppressWarnings("unchecked")
    public ResizingArrayStorage(Class<? super T> arrayClazz, int initialCapacity) {
        this.array = (T[]) Array.newInstance(arrayClazz, Math.max(0, initialCapacity));
    }

    /**
     * Copy-constructor which (shallowly) clones the storage of the other {@link ResizingArrayStorage}.
     *
     * @param other
     *         the other storage whose data should be (shallowly) cloned
     */
    public ResizingArrayStorage(ResizingArrayStorage<T> other) {
        this.array = other.array.clone();
        this.nextCapacityHint = other.nextCapacityHint;
    }

    @Override
    public boolean ensureCapacity(int minCapacity) {
        if (minCapacity <= array.length) {
            return false;
        }

        final int newCapacity = ArrayUtil.computeNewCapacity(array.length, minCapacity, nextCapacityHint);

        array = Arrays.copyOf(array, newCapacity);
        nextCapacityHint = 0;
        return true;
    }

    @Override
    public boolean ensureAdditionalCapacity(int additionalCapacity) {
        return ensureCapacity(array.length + additionalCapacity);
    }

    @Override
    public void hintNextCapacity(int nextCapacityHint) {
        this.nextCapacityHint = nextCapacityHint;
    }

    /**
     * Shrinks the storage to the specified maximum capacity.
     * <p>
     * If the current capacity is less or equal to the specified capacity, nothing happens.
     *
     * @param maxCapacity
     *         the maximal number of elements the storage array has to provide room for.
     *
     * @return <code>true</code> iff the storage array had to be resized, <code>false</code> otherwise.
     */
    public boolean shrink(int maxCapacity) {
        if (maxCapacity >= array.length) {
            return false;
        }

        array = Arrays.copyOf(array, maxCapacity);
        return true;
    }

    /**
     * Sets all the elements in the array to the specified value.
     *
     * @param value
     *         the value.
     */
    public void setAll(T value) {
        Arrays.fill(array, value);
    }

    public void swap(ResizingArrayStorage<T> other) {
        final Class<?> myType = array.getClass().getComponentType();
        final Class<?> otherType = other.array.getClass().getComponentType();
        if (myType != otherType) {
            throw new IllegalArgumentException(
                    "Cannot swap array storages of different array classes (" + myType + " vs. " + otherType + ")");
        }
        T[] arrayTmp = array;
        int hintTmp = nextCapacityHint;
        array = other.array;
        nextCapacityHint = other.nextCapacityHint;
        other.array = arrayTmp;
        other.nextCapacityHint = hintTmp;
    }
}
