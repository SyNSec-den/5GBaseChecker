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
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class DefaultLinkedListTest {

    private DefaultLinkedList<Object> linkedList;
    private final Object first = new Object();
    private final Object second = new Object();
    private final Object third = new Object();
    private ElementReference firstRef, secondRef, thirdRef;

    @BeforeClass
    public void setup() {
        linkedList = new DefaultLinkedList<>();
        firstRef = linkedList.referencedAdd(first);
        secondRef = linkedList.referencedAdd(second);
        thirdRef = linkedList.referencedAdd(third);
    }

    @Test
    public void testInsertRemove() {
        Assert.assertEquals(linkedList.size(), 3);
        Assert.assertEquals(linkedList.get(firstRef), first);
        Assert.assertEquals(linkedList.get(secondRef), second);
        Assert.assertEquals(linkedList.get(thirdRef), third);

        Assert.assertEquals(linkedList.getFront(), first);
        Assert.assertEquals(linkedList.getBack(), third);

        Object fourth = new Object();
        ElementReference fourthRef = linkedList.pushFront(fourth);
        Assert.assertEquals(linkedList.size(), 4);
        Assert.assertEquals(linkedList.get(fourthRef), fourth);
        Assert.assertEquals(linkedList.getFront(), fourth);
        Assert.assertEquals(linkedList.get(linkedList.succ(fourthRef)), first);

        Object fifth = new Object();
        linkedList.insertBefore(fifth, fourthRef);
        linkedList.insertAfter(fifth, thirdRef);

        Assert.assertEquals(linkedList.popBack(), fifth);
        Assert.assertEquals(linkedList.popFront(), fifth);

        final List<ElementReference> expected = Arrays.asList(fourthRef, firstRef, secondRef, thirdRef);
        Assert.assertEquals(expected.iterator(), linkedList.referenceIterator());
    }
}
