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
package net.automatalib.commons.util.mappings;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.commons.util.nid.IDChangeListener;
import net.automatalib.commons.util.nid.NumericID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ArrayMapping<K extends NumericID, @Nullable V> implements MutableMapping<K, V>, IDChangeListener<K> {

    private final ResizingArrayStorage<V> storage;

    public ArrayMapping() {
        storage = new ResizingArrayStorage<>(Object.class);
    }

    public ArrayMapping(int initialSize) {
        storage = new ResizingArrayStorage<>(Object.class, initialSize);
    }

    @Override
    public V get(K elem) {
        int id = elem.getId();
        if (id < 0 || id >= storage.array.length) {
            return null;
        }
        return storage.array[id];
    }

    @Override
    public V put(K key, V value) {
        int id = key.getId();
        storage.ensureCapacity(id + 1);
        V old = storage.array[id];
        storage.array[id] = value;
        return old;
    }

    @Override
    public void idChanged(K obj, int newId, int oldId) {
        if (newId == -1) {
            if (oldId < storage.array.length) {
                storage.array[oldId] = null;
            }
            return;
        }
        V oldVal = null;
        if (oldId < storage.array.length) {
            oldVal = storage.array[oldId];
            storage.array[oldId] = oldVal;
        }
        storage.ensureCapacity(newId + 1);
        storage.array[newId] = oldVal;
    }

}
