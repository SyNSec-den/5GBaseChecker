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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BackedGeneralPriorityQueueTest {

    private BackedGeneralPriorityQueue<Character, Integer> queue;

    @BeforeClass
    public void setUp() {
        final List<Character> values = Lists.charactersOf("abcdefghij");
        final List<Integer> keys = Ints.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Collections.reverse(keys);

        this.queue = new BackedGeneralPriorityQueue<>(values, keys);
    }

    @Test
    public void testExtractMin() {
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(this.queue.extractMin(), Character.valueOf((char) ('j' - i)));
        }
    }

    @Test(dependsOnMethods = "testExtractMin")
    public void testChangeKeys() {
        for (int i = 0; i < 5; i++) {
            final ElementReference ref = this.queue.find((char) ('a' + i));
            this.queue.changeKey(ref, i);
        }
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(this.queue.extractMin(), Character.valueOf((char) ('a' + i)));
        }
    }

    @Test(dependsOnMethods = "testChangeKeys")
    public void testDefaultInsert() {
        Assert.assertTrue(this.queue.isEmpty());

        this.queue.add('c', 3);
        this.queue.add('a', 1);
        this.queue.add('b', 2);

        // inserting without a key, should use the default key (null) which is the smallest element
        this.queue.add('z');

        Assert.assertEquals(this.queue.extractMin(), Character.valueOf('a'));
        Assert.assertEquals(this.queue.extractMin(), Character.valueOf('b'));
        Assert.assertEquals(this.queue.extractMin(), Character.valueOf('c'));
        Assert.assertEquals(this.queue.extractMin(), Character.valueOf('z'));
        Assert.assertTrue(this.queue.isEmpty());
    }

    @Test(dependsOnMethods = "testDefaultInsert")
    public void testFindReplaceAndClear() {

        Assert.assertTrue(this.queue.isEmpty());

        final ElementReference c = this.queue.add('c', 3);
        final ElementReference a = this.queue.add('a', 1);
        final ElementReference b = this.queue.add('b', 2);

        Assert.assertEquals(this.queue.get(c), Character.valueOf('c'));
        Assert.assertEquals(this.queue.get(b), Character.valueOf('b'));
        Assert.assertEquals(this.queue.get(a), Character.valueOf('a'));

        this.queue.replace(c, 'a');
        this.queue.replace(a, 'c');

        Assert.assertEquals(this.queue.get(c), Character.valueOf('a'));
        Assert.assertEquals(this.queue.get(b), Character.valueOf('b'));
        Assert.assertEquals(this.queue.get(a), Character.valueOf('c'));

        this.queue.remove(b);
        Assert.assertEquals(this.queue.size(), 2);

        this.queue.clear();
        Assert.assertTrue(this.queue.isEmpty());
    }

    @Test(dependsOnMethods = "testFindReplaceAndClear")
    public void testEmptyExceptions() {
        Assert.assertThrows(NoSuchElementException.class, () -> this.queue.choose());
        Assert.assertThrows(NoSuchElementException.class, () -> this.queue.chooseRef());
    }
}
