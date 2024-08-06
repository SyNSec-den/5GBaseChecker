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

import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CharRangeTest {

    final char start = 'a', end = 'f';
    final CharRange range = new CharRange(start, end);

    @Test
    public void testListIterator() {
        ListIterator<Character> iterator = range.listIterator(range.size() + 1);
        Assert.assertEquals(iterator.nextIndex(), range.size());
        Assert.assertThrows(NoSuchElementException.class, iterator::next);

        for (char i = end; i >= start; i--) {
            Assert.assertTrue(iterator.hasPrevious());
            Assert.assertEquals(iterator.previous(), Character.valueOf(i));
        }

        Assert.assertFalse(iterator.hasPrevious());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.nextIndex(), 0);
        Assert.assertEquals(iterator.previousIndex(), -1);
        Assert.assertThrows(NoSuchElementException.class, iterator::previous);
    }
}
