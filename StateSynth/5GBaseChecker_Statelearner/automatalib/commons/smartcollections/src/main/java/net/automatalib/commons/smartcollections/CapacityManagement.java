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
 * Control interface for collections supporting a capacity management, i.e., reserving space in advance in order to
 * avoid repeated reallocations.
 *
 * @author Malte Isberner
 */
public interface CapacityManagement {

    /**
     * Ensures that the internal storage has room for at least the provided number of elements.
     *
     * @param minCapacity
     *         the minimal number of elements the storage should have room for.
     *
     * @return <code>true</code> iff the internal storage had to be resized, <code>false</code> otherwise.
     */
    boolean ensureCapacity(int minCapacity);

    /**
     * Ensures that the internal storage has room for at least the provided number of <i>additional</i> elements.
     * <p>
     * Calling this method is equivalent to calling the above {@link #ensureCapacity(int)} with an argument of
     * <code>size() + additionalCapacity</code>.
     *
     * @param additionalCapacity
     *         the number of additional elements the storage should have room for.
     *
     * @return <code>true</code> iff the internal storage had to be resized, <code>false</code> otherwise.
     */
    boolean ensureAdditionalCapacity(int additionalCapacity);

    /**
     * Gives a hint regarding the capacity that should be reserved when resizing the internal storage for the next time.
     * This method acts like a "lazy" {@link #ensureCapacity(int)}, i.e. it reserves the specified capacity at the time
     * the next resizing of the internal storage is performed.
     * <p>
     * This method is useful when a not too imprecise upper bound on the elements that will in consequence be added is
     * known. Since the actual number of elements added may be lower than the specified upper bound, a resizing that
     * would have been performed by {@link #ensureCapacity(int)} might not be necessary.
     *
     * @param nextCapacityHint
     *         the next capacity hint.
     */
    void hintNextCapacity(int nextCapacityHint);
}
