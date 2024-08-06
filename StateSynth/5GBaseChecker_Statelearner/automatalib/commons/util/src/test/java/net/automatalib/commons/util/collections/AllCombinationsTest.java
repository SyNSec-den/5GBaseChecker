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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class AllCombinationsTest {

    private static final List<Integer> DOMAIN1 = Arrays.asList(1, 2);
    private static final List<Integer> DOMAIN2 = Collections.singletonList(3);
    private static final List<Integer> DOMAIN3 = Arrays.asList(4, 5);

    @Test
    public void testNormalDomain() {

        final Set<List<Integer>> combinations =
                Streams.stream(CollectionsUtil.cartesianProduct(DOMAIN1, DOMAIN2, DOMAIN3))
                       .map(ArrayList::new)
                       .collect(Collectors.toSet());

        Assert.assertTrue(combinations.remove(Arrays.asList(1, 3, 4)));
        Assert.assertTrue(combinations.remove(Arrays.asList(1, 3, 5)));
        Assert.assertTrue(combinations.remove(Arrays.asList(2, 3, 4)));
        Assert.assertTrue(combinations.remove(Arrays.asList(2, 3, 5)));

        Assert.assertTrue(combinations.isEmpty());
    }

    @Test
    public void testEmptyDomain() {
        final Iterable<List<Integer>> iter =
                CollectionsUtil.cartesianProduct(DOMAIN1, Collections.emptyList(), DOMAIN3);

        Assert.assertTrue(Iterables.isEmpty(iter));
        Assert.assertThrows(NoSuchElementException.class, () -> iter.iterator().next());
    }

    @Test
    public void testEmptyDimension() {
        final Iterable<List<Integer>> iter = CollectionsUtil.cartesianProduct();

        Assert.assertEquals(Iterables.size(iter), 1);
        Assert.assertTrue(Iterables.all(iter, List::isEmpty));
    }
}
