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

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * A priority queue interface.
 * <p>
 * A priority queue is a queue which supports removal of the element with the minimal key value (wrt. natural ordering
 * or an explicitly specified {@link Comparator}).
 * <p>
 * This interface extends the functionality of the standard {@link PriorityQueue} in the way that it allows dynamic
 * behavior: The ordering of the elements in the queue is allowed to change. The only restriction is that whenever the
 * key which is used for comparison changes, the method {@link #keyChanged(ElementReference)} has to be called with the
 * reference of the respective element.
 *
 * @param <E>
 *         element class.
 *
 * @author Malte Isberner
 */
public interface SmartDynamicPriorityQueue<E> extends SmartPriorityQueue<E> {

    /**
     * Notifies the implementation that the key of an element has changed.
     *
     * @param reference
     *         the reference for the element whose key has changed.
     */
    void keyChanged(ElementReference reference);
}
