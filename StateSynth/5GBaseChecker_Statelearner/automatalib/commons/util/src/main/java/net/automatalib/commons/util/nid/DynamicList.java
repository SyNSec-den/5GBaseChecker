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
package net.automatalib.commons.util.nid;

import java.util.AbstractList;
import java.util.Iterator;

import net.automatalib.commons.smartcollections.ArrayWritable;
import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DynamicList<T extends MutableNumericID> extends AbstractList<T> implements ArrayWritable<T> {

    private final ResizingArrayStorage<T> storage = new ResizingArrayStorage<>(MutableNumericID.class);

    private int size;

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean remove(@Nullable Object elem) {
        return remove(elem, null);
    }

    @SuppressWarnings("nullness") // setting 'null' is fine, because we also decrease the size
    public boolean remove(@Nullable Object elem, @Nullable IDChangeNotifier<T> tracker) {
        if (!(elem instanceof MutableNumericID)) {
            return false;
        }
        MutableNumericID idElem = (MutableNumericID) elem;
        int idx = idElem.getId();
        T myElem = safeGet(idx);
        if (elem != myElem) {
            return false;
        }

        T last = storage.array[--size];

        if (idx != size) {
            storage.array[idx] = last;
            last.setId(idx);
            if (tracker != null) {
                tracker.notifyListeners(last, idx, size);
            }
        }
        storage.array[size] = null;
        myElem.setId(-1);

        return true;
    }

    @SuppressWarnings("nullness") // setting 'null' is fine, because we also decrease the size
    public T remove(int index, IDChangeNotifier<T> tracker) {
        T elem = get(index);

        T last = storage.array[--size];

        if (index != size) {
            storage.array[index] = last;
            last.setId(index);
            if (tracker != null) {
                tracker.notifyListeners(last, index, size);
            }
        }
        storage.array[size] = null;
        elem.setId(-1);

        return elem;
    }

    public @Nullable T safeGet(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return storage.array[index];
    }

    @Override
    public boolean add(T elem) {
        storage.ensureCapacity(size + 1);
        storage.array[size] = elem;
        elem.setId(size);
        size++;
        return true;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index " + index);
        }
        return storage.array[index];
    }

    @SuppressWarnings("nullness") // setting 'null' is fine, because we also decrease the size
    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            storage.array[i] = null;
        }
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            int index;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                return get(index++);
            }

            @Override
            public void remove() {
                DynamicList.this.remove(--index);
            }

        };
    }

    public void swap(int a, int b) {
        if (a == b) {
            return;
        }
        if (a < 0 || a >= size) {
            throw new IndexOutOfBoundsException("Invalid index " + a);
        }
        if (b < 0 || b >= size) {
            throw new IndexOutOfBoundsException("Invalid index " + b);
        }
        T tmp = storage.array[a];
        storage.array[a] = storage.array[b];
        storage.array[b] = tmp;
        storage.array[a].setId(a);
        storage.array[b].setId(b);
    }

    @Override
    public void writeToArray(int offset, @Nullable Object[] array, int tgtOfs, int num) {
        System.arraycopy(storage.array, offset, array, tgtOfs, num);
    }

}
