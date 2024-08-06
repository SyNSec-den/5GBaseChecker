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
 * Priority queue interface. Note that this class deliberately does not specify whether the inserted elements come with
 * their own key (i.e. implement {@link Comparable} or can be compared using a comparator), or have external keys
 * attached.
 *
 * @param <E>
 *         element class
 *
 * @author Malte Isberner
 */
public interface SmartPriorityQueue<E> extends SmartCollection<E> {

    /**
     * Retrieves, but does not remove the element with the minimum key in the priority queue. If there are several
     * elements with minimal key values, one of them is chosen arbitrarily.
     *
     * @return an element with a minimal key.
     */
    E peekMin();

    /**
     * Retrieves and removes the element with the minimum key in the priority queue. If there are several elements with
     * minimal key values, one of them is chosen arbitrarily.
     *
     * @return the element with the previously minimal key.
     */
    E extractMin();

}