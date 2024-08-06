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
package net.automatalib.commons.util.comparison;

import java.util.Comparator;
import java.util.List;

import net.automatalib.commons.util.comparison.CmpUtil.NullOrdering;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class CmpUtilTest {

    @Test
    public void testArrays() {
        int[] arr1 = new int[] {1, 2, 2, 3};
        int[] arr2 = new int[] {1, 2, 3};

        Assert.assertEquals(CmpUtil.canonicalCompare(arr1, arr2), 1);
        Assert.assertEquals(CmpUtil.canonicalCompare(arr2, arr1), -1);

        Assert.assertEquals(CmpUtil.lexCompare(arr1, arr2), -1);
        Assert.assertEquals(CmpUtil.lexCompare(arr2, arr1), 1);

        arr1 = new int[] {1, 2, 3};

        Assert.assertEquals(CmpUtil.canonicalCompare(arr1, arr2), 0);
        Assert.assertEquals(CmpUtil.canonicalCompare(arr2, arr1), 0);

        Assert.assertEquals(CmpUtil.lexCompare(arr1, arr2), 0);
        Assert.assertEquals(CmpUtil.lexCompare(arr2, arr1), 0);
    }

    @Test
    public void testLists() {
        final List<Integer> l1 = Lists.newArrayList(1, 2, 2, 3, null);
        final List<Integer> l2 = Lists.newArrayList(1, 2, 3, null);

        Assert.assertEquals(CmpUtil.canonicalCompare(l1, l2), 1);
        Assert.assertEquals(CmpUtil.canonicalCompare(l2, l1), -1);

        final Comparator<Integer> comp = CmpUtil.safeComparator(Integer::compareTo, NullOrdering.MAX);

        final Comparator<List<Integer>> canonicalComp = CmpUtil.canonicalComparator(comp);
        final Comparator<List<Integer>> lexComparator = CmpUtil.lexComparator(comp);

        Assert.assertEquals(canonicalComp.compare(l1, l2), 1);
        Assert.assertEquals(canonicalComp.compare(l2, l1), -1);

        Assert.assertEquals(lexComparator.compare(l1, l2), -1);
        Assert.assertEquals(lexComparator.compare(l2, l1), 1);

        l1.remove(1);

        Assert.assertThrows(NullPointerException.class, () -> CmpUtil.canonicalCompare(l1, l2));
        Assert.assertThrows(NullPointerException.class, () -> CmpUtil.canonicalCompare(l2, l1));

        Assert.assertEquals(canonicalComp.compare(l1, l2), 0);
        Assert.assertEquals(canonicalComp.compare(l2, l1), 0);

        Assert.assertEquals(lexComparator.compare(l1, l2), 0);
        Assert.assertEquals(lexComparator.compare(l2, l1), 0);
    }
}
