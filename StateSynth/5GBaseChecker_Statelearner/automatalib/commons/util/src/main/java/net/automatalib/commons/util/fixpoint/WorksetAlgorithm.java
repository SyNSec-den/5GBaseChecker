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
package net.automatalib.commons.util.fixpoint;

import java.util.Collection;

/**
 * @author msc
 */
public interface WorksetAlgorithm<T, R> {

    /**
     * Provides a hint to initialize containers appropriately.
     *
     * @return expected number of elements visited
     */
    int expectedElementCount();

    /**
     * Provide the initial elements that should be processed by {@link #update(Object) update}.
     *
     * @return initial elements
     */
    Collection<T> initialize();

    /**
     * Process the given element and perform its corresponding actions. If during this process the need arises to update
     * other elements as well, return them.
     *
     * @param currentT
     *         the current element that should be processed by this method
     *
     * @return a collection of elements that need to be processed
     */
    Collection<T> update(T currentT);

    /**
     * Provides the result of this algorithms internal action. More precise, this function returns the accumulated
     * object E after the successful application of <i>all</i> {@link #update(Object) update} calls.
     *
     * @return the resulting object
     */
    R result();
}
