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

/**
 * The default linked list entry. It provides handling of successor and predecessor entries as well as storage of the
 * actual element.
 *
 * @param <E>
 *         element class.
 *
 * @author Malte Isberner
 */
public class DefaultLinkedListEntry<E> extends AbstractBasicLinkedListEntry<E, DefaultLinkedListEntry<E>> {

    // The stored element
    private E element;

    /**
     * Constructor.
     *
     * @param element
     *         the element to be stored at this entry.
     */
    public DefaultLinkedListEntry(E element) {
        this.element = element;
    }

    @Override
    public E getElement() {
        return element;
    }

    /**
     * Sets the stored element to the specified element.
     *
     * @param element
     *         the new stored element.
     */
    public void setElement(E element) {
        this.element = element;
    }
}
