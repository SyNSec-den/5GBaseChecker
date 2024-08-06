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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.EnsuresQualifierIf;

/**
 * Abstract base class for linked lists.
 * <p>
 * This class implements the base functionality for dealing with linked lists of elements implementing the {@link
 * LinkedListEntry} interface. It provides the logic for the basic operations (esp. the (re-/un-)linking of elements),
 * but not how entries into the lists are created. Therefore, it can be used by both intrusive and non-intrusive linked
 * lists.
 *
 * @param <E>
 *         element type
 * @param <T>
 *         linked list entry type
 *
 * @author Malte Isberner
 * @see IntrusiveLinkedList
 * @see DefaultLinkedList
 */
public abstract class AbstractLinkedList<E, T extends LinkedListEntry<E, T>> extends AbstractSmartCollection<E>
        implements SmartSequence<E> {

    // head element (may be null if list is empty)
    private @Nullable T head;
    // last element (may be null if list is empty)
    private @Nullable T last;
    // number of elements in the list
    private int size;

    /**
     * Retrieves the first entry in the list, or <code>null</code> if the list is empty.
     *
     * @return the first entry or <code>null</code>.
     */
    protected @Nullable T getFrontEntry() {
        return head;
    }

    /**
     * Retrieves the last entry in the list, or <code>null</code> if the list is empty.
     *
     * @return the first entry or <code>null</code>.
     */
    protected @Nullable T getBackEntry() {
        return last;
    }

    /**
     * Concatenates two linked lists. All elements of the specified list (which will be empty afterwards) are added at
     * the end of this list. This operation runs in constant time.
     *
     * @param other
     *         the list to append,
     */
    public void concat(AbstractLinkedList<? extends E, ? extends T> other) {
        if (other.isEmpty()) {
            return;
        }

        if (isEmpty()) {
            head = other.head;
        } else {
            T otherHead = other.head;
            last.setNext(otherHead);
            otherHead.setPrev(last);
        }
        last = other.last;
        size += other.size;
        other.clear();
    }

    @Override
    public E choose() {
        if (head == null) {
            throw new NoSuchElementException();
        }
        return head.getElement();
    }

    @Override
    public ElementReference chooseRef() {
        if (head == null) {
            throw new NoSuchElementException();
        }
        return head;
    }

    @Override
    public Iterator<E> iterator() {
        return new ElementIterator(head);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(ElementReference ref) {
        return ((T) ref).getElement();
    }

    @Override
    public ElementReference referencedAdd(E elem) {
        T entry = makeEntry(elem);
        pushBackEntry(entry);
        return entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void remove(ElementReference elem) {
        removeEntry((T) elem);
    }

    /**
     * Removes an entry from the list.
     *
     * @param entry
     *         the entry to remove.
     */
    protected void removeEntry(T entry) {
        T prev = entry.getPrev();
        T next = entry.getNext();
        if (prev != null) {
            prev.setNext(next);
        } else {
            head = next;
        }
        if (next != null) {
            next.setPrev(prev);
        } else {
            last = prev;
        }
        size--;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<ElementReference> referenceIterator() {
        return (Iterator<ElementReference>) (Iterator<?>) new LinkedListEntryIterator(head);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void replace(ElementReference ref, E newElement) {
        T newEntry = makeEntry(newElement);
        replaceEntry((T) ref, newEntry);
    }

    /**
     * Replaces an entry in the list.
     *
     * @param oldEntry
     *         the entry to be replaced.
     * @param newEntry
     *         the replacement entry.
     */
    protected void replaceEntry(T oldEntry, T newEntry) {
        T prev = oldEntry.getPrev();
        T next = newEntry.getNext();
        if (prev != null) {
            prev.setNext(newEntry);
        } else {
            head = newEntry;
        }
        if (next != null) {
            next.setPrev(newEntry);
        } else {
            last = newEntry;
        }
    }

    /**
     * Creates (if necessary) a {@link LinkedListEntry} for the given element. For intrusive linked lists, e.g., the
     * argument itself is returned.
     *
     * @param element
     *         the element for which to retrieve an entry.
     *
     * @return the entry for the given element.
     */
    protected abstract T makeEntry(E element);

    /**
     * Adds an entry at the end of the list.
     *
     * @param e
     *         the entry to add.
     */
    protected void pushBackEntry(T e) {
        e.setNext(null);
        e.setPrev(last);
        if (last != null) {
            last.setNext(e);
        } else {
            head = e;
        }
        last = e;
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    @EnsuresQualifierIf(qualifier = NonNull.class, expression = {"this.head", "this.last"}, result = false)
    public boolean isEmpty() {
        return head == null || last == null;
    }

    @Override
    public void clear() {
        head = null;
        last = null;
        size = 0;
    }

    /**
     * Retrieves the last element in the list. If the list is empty, a {@link NoSuchElementException} will be thrown.
     *
     * @return the last element in the list.
     */
    public E getBack() {
        if (last == null) {
            throw new NoSuchElementException();
        }
        return last.getElement();
    }

    /**
     * Retrieves a reference to the last element in the list. If the list is empty, <code>null</code> is returned.
     *
     * @return a reference to the last element, or <code>null</code>.
     */
    public @Nullable ElementReference getBackReference() {
        return last;
    }

    /**
     * Retrieves the first element in the list. If the list is empty, a {@link NoSuchElementException} will be thrown
     *
     * @return the first element in the list.
     */
    public E getFront() {
        if (head == null) {
            throw new NoSuchElementException();
        }
        return head.getElement();
    }

    /**
     * Retrieves a reference to the first element in the list. If the list is empty, <code>null</code> is returned.
     *
     * @return a reference to the first element, or <code>null</code>.
     */
    public @Nullable ElementReference getFrontReference() {
        return head;
    }

    /**
     * Retrieves and removes the last element in the list. If the list is empty, a {@link NullPointerException} may be
     * thrown.
     *
     * @return the formerly last element in the list.
     */
    @SuppressWarnings("nullness") // fine according to JavaDoc
    public E popBack() {
        return popBackEntry().getElement();
    }

    /**
     * Removes and returns the last entry in the list. If the list is empty, it remains unmodified and <code>null</code>
     * is returned.
     *
     * @return the previously first entry in the list, or <code>null</code>.
     */
    @SuppressWarnings("nullness") // since intermediate method calls must not change the head reference, e is non-null
    protected @Nullable T popBackEntry() {
        if (last == null) {
            return null;
        }
        T prev = last.getPrev();
        if (prev != null) {
            prev.setNext(null);
        } else {
            head = null;
        }
        T e = last;
        last = prev;
        e.setPrev(null);
        size--;
        return e;
    }

    /**
     * Retrieves and removes the first element in the list. If the list is empty, a {@link NullPointerException} may be
     * thrown.
     *
     * @return the formerly first element in the list.
     */
    @SuppressWarnings("nullness") // fine according to JavaDoc
    public E popFront() {
        return popFrontEntry().getElement();
    }

    /**
     * Removes and returns the first entry in the list. If the list is empty, it remains unmodified and
     * <code>null</code> is returned.
     *
     * @return the previously first entry in the list, or <code>null</code>.
     */
    @SuppressWarnings("nullness") // since intermediate method calls must not change the head reference, e is non-null
    protected @Nullable T popFrontEntry() {
        if (head == null) {
            return null;
        }
        T next = head.getNext();
        if (next != null) {
            next.setPrev(null);
        } else {
            last = null;
        }
        T e = head;
        head = next;
        e.setNext(null);
        size--;
        return e;
    }

    /**
     * Adds an element at the end of the list.
     *
     * @param element
     *         the element to add.
     *
     * @return a reference to the newly added element.
     */
    public ElementReference pushBack(E element) {
        T entry = makeEntry(element);
        pushBackEntry(entry);
        return entry;
    }

    /**
     * Adds an element at the beginning of the list.
     *
     * @param element
     *         the element to add.
     *
     * @return a reference to the newly added element.
     */
    public ElementReference pushFront(E element) {
        T entry = makeEntry(element);
        pushFrontEntry(entry);
        return entry;
    }

    /**
     * Adds an entry at the beginning of the list.
     *
     * @param e
     *         the entry to add.
     */
    protected void pushFrontEntry(T e) {
        e.setPrev(null);
        e.setNext(head);
        if (head != null) {
            head.setPrev(e);
        } else {
            last = e;
        }
        head = e;
        size++;
    }

    @Override
    public @Nullable ElementReference pred(ElementReference ref) {
        return castRef(ref).getPrev();
    }

    /**
     * Helper function for casting a general {@link ElementReference} to the specific linked list entry type.
     *
     * @param ref
     *         the reference.
     *
     * @return the argument cast to the entry type.
     */
    @SuppressWarnings("unchecked")
    protected T castRef(ElementReference ref) {
        return (T) ref;
    }

    @Override
    public @Nullable ElementReference succ(ElementReference ref) {
        return castRef(ref).getNext();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ElementReference insertBefore(E element, ElementReference ref) {
        T entry = makeEntry(element);
        insertBeforeEntry(entry, (T) ref);
        return entry;
    }

    /**
     * Inserts a new entry <i>before</i> a given one.
     *
     * @param e
     *         the entry to add.
     * @param insertPos
     *         the entry before which to add the new one.
     */
    protected void insertBeforeEntry(T e, T insertPos) {
        T oldPrev = insertPos.getPrev();
        e.setNext(insertPos);
        e.setPrev(oldPrev);
        insertPos.setPrev(e);
        if (oldPrev != null) {
            oldPrev.setNext(e);
        } else {
            head = e;
        }
        size++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ElementReference insertAfter(E element, ElementReference ref) {
        T entry = makeEntry(element);
        insertAfterEntry(entry, (T) ref);
        return entry;
    }

    /**
     * Inserts a new entry <i>after</i> a given one.
     *
     * @param e
     *         the entry to add.
     * @param insertPos
     *         the entry before which to add the new one.
     */
    protected void insertAfterEntry(T e, T insertPos) {
        T oldNext = insertPos.getNext();
        e.setNext(oldNext);
        e.setPrev(insertPos);
        insertPos.setNext(e);
        if (oldNext != null) {
            oldNext.setPrev(e);
        } else {
            last = e;
        }
        size++;
    }

    /**
     * Swaps the contents of two linked lists with the same entry types. This method runs in constant time.
     *
     * @param other
     *         the other list to swap contents with.
     */
    public void swap(AbstractLinkedList<E, T> other) {
        int sizeTmp = this.size;
        T headTmp = this.head;
        T lastTmp = this.last;
        this.size = other.size;
        this.head = other.head;
        this.last = other.last;
        other.size = sizeTmp;
        other.head = headTmp;
        other.last = lastTmp;
    }

    /**
     * Iterator that follows the linked structure of the elements.
     *
     * @author Malte Isberner
     */
    private class LinkedListEntryIterator implements Iterator<T> {

        // previous entry
        private @Nullable T prev;
        // current entry
        private @Nullable T current;

        /*
         * Constructor.
         */
        LinkedListEntryIterator(@Nullable T head) {
            this.current = head;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (current == null) {
                throw new NoSuchElementException();
            }
            T e = current;
            current = current.getNext();
            prev = e;
            return e;
        }

        @Override
        public void remove() {
            if (prev == null) {
                throw new IllegalStateException();
            }
            removeEntry(prev);
            prev = null;
        }
    }

    private class ElementIterator implements Iterator<E> {

        // previous entry
        private @Nullable T prev;
        // current entry
        private @Nullable T current;

        /*
         * Constructor.
         */
        ElementIterator(@Nullable T head) {
            this.current = head;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            if (current == null) {
                throw new NoSuchElementException();
            }
            T e = current;
            current = current.getNext();
            prev = e;
            return e.getElement();
        }

        @Override
        public void remove() {
            if (prev == null) {
                throw new IllegalStateException();
            }
            removeEntry(prev);
            prev = null;
        }
    }

}
