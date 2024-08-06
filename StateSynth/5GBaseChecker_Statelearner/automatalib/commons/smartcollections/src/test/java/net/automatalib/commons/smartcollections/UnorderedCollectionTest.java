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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Some tests for manipulating an {@link UnorderedCollection}.
 *
 * @author frohme
 */
public class UnorderedCollectionTest {

    private UnorderedCollection<Integer> collection;

    @BeforeClass
    public void setUp() {
        this.collection = new UnorderedCollection<>(Collections.singleton(0));
    }

    @Test
    public void testAddArray() {
        this.collection.addAll(new Integer[] {1, 1, 1});
    }

    @Test
    public void testAddCollection() {
        this.collection.addAll(Arrays.asList(2, 2, 2));
    }

    @Test
    public void testAddIterable() {
        this.collection.addAll((Iterable<Integer>) Arrays.asList(3, 3, 3));
    }

    @Test(dependsOnMethods = {"testAddArray", "testAddCollection", "testAddIterable"})
    public void testRemove() {
        Assert.assertTrue(this.collection.remove(0));
        Assert.assertTrue(this.collection.remove(1));
        Assert.assertTrue(this.collection.remove(2));
        Assert.assertTrue(this.collection.remove(3));
    }

    @Test(dependsOnMethods = {"testAddArray", "testAddCollection", "testAddIterable", "testRemove"})
    public void testContains() {
        final List<Integer> allElements = new ArrayList<>(this.collection);
        Collections.sort(allElements);

        Assert.assertEquals(allElements, Arrays.asList(1, 1, 2, 2, 3, 3));

    }
}
