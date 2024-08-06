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
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class IntRangeTest {

    private IntRange ir0, ir1;

    @BeforeClass
    public void setup() {
        ir0 = new IntRange(10, 20);
        ir1 = new IntRange(20, 30, 3);
    }

    @Test
    public void testSize() {
        Assert.assertEquals(ir0.size(), 10);
        Assert.assertEquals(ir1.size(), 4);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(ir0.get(0).intValue(), 10);
        Assert.assertEquals(ir0.get(4).intValue(), 14);

        Assert.assertEquals(ir1.get(2).intValue(), 26);
        Assert.assertEquals(ir1.get(3).intValue(), 29);
    }

    @Test
    public void testIterator() {
        testIterator(ir0);
        testIterator(ir1);
    }

    private static <T> void testIterator(List<T> lst) {
        Iterator<T> it = lst.iterator();

        int idx = 0;

        while (it.hasNext()) {
            T itObj = it.next();
            T lstObj = lst.get(idx++);
            Assert.assertEquals(itObj, lstObj);
        }

        Assert.assertEquals(idx, lst.size());
    }
}
