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
package net.automatalib.commons.util.random;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import net.automatalib.commons.util.collections.CollectionsUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class RandomUtilTest {

    private static final int HIGH = 10;

    private RandomUtil util;

    private List<Integer> list;
    private Integer[] array;

    @BeforeClass
    public void setUp() {
        this.util = new RandomUtil(new Random(42));
        this.list = CollectionsUtil.intRange(0, HIGH, 1);
        this.array = IntStream.range(0, HIGH).boxed().toArray(Integer[]::new);
    }

    @Test
    public void testChooseArray() {
        Assert.assertNull(util.choose(new Object[0]));
        Assert.assertEquals(util.choose(new Object[] {1}), 1);

        final Integer chosenElement = util.choose(array);
        Assert.assertNotNull(chosenElement);
        Assert.assertTrue(0 <= chosenElement && chosenElement < HIGH);
    }

    @Test
    public void testChooseList() {
        Assert.assertNull(util.choose(Collections.emptyList()));
        Assert.assertEquals(util.choose(Collections.singletonList(1)), (Integer) 1);

        final Integer chosenElement = util.choose(list);
        Assert.assertNotNull(chosenElement);
        Assert.assertTrue(0 <= chosenElement && chosenElement < HIGH);
    }

    @Test
    public void testDistinctIntegers() {
        Assert.assertEquals(util.distinctIntegers(0, HIGH).length, 0);

        int[] result = util.distinctIntegers(HIGH, HIGH);
        util.distinctIntegers(HIGH, HIGH);
        Assert.assertEquals(result.length, HIGH);
        Arrays.sort(result);
        Assert.assertEquals(box(result), array);

        // Sample from lower half domain
        result = util.distinctIntegers(HIGH, HIGH / 2);
        Assert.assertEquals(result.length, HIGH / 2);
        Arrays.sort(result);
        Assert.assertEquals(box(result), Arrays.copyOfRange(array, 0, HIGH / 2));

        // Sample from upper half domain
        result = util.distinctIntegers(HIGH, HIGH / 2, HIGH);
        Assert.assertEquals(result.length, HIGH / 2);
        Arrays.sort(result);
        Assert.assertEquals(box(result), Arrays.copyOfRange(array, HIGH / 2, HIGH));

        // Sample all but one from full domain
        result = util.distinctIntegers(HIGH - 1, HIGH);
        Assert.assertEquals(result.length, HIGH - 1);
        Assert.assertEquals(new HashSet<>(Arrays.asList(box(result))).size(), HIGH - 1);
        Assert.assertTrue(Arrays.asList(array).containsAll(Arrays.asList(box(result))));
    }

    @Test
    public void testSampleUnique() {
        Assert.assertEquals(util.sampleUnique(Collections.emptyList(), HIGH), Collections.emptyList());

        List<Integer> result = util.sampleUnique(list, HIGH);
        Assert.assertEquals(result.size(), HIGH);
        result.sort(Integer::compareTo);
        Assert.assertEquals(result, list);

        // Sample all but one from full domain
        result = util.sampleUnique(list, HIGH - 1);
        Assert.assertEquals(result.size(), HIGH - 1);
        Assert.assertEquals(new HashSet<>(result).size(), HIGH - 1);
        Assert.assertTrue(list.containsAll(result));
    }

    @Test
    public void testSampleList() {
        Assert.assertEquals(util.sample(Collections.emptyList(), HIGH), Collections.emptyList());

        List<Integer> result = util.sample(list, HIGH);
        Assert.assertEquals(result.size(), HIGH);
        Assert.assertTrue(list.containsAll(result));

        // Sample double the size from full domain
        result = util.sample(list, HIGH * 2);
        Assert.assertEquals(result.size(), HIGH * 2);
        Assert.assertTrue(list.containsAll(result));

    }

    private static Integer[] box(int[] array) {
        return IntStream.of(array).boxed().toArray(Integer[]::new);
    }
}
