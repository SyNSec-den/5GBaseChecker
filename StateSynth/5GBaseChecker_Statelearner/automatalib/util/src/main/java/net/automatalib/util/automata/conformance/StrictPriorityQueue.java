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
package net.automatalib.util.automata.conformance;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterators;
import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A priority queue which enforces that no two elements that it contains are equal wrt. the specified comparator (i.e.,
 * {@link Comparator#compare(Object, Object)} does not return {@code 0} for two distinct elements).
 * <p>
 * If an element is inserted which, according to the {@link Comparator}, is already present, the specified {@link
 * MergeOperation}'s {@link MergeOperation#merge(Object, Object)} method is invoked to determine the replacement
 * element.
 * <p>
 * The name derives from the fact that subsequent calls to {@link #extractMin()} will yield a <i>strictly</i> growing
 * sequence of elements.
 * <p>
 * This class does not disallow {@code null} values, but the supplied {@link Comparator} has to support them.
 *
 * @param <E>
 *         element type
 *
 * @author Malte Isberner
 */
public class StrictPriorityQueue<E> extends AbstractQueue<E> {

    private final ResizingArrayStorage<E> storage = new ResizingArrayStorage<>(Object.class);
    private final Comparator<? super E> comparator;
    private final MergeOperation<E> mergeOp;
    private int size;

    /**
     * Constructor.
     *
     * @param comparator
     *         the comparator used to compare elements
     * @param mergeOp
     *         the merge operation to perform for equally-ranked elements
     */
    public StrictPriorityQueue(Comparator<? super E> comparator, MergeOperation<E> mergeOp) {
        this.comparator = comparator;
        this.mergeOp = mergeOp;
    }

    /**
     * Retrieves, but does not remove the element at the head of the queue (i.e., the minimum element in the queue).
     * <p>
     * Note: Unlike {@link #peek()}, this method throws a {@link NoSuchElementException} in case of an empty priority
     * queue.
     *
     * @return the minimum element in the queue
     */
    public E peekMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return storage.array[0];
    }

    @Override
    public boolean offer(E e) {
        return insert(e);
    }

    /**
     * Inserts an element into the queue.
     *
     * @param object
     *         the element to insert
     *
     * @return {@code true} if a new element has been inserted (i.e., the size has grown), {@code false} otherwise
     * (i.e., an existing element has been replaced)
     */
    public boolean insert(E object) {
        storage.ensureCapacity(size + 1);
        storage.array[size++] = object;
        if (!upHeap()) {
            size--;
            return false;
        }
        return true;
    }

    /**
     * Moves the last element upwards in the heap until the heap condition is restored.
     *
     * @return {@code true} if the element has been inserted, {@code false} if it has been merged with an existing
     * element.
     */
    private boolean upHeap() {
        int currIdx = size - 1;
        E elem = storage.array[currIdx];

        int steps = 0;

        while (currIdx > 0) {
            int parentIdx = currIdx / 2;
            E parent = storage.array[parentIdx];
            int cmp = comparator.compare(elem, parent);
            if (cmp == 0) {
                storage.array[parentIdx] = mergeOp.merge(parent, elem);
                return false;
            } else if (cmp > 0) {
                break;
            }

            currIdx = parentIdx;
            steps++;
        }

        currIdx = size - 1;
        for (int i = 0; i < steps; i++) {
            int parentIdx = currIdx / 2;
            storage.array[currIdx] = storage.array[parentIdx];
            currIdx = parentIdx;
        }
        storage.array[currIdx] = elem;

        return true;
    }

    @Override
    public @Nullable E poll() {
        if (size == 0) {
            return null;
        }
        return extractMin();
    }

    /**
     * Retrieves and removes the element at the head of the queue (i.e., the minimum element in the queue).
     * <p>
     * Note: Unlike {@link #poll()}, this method throws a {@link NoSuchElementException} in case of an empty priority
     * queue.
     *
     * @return the minimum element in the queue
     */
    @SuppressWarnings("nullness") // setting 'null' is fine, because we also decrease the size
    public E extractMin() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        E result = storage.array[0];
        size--;
        if (size > 0) {
            storage.array[0] = storage.array[size];
            downHeap();
        }
        storage.array[size] = null;

        return result;
    }

    /**
     * Sifts the topmost element down into the heap until the heap condition is restored.
     */
    private void downHeap() {
        E elem = storage.array[0];
        int currIdx = 0;

        while (2 * currIdx < size) {
            int leftChildIdx = 2 * currIdx;
            E leftChild = storage.array[leftChildIdx];
            if (comparator.compare(elem, leftChild) > 0) {
                storage.array[currIdx] = leftChild;
                storage.array[leftChildIdx] = elem;
                currIdx = leftChildIdx;
            } else if (2 * currIdx + 1 < size) {
                int rightChildIdx = 2 * currIdx + 1;
                E rightChild = storage.array[rightChildIdx];
                if (comparator.compare(elem, rightChild) > 0) {
                    storage.array[currIdx] = rightChild;
                    storage.array[rightChildIdx] = elem;
                    currIdx = rightChildIdx;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    @Override
    public @Nullable E peek() {
        if (size == 0) {
            return null;
        }
        return storage.array[0];
    }

    @Override
    public Iterator<E> iterator() {
        return Iterators.forArray(storage.array);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        StringBuilder result = new StringBuilder();
        result.append('[').append(storage.array[0]);
        for (int i = 1; i < size; i++) {
            result.append(',');
            result.append(storage.array[i]);
        }
        result.append(']');
        return result.toString();
    }

    /**
     * The merge operation two perform on two equally-ranked elements.
     *
     * @param <E>
     *         element type
     *
     * @author Malte Isberner
     */
    public interface MergeOperation<E> {

        /**
         * Merges the old element and the new element into a replacement element.
         * <p>
         * Implementations can assume that {@code cmp.compare(oldObject, newObject) == 0} holds for the comparator
         * {@code cmp} specified in {@link StrictPriorityQueue#StrictPriorityQueue(Comparator, MergeOperation)}. In
         * turn, they must guarantee that also {@code cmp.compare(result, oldObject) == 0} holds for the return value
         * {@code result}.
         *
         * @param oldObject
         *         the old element
         * @param newObject
         *         the new element
         *
         * @return the replacement element
         */
        E merge(E oldObject, E newObject);
    }

}
