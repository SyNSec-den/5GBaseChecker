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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReflexiveMapViewTest {

    private final Set<Integer> elements = Sets.newHashSet(1, 2, 3, 4, 5);
    private final Map<Integer, Integer> map = new ReflexiveMapView<>(elements);

    @Test
    public void testMapping() {
        for (Integer i : elements) {
            Assert.assertEquals(map.get(i), i);
        }

        for (Entry<Integer, Integer> e : map.entrySet()) {
            Assert.assertEquals(e.getKey(), e.getValue());

        }
    }

    @Test
    public void testCompleteness() {
        Assert.assertEquals(map.size(), elements.size());
        Assert.assertEquals(map.keySet(), elements);
        Assert.assertEquals(map.values(), elements);

        final Set<Integer> copy = new HashSet<>(elements);
        for (Entry<Integer, Integer> e : map.entrySet()) {
            copy.remove(e.getKey());
        }

        Assert.assertTrue(copy.isEmpty());
    }

    @Test
    public void testImmutability() {
        Assert.assertThrows(() -> map.put(6, 6));
        Assert.assertThrows(() -> map.remove(4));
        Assert.assertThrows(() -> map.entrySet().iterator().remove());
    }

    @Test
    public void testBacking() {
        Assert.assertNull(map.get(6));
        elements.add(6);
        Integer newValue = map.get(6);
        Assert.assertNotNull(newValue);
        Assert.assertEquals(newValue.intValue(), 6);
    }
}
