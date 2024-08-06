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

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.internal.collections.Ints;

/**
 * @author frohme
 */
public class PositiveIntSetTest {

    @Test
    public void testBackedView() {
        final BitSet set = new BitSet();
        final PositiveIntSet view = new PositiveIntSet(set);

        set.set(1);
        Assert.assertTrue(view.contains(1));

        final int[] ints = {1, 2, 3};
        final List<Integer> list = Ints.asList(ints);

        IntStream.of(ints).forEach(set::set);
        Assert.assertTrue(view.containsAll(list));

        set.clear(3);
        Assert.assertFalse(view.contains(3));
        Assert.assertFalse(view.containsAll(list));

        set.clear();
        set.set(2);
        Assert.assertFalse(view.contains(1));
        Assert.assertTrue(view.contains(2));
        Assert.assertFalse(view.contains(3));

        set.clear();
        Assert.assertTrue(view.isEmpty());
    }

    @Test
    public void testMutability() {
        final PositiveIntSet set = new PositiveIntSet();

        Assert.assertTrue(set.isEmpty());

        Assert.assertTrue(set.add(1));
        Assert.assertTrue(set.contains(1));
        Assert.assertFalse(set.add(1));

        final List<Integer> ints = Ints.asList(1, 2, 3);

        Assert.assertTrue(set.addAll(ints));
        Assert.assertTrue(set.containsAll(ints));

        Assert.assertTrue(set.remove(3));
        Assert.assertFalse(set.contains(3));
        Assert.assertFalse(set.containsAll(ints));
        Assert.assertFalse(set.remove(3));
        Assert.assertFalse(set.remove("asd"));

        Assert.assertTrue(set.retainAll(Ints.asList(2)));
        Assert.assertFalse(set.contains(1));
        Assert.assertTrue(set.contains(2));
        Assert.assertFalse(set.contains(3));
        Assert.assertFalse(set.contains("asd"));

        set.clear();
        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void testImmutability() {
        final int[] ints = {1, 2, 3};
        final PositiveIntSet view = getExampleView(true, ints);
        final Collection<Integer> tmp = Ints.asList(ints);

        Assert.assertThrows(UnsupportedOperationException.class, () -> view.add(1));
        Assert.assertThrows(UnsupportedOperationException.class, () -> view.addAll(tmp));
        Assert.assertThrows(UnsupportedOperationException.class, view::clear);
        Assert.assertThrows(UnsupportedOperationException.class, () -> view.remove(1));
        Assert.assertThrows(UnsupportedOperationException.class, () -> view.removeIf((x) -> true));
        Assert.assertThrows(UnsupportedOperationException.class, () -> view.removeAll(tmp));
        Assert.assertThrows(UnsupportedOperationException.class, () -> view.retainAll(Collections.emptyList()));
    }

    private PositiveIntSet getExampleView(boolean immutable, int... ints) {
        final BitSet set = new BitSet();
        IntStream.of(ints).forEach(set::set);
        return new PositiveIntSet(set, immutable);
    }
}
