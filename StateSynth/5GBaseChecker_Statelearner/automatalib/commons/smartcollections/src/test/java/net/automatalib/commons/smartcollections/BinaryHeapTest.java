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

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class BinaryHeapTest {

    private final BinaryHeap<Integer> heap = BinaryHeap.create(Arrays.asList(42, 37));

    @Test
    public void testHeapOps() {
        Assert.assertEquals(heap.size(), 2);
        Assert.assertFalse(heap.isEmpty());
        Assert.assertFalse(heap.contains(13));

        Assert.assertEquals(heap.peekMin().intValue(), 37);

        Assert.assertTrue(heap.add(13));
        Assert.assertEquals(heap.size(), 3);

        Assert.assertEquals(heap.peekMin().intValue(), 13);
        Assert.assertEquals(heap.extractMin().intValue(), 13);
        Assert.assertEquals(heap.size(), 2);

        Assert.assertEquals(heap.peekMin().intValue(), 37);
        heap.add(40);
        Assert.assertEquals(heap.size(), 3);
        Assert.assertEquals(heap.peekMin().intValue(), 37);
        Assert.assertEquals(heap.extractMin().intValue(), 37);
        Assert.assertEquals(heap.size(), 2);

        Assert.assertEquals(heap.peekMin().intValue(), 40);
        Assert.assertEquals(heap.extractMin().intValue(), 40);
        Assert.assertEquals(heap.size(), 1);

        Assert.assertEquals(heap.peekMin().intValue(), 42);
        Assert.assertEquals(heap.extractMin().intValue(), 42);

        Assert.assertEquals(heap.size(), 0);
        Assert.assertTrue(heap.isEmpty());
    }

}
