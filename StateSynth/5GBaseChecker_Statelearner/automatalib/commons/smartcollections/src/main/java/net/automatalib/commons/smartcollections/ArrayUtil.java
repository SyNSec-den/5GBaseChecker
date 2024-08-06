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
 * Utility methods for arrays.
 *
 * @author frohme
 */
public final class ArrayUtil {

    private ArrayUtil() {
        // prevent instantiation
    }

    /**
     * A convenient method for calling {@code computeNewCapacity(length, requiredCapacity, 0)}.
     *
     * @param length
     *         the current length of the array
     * @param requiredCapacity
     *         the immediately required capacity
     *
     * @return the size of an array that is guaranteed to hold {@code requiredCapacity} number of elements.
     *
     * @see #computeNewCapacity(int, int, int)
     */
    public static int computeNewCapacity(int length, int requiredCapacity) {
        return computeNewCapacity(length, requiredCapacity, 0);
    }

    /**
     * Computes the size of an array that is required to hold {@code requiredCapacity} number of elements.
     * <p>
     * This method first tries to increase the size of the array by a factor of 1.5 to prevent a sequence of successive
     * increases by 1. It then evaluates the {@code nextCapacityHint} parameter as well as the {@code requiredCapacity}
     * parameter to determine the next size.
     *
     * @param length
     *         the current length of the array
     * @param requiredCapacity
     *         the immediately required capacity
     * @param nextCapacityHint
     *         a hint for future capacity requirements that may not be required as of now
     *
     * @return the size of an array that is guaranteed to hold {@code requiredCapacity} number of elements.
     */
    public static int computeNewCapacity(int length, int requiredCapacity, int nextCapacityHint) {
        if (requiredCapacity < length) {
            return length;
        }

        int newCapacity = (length * 3) / 2 + 1;

        if (newCapacity < nextCapacityHint) {
            newCapacity = nextCapacityHint;
        }

        if (newCapacity < requiredCapacity) {
            newCapacity = requiredCapacity;
        }

        return newCapacity;
    }
}
