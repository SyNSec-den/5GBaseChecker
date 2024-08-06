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

import com.google.common.primitives.Ints;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class IntSeqTest {

    private final int[] arr1 = {1, 2, 3, 4, 5, 6};
    private final int[] arr2 = {31, 43, 45, 1, 3445, 56};

    @Test
    public void testArrays() {
        Assert.assertEquals(IntSeq.of(arr1), Ints.asList(arr1));
        Assert.assertEquals(IntSeq.of(arr2), Ints.asList(arr2));
    }

    @Test
    public void testLists() {
        Assert.assertEquals(IntSeq.of(Ints.asList(arr1)), Ints.asList(arr1));
        Assert.assertEquals(IntSeq.of(Ints.asList(arr2)), Ints.asList(arr2));
    }
}
