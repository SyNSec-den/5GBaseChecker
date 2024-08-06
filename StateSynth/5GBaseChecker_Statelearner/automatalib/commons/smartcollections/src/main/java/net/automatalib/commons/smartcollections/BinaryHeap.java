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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link PriorityQueue} implementation using a binary heap.
 *
 * @param <E>
 *         element class.
 *
 * @author Malte Isberner
 */
public class BinaryHeap<E> extends AbstractSmartCollection<E>
        implements SmartDynamicPriorityQueue<E>, CapacityManagement, Queue<E> {

    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    private final Comparator<? super E> comparator;
    // Entry storage.
    private final ResizingArrayStorage<Reference<E>> entries;
    // Number of entries in the queue.
    private int size;

    protected BinaryHeap(int initCapacity, Collection<? extends E> initValues, Comparator<? super E> comparator) {
        this(Math.max(initCapacity, initValues.size()), comparator);
        int i = 0;
        for (E e : initValues) {
            entries.array[i++] = new Reference<>(0, e);
        }
        size = initValues.size();
        for (int j = size / 2; j >= 0; j--) {
            downHeap(j);
        }
    }

    protected BinaryHeap(int initialCapacity, Comparator<? super E> comparator) {
        this.entries = new ResizingArrayStorage<>(Reference.class, initialCapacity);
        this.comparator = comparator;
    }

    /**
     * Move an element downwards inside the heap, until all of its children have a key greater or equal to its own.
     */
    private void downHeap(@UnknownInitialization(BinaryHeap.class) BinaryHeap<E> this, int idx) {
        Reference<E> e = entries.array[idx];

        int iter = idx;
        while (hasChildren(iter)) {
            int cidx = leftChild(iter);
            Reference<E> c = entries.array[cidx];

            if (hasRightChild(iter)) {
                int rcidx = rightChild(iter);
                Reference<E> rc = entries.array[rcidx];
                if (compare(rc, c) < 0) {
                    cidx = rcidx;
                    c = rc;
                }
            }

            if (compare(e, c) <= 0) {
                break;
            }

            entries.array[cidx] = e;
            entries.array[iter] = c;
            c.index = iter;
            iter = cidx;
        }

        e.index = iter;
    }

    /**
     * Checks whether the entry at the specified index has at least one child.
     */
    private boolean hasChildren(@UnknownInitialization(BinaryHeap.class) BinaryHeap<E> this, int idx) {
        return idx * 2 < size;
    }

    /**
     * Retrieves the index of the left child of a given parent index.
     */
    private static int leftChild(int parent) {
        return 2 * parent;
    }

    /**
     * Checks whether the entry at the specified index has two children.
     */
    private boolean hasRightChild(@UnknownInitialization(BinaryHeap.class) BinaryHeap<E> this, int idx) {
        return idx * 2 + 1 < size;
    }

    /**
     * Retrieves the index of the right child of a given parent index.
     */
    private static int rightChild(int parent) {
        return 2 * parent + 1;
    }

    /**
     * Compares the referenced elements.
     */
    private int compare(@UnknownInitialization(BinaryHeap.class) BinaryHeap<E> this, Reference<E> e1, Reference<E> e2) {
        return comparator.compare(e1.element, e2.element);
    }

    public static <E extends Comparable<E>> BinaryHeap<E> create() {
        return new BinaryHeap<>(DEFAULT_INITIAL_CAPACITY, Comparator.naturalOrder());
    }

    public static <E extends Comparable<E>> BinaryHeap<E> create(int initialCapacity) {
        return new BinaryHeap<>(initialCapacity, Comparator.naturalOrder());
    }

    public static <E extends Comparable<E>> BinaryHeap<E> create(Collection<? extends E> initValues) {
        return new BinaryHeap<>(0, initValues, Comparator.naturalOrder());
    }

    public static <E extends Comparable<E>> BinaryHeap<E> create(int initialCapacity,
                                                                 Collection<? extends E> initValues) {
        return new BinaryHeap<>(initialCapacity, initValues, Comparator.naturalOrder());
    }

    public static <E> BinaryHeap<E> createCmp(Comparator<? super E> comparator) {
        return new BinaryHeap<>(DEFAULT_INITIAL_CAPACITY, comparator);
    }

    public static <E> BinaryHeap<E> createCmp(Comparator<? super E> comparator, int initialCapacity) {
        return new BinaryHeap<>(initialCapacity, comparator);
    }

    public static <E> BinaryHeap<E> createCmp(Comparator<? super E> comparator, Collection<? extends E> initValues) {
        return new BinaryHeap<>(0, initValues, comparator);
    }

    public static <E> BinaryHeap<E> createCmp(Comparator<? super E> comparator,
                                              int initialCapacity,
                                              Collection<? extends E> initValues) {
        return new BinaryHeap<>(initialCapacity, initValues, comparator);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(ElementReference ref) {
        return BinaryHeap.<E>asHeapRef(ref).element;
    }

    @SuppressWarnings("unchecked")
    private static <E> Reference<E> asHeapRef(ElementReference ref) {
        if (ref.getClass() != Reference.class) {
            throw new InvalidReferenceException(
                    "Reference is of wrong class '" + ref.getClass().getName() + "', should be " +
                    Reference.class.getName() + ".");
        }
        return (Reference<E>) ref;
    }

    @Override
    public Reference<E> referencedAdd(E elem) {
        ensureCapacity(size + 1);

        Reference<E> entry = new Reference<>(size, elem);
        entries.array[size] = entry;
        upHeap(size++);

        return entry;
    }

    @Override
    public void remove(ElementReference ref) {
        remove(asHeapRef(ref).index);
    }

    /**
     * Removes the element at the specified index from the heap. This is done by simulating a key decrease to -infinity
     * and then performing extractMin.
     */
    private void remove(int index) {
        forceToTop(index);
        extractMin();
    }

    @Override
    public E remove() {
        return extractMin();
    }

    private void forceToTop(int idx) {
        Reference<E> e = entries.array[idx];

        int iter = idx;
        while (hasParent(iter)) {
            int pidx = parent(iter);
            Reference<E> p = entries.array[pidx];
            entries.array[pidx] = e;
            entries.array[iter] = p;
            p.index = iter;
            iter = parent(iter);
        }
        e.index = iter;
    }

    @Override
    public Iterator<ElementReference> referenceIterator() {
        return new ReferenceIterator();
    }

    @Override
    public void replace(ElementReference ref, E newElement) {
        Reference<E> heapRef = asHeapRef(ref);
        heapRef.element = newElement;
        keyChanged(ref);
    }

    @Override
    public void keyChanged(ElementReference ref) {
        keyChanged(asHeapRef(ref).index);
    }

    public void keyChanged(int index) {
        upHeap(index);
        downHeap(index);
    }

    @Override
    public boolean ensureCapacity(int minCapacity) {
        return entries.ensureCapacity(minCapacity);
    }

    /**
     * Move an element upwards inside the heap, until it has a parent with a key less or equal to its own.
     */
    private void upHeap(int idx) {
        Reference<E> e = entries.array[idx];

        int iter = idx;
        while (hasParent(iter)) {
            int pidx = parent(iter);
            Reference<E> p = entries.array[pidx];
            if (compare(e, p) < 0) {
                entries.array[pidx] = e;
                entries.array[iter] = p;
                p.index = iter;
                iter = parent(iter);
            } else {
                break;
            }
        }
        e.index = iter;
    }

    /**
     * Checks if the specified index has a parent.
     */
    private static boolean hasParent(int idx) {
        return idx > 0;
    }

    /**
     * Retrieves, for a given child index, its parent index.
     */

    private static int parent(int child) {
        return child / 2;
    }

    @Override
    public boolean ensureAdditionalCapacity(int additionalCapacity) {
        return ensureCapacity(size + additionalCapacity);
    }

    @Override
    public void hintNextCapacity(int nextCapacityHint) {
        entries.hintNextCapacity(nextCapacityHint);
    }

    @Override
    public void quickClear() {
        size = 0;
    }

    @SuppressWarnings("nullness") // setting 'null' is fine, when (according to JavaDoc) calling quickClear() first
    @Override
    public void deepClear() {
        entries.setAll(null);
    }

    @Override
    public boolean offer(E e) {
        add(e);
        return true;
    }

    @Override
    public @Nullable E poll() {
        if (size > 0) {
            return extractMin();
        }
        return null;
    }

    @Override
    public E element() {
        return peekMin();
    }

    @Override
    public E peekMin() {
        if (size <= 0) {
            throw new NoSuchElementException();
        }
        return entries.array[0].element;
    }

    @SuppressWarnings("nullness") // setting 'null' is fine, because we also decrease the size
    @Override
    public E extractMin() {
        if (size <= 0) {
            throw new NoSuchElementException();
        }
        E min = entries.array[0].element;
        entries.array[0] = entries.array[--size];
        entries.array[size] = null;

        if (size > 0) {
            downHeap(0);
        }

        return min;
    }

    @Override
    public @Nullable E peek() {
        if (size > 0) {
            return peekMin();
        }
        return null;
    }

    /**
     * Class for entries in a priority queue. Entry objects are returned by the {@link
     * SmartDynamicPriorityQueue#referencedAdd(Object)} method and are passed to the {@link
     * SmartDynamicPriorityQueue#keyChanged(ElementReference)} method. The usage of entry objects eliminates the
     * necessity of an extra element to index mapping.
     *
     * @param <E>
     *         element class.
     *
     * @author Malte Isberner
     */
    private static final class Reference<E> implements ElementReference {

        private int index;
        private E element;

        /**
         * Constructor.
         *
         * @param index
         *         the index of the entry inside the queue.
         * @param element
         *         the element stored in this entry.
         */
        protected Reference(int index, E element) {
            this.element = element;
            this.index = index;
        }
    }

    private class ReferenceIterator implements Iterator<ElementReference> {

        private int current;

        @Override
        public boolean hasNext() {
            return current < size;
        }

        @Override
        public ElementReference next() {
            if (current >= size) {
                throw new NoSuchElementException();
            }
            return entries.array[current++];
        }

        @Override
        public void remove() {
            BinaryHeap.this.remove(--current);
        }
    }

}
