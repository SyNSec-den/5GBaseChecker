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
package net.automatalib.commons.util.array;

import java.util.Arrays;

import net.automatalib.commons.smartcollections.ArrayWritable;

/**
 * Utility class for writing containers to arrays.
 * <p>
 * It is generally preferable to use the static methods this class offers than using {@link
 * ArrayWritable#writeToArray(int, Object[], int, int)} directly.
 *
 * @author Malte Isberner
 */
public final class AWUtil {

    private AWUtil() {
        // prevent instantiation
    }

    /**
     * Writes the complete container data to an array. This method ensures that the array's capacity is not exceeded.
     *
     * @param aw
     *         the container.
     * @param array
     *         the array
     *
     * @return the number of elements copied
     */
    public static <T, U extends T> int safeWrite(ArrayWritable<U> aw, T[] array) {
        int num = aw.size();
        if (num <= 0) {
            return 0;
        }
        if (num > array.length) {
            num = array.length;
        }
        aw.writeToArray(0, array, 0, num);
        return num;
    }

    /**
     * Writes a given maximum amount of data items from a container to an array. This method ensures that the array's
     * capacity is not exceeded.
     *
     * @param num
     *         the number of elements to copy
     * @param aw
     *         the container.
     * @param array
     *         the array
     *
     * @return the number of elements copied
     */
    public static <T, U extends T> int safeWrite(int num, ArrayWritable<U> aw, T[] array) {

        final int elementsToCopy = Math.min(num, Math.min(aw.size(), array.length));

        if (elementsToCopy <= 0) {
            return 0;
        }

        aw.writeToArray(0, array, 0, elementsToCopy);
        return elementsToCopy;
    }

    public static <T, U extends T> int safeWrite(int num, ArrayWritable<U> aw, int ofs, T[] array, int tgtOfs) {
        final int awBound = Math.min(num + ofs, aw.size());
        final int arrayBound = Math.min(num + tgtOfs, array.length);

        final int elementsToCopy = Math.min(awBound, arrayBound);

        if (elementsToCopy <= 0) {
            return 0;
        }

        aw.writeToArray(ofs, array, tgtOfs, elementsToCopy);
        return elementsToCopy;
    }

    @SuppressWarnings("nullness") // false positive?
    public static <T, U extends T> int safeWrite(ArrayWritable<U> aw, T[] array, int tgtOfs) {
        return safeWrite(aw.size(), aw, 0, array, tgtOfs);
    }

    public static Object[] toArray(ArrayWritable<?> aw) {
        int num = aw.size();
        Object[] arr = new Object[num];
        aw.writeToArray(0, arr, 0, num);
        return arr;
    }

    public static <T> T[] toArray(ArrayWritable<?> aw, T[] arr) {
        final int num = aw.size();
        final T[] targetArray;

        if (arr.length < num) {
            targetArray = Arrays.copyOf(arr, num);
        } else {
            targetArray = arr;
        }

        aw.writeToArray(0, targetArray, 0, num);
        return targetArray;
    }
}
