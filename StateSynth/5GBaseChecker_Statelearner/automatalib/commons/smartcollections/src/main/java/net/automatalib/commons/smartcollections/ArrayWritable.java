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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Unified interface for (collection) classes that allow writing their contents to an array. The intended behavior
 * differs from the standard Java {@link Collection#toArray(Object[])} method in the following way: <ul> <li>It is
 * possible to specify a source offset for this collection, i.e., how many elements are skipped before the target array
 * is written to. Note that if the collection does not define an iteration order, the set of elements which are omitted
 * is unspecified if {@code offset > 0}.</li> <li>It is possible to specify an offset for the destination array, i.e.,
 * at which position to begin writing into the destination array. <li>It is possible to specify the number of elements
 * to be written. Again, if this collection does not define an iteration order, the set of elements which are omitted if
 * {@code num < size()} is unspecified.</li> </ul>
 *
 * @param <T>
 *         type class. This is a marker parameter that is not reflected in the signatures, but can be respected by
 *         methods requiring type information.
 *
 * @author Malte Isberner
 */
public interface ArrayWritable<T> {

    /**
     * Writes the contents of this container to an array. The behavior of calling this method should be equivalent to
     * <code>System.arraycopy(this.toArray(), offset, array, tgtOfs, num);</code>
     *
     * @param offset
     *         how many elements of <i>this</i> container to skip.
     * @param array
     *         the array in which to store the elements.
     * @param tgtOfs
     *         the starting offset in the target array.
     * @param num
     *         the maximum number of elements to copy.
     */
    void writeToArray(int offset, @Nullable Object[] array, int tgtOfs, int num);

    /**
     * The size of this container.
     */
    int size();
}
