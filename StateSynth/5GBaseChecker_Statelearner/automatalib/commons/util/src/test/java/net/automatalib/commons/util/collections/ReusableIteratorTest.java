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
package net.automatalib.commons.util.collections;

import java.util.Iterator;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
@Test
public class ReusableIteratorTest {

    @Test
    public void testIterator() {

        final int size = 10;
        final Iterator<Integer> iterator = IntStream.range(0, size).iterator();
        final Iterable<Integer> iterable = new ReusableIterator<>(iterator);

        // create some iterators and consume them partially
        final Iterator<Integer> iter1 = iterable.iterator();
        final int firstLimit = 3;
        for (int i = 0; i < firstLimit; i++) {
            Assert.assertEquals(iter1.next(), Integer.valueOf(i));
        }

        final Iterator<Integer> iter2 = iterable.iterator();
        final int secondLimit = 8;
        for (int i = 0; i < secondLimit; i++) {
            Assert.assertEquals(iter2.next(), Integer.valueOf(i));
        }

        final Iterator<Integer> iter3 = iterable.iterator();
        final int thirdLimit = 5;
        for (int i = 0; i < thirdLimit; i++) {
            Assert.assertEquals(iter3.next(), Integer.valueOf(i));
        }

        // consume the remaining elements. intermediate allocations shouldn't change the iterators
        for (int i = firstLimit; i < size; i++) {
            Assert.assertEquals(iter1.next(), Integer.valueOf(i));
        }
        Assert.assertFalse(iter1.hasNext());

        for (int i = secondLimit; i < size; i++) {
            Assert.assertEquals(iter2.next(), Integer.valueOf(i));
        }
        Assert.assertFalse(iter2.hasNext());

        for (int i = thirdLimit; i < size; i++) {
            Assert.assertEquals(iter3.next(), Integer.valueOf(i));
        }
        Assert.assertFalse(iter3.hasNext());

    }
}
