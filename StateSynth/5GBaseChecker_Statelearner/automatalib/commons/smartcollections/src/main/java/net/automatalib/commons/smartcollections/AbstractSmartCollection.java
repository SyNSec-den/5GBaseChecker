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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import com.google.common.collect.Iterators;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class eases the implementation of the {@link SmartCollection} interface. It is comparable to {@link
 * AbstractCollection} from the Java Collections Framework.
 * <p>
 * A class extending this abstract class has to implement the following methods: - {@link Collection#size()} - {@link
 * SmartCollection#get(ElementReference)} - {@link SmartCollection#referenceIterator()} - {@link
 * SmartCollection#referencedAdd(Object)} - {@link SmartCollection#remove(ElementReference)} - {@link
 * SmartCollection#replace(ElementReference, Object)}
 *
 * @param <E>
 *         element class.
 *
 * @author Malte Isberner
 */
public abstract class AbstractSmartCollection<E> extends AbstractCollection<E> implements SmartCollection<E> {

    @Override
    public E choose() {
        return iterator().next();
    }

    @Override
    public ElementReference chooseRef() {
        return referenceIterator().next();
    }

    @Override
    public Iterable<ElementReference> references() {
        return this::referenceIterator;
    }

    @Override
    public void addAll(Iterable<? extends E> iterable) {
        for (E e : iterable) {
            add(e);
        }
    }

    @Override
    public <T extends E> void addAll(T[] array) {
        for (T t : array) {
            add(t);
        }
    }

    @Override
    public @Nullable ElementReference find(@Nullable Object element) {
        for (ElementReference ref : references()) {
            E current = get(ref);
            if (Objects.equals(current, element)) {
                return ref;
            }
        }

        return null;
    }

    @Override
    public void quickClear() {
        clear();
    }

    @Override
    public void deepClear() {
        clear();
    }

    @Override
    public Iterator<E> iterator() {
        return Iterators.transform(referenceIterator(), this::get);
    }

    @Override
    public boolean add(E e) {
        referencedAdd(e);
        return true;
    }

    @Override
    public boolean remove(@Nullable Object element) {
        ElementReference ref = find(element);
        if (ref == null) {
            return false;
        }
        remove(ref);
        return true;
    }

}
