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
package net.automatalib.commons.util;

/**
 * Interface for disjoint-set forest implementations that operate on a universe of contiguous integers.
 *
 * @author Malte Isberner
 */
public interface IntDisjointSets {

    /**
     * Returns the size of the universe. The elements of the universe are the integers between {@code 0} (inclusive) and
     * {@code size()} (exclusive).
     *
     * @return the size of the universe
     */
    int size();

    /**
     * Checks if two elements are in the same set.
     *
     * @param x
     *         the first element
     * @param y
     *         the second element
     *
     * @return {@code true} if {@code x} and {@code y} are in the same set, {@code false} otherwise
     */
    default boolean equivalent(int x, int y) {
        int rx = find(x);
        int ry = find(y);
        return rx == ry;
    }

    /**
     * Determines the representative element of the set containing {@code x}.
     *
     * @param x
     *         the element to find
     *
     * @return the representative element of the set containing {@code x}.
     */
    int find(int x);

    /**
     * Unites the sets containing the respective elements. If two disjoint sets were united, the return value is {@code
     * true}. Otherwise, if {@code x} and {@code y} were already in the same set, {@code false} is returned.
     * <p>
     * <b>Attention:</b> this method returns {@code true} if and only if {@code equivalent(x, y)} would have returned
     * {@code false}.
     *
     * @param x
     *         the first element
     * @param y
     *         the second element
     *
     * @return {@code true} if two disjoint sets have been united as a result, {@code false} otherwise
     */
    default boolean union(int x, int y) {
        int rx = find(x);
        int ry = find(y);
        if (rx == ry) {
            return false;
        }
        link(rx, ry);
        return true;
    }

    /**
     * Links (unites) two sets, identified by their representatives.
     *
     * @param rx
     *         the representative of the first set
     * @param ry
     *         the representative of the second set
     *
     * @return the representative of the resulting set (typically either {@code rx} or {@code ry}).
     */
    int link(int rx, int ry);

}
