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

import java.util.Collection;
import java.util.Comparator;

/**
 * A generalized priority queue which allows storing arbitrary elements that don't have to be comparable, neither by
 * their natural ordering nor by a provided {@link Comparator}. Instead, keys can be assigned to the elements
 * explicitly.
 * <p>
 * Since this interface extends the {@link SmartCollection} (and thus also the {@link Collection}) interface, it has to
 * provide the {@link SmartCollection#referencedAdd(Object)} and {@link Collection#add(Object)} methods with no
 * additional key parameters. This is handled by using a <i>default key</i>, which is implicitly used for all elements
 * inserted using the above methods. Initially, the default key is <code>null</code>, whereas the <code>null</code> key
 * is by convention larger than any non-<code>null</code> key. The default key for consequent insertions can be changed
 * by calling {@link #setDefaultKey(Comparable)}.
 *
 * @param <E>
 *         element class.
 * @param <K>
 *         key class.
 *
 * @author Malte Isberner
 */
public interface SmartGeneralPriorityQueue<E, K extends Comparable<K>> extends SmartPriorityQueue<E> {

    /**
     * Inserts an element with the specified key.
     *
     * @param elem
     *         the element to insert.
     * @param key
     *         the key for this element.
     *
     * @return the reference to the inserted element.
     */
    ElementReference add(E elem, K key);

    /**
     * Sets the default key, which is used for elements that are inserted with no explicit key specified.
     *
     * @param defaultKey
     *         the new default key.
     */
    void setDefaultKey(K defaultKey);

    /**
     * Changes the key of an element in the priority key.
     *
     * @param ref
     *         reference to the element whose key is to be changed.
     * @param newKey
     *         the new key of this element.
     */
    void changeKey(ElementReference ref, K newKey);
}
