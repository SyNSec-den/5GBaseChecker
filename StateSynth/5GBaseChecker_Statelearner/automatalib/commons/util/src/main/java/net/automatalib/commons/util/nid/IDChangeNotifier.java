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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.automatalib.commons.util.ref.Ref;
import net.automatalib.commons.util.ref.StrongRef;
import net.automatalib.commons.util.ref.WeakRef;

public class IDChangeNotifier<T extends NumericID> {

    private final List<Ref<IDChangeListener<T>>> listeners = new LinkedList<>();

    public void addListener(IDChangeListener<T> listener, boolean weak) {
        Ref<IDChangeListener<T>> ref;
        if (weak) {
            ref = new WeakRef<>(listener);
        } else {
            ref = new StrongRef<>(listener);
        }

        listeners.add(ref);
    }

    public void removeListener(IDChangeListener<T> listener) {
        final Iterator<? extends Ref<?>> it = listeners.iterator();

        while (it.hasNext()) {
            Object referent = it.next().get();
            if (referent == null) {
                it.remove();
            } else if (referent.equals(listener)) {
                it.remove();
            }
        }
    }

    public void notifyListeners(T obj, int newId, int oldId) {
        Iterator<Ref<IDChangeListener<T>>> it = listeners.iterator();

        while (it.hasNext()) {
            IDChangeListener<T> listener = it.next().get();
            if (listener == null) {
                it.remove();
            } else {
                listener.idChanged(obj, newId, oldId);
            }
        }
    }
}
