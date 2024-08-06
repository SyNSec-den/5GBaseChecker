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
 * An intrusive version of a linked list. When using this linked list implementation, the elements themselve have to
 * store the reference to their successors and predecessors, i.e., must implement the {@link LinkedListEntry}
 * interface.
 * <p>
 * Note that this furthermore implies that each such element can only be stored in at most <i>one</i> {@link
 * IntrusiveLinkedList}.
 *
 * @param <T>
 *         element class, must implement {@link LinkedListEntry}.
 *
 * @author Malte Isberner
 */
public class IntrusiveLinkedList<T extends LinkedListEntry<T, T>> extends AbstractLinkedList<T, T> {

    @Override
    protected T makeEntry(T element) {
        return element;
    }
}
