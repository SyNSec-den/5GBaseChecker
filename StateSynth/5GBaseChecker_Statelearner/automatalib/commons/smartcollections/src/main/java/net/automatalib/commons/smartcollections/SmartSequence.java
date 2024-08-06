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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Sequence interface. A sequence is a collection where elements are stored in a specific order, and elements can be
 * inserted in between.
 *
 * @param <E>
 *         element class.
 *
 * @author Malte Isberner
 */
public interface SmartSequence<E> extends SmartCollection<E> {

    /**
     * Retrieves the reference to the preceding element, or <code>null</code> if the given reference references the
     * first element in the list.
     *
     * @param ref
     *         the reference
     *
     * @return the reference to the preceding element
     */
    @Nullable ElementReference pred(ElementReference ref);

    /**
     * Retrieves the reference to the succeeding element, or <code>null</code> if the given reference references the
     * last element in the list.
     *
     * @param ref
     *         the reference
     *
     * @return the reference to the succeeding element
     */
    @Nullable ElementReference succ(ElementReference ref);

    /**
     * Inserts the given element <i>before</i> the element referenced by the specified reference.
     *
     * @param element
     *         the element to be added.
     * @param ref
     *         reference to the element before which the new element is to be inserted.
     *
     * @return reference to the newly added element.
     */
    ElementReference insertBefore(E element, ElementReference ref);

    /**
     * Inserts the given element <i>after</i> the element referenced by the specified reference.
     *
     * @param element
     *         the element to be added.
     * @param ref
     *         reference to the element after which the new element is to be inserted.
     *
     * @return reference to the newly added element.
     */
    ElementReference insertAfter(E element, ElementReference ref);
}
