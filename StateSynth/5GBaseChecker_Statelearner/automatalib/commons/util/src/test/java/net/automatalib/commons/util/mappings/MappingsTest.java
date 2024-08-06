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
package net.automatalib.commons.util.mappings;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class MappingsTest {

    @Test
    public void testIndexStringMapping() {
        List<Long> indices = Lists.newArrayList(0L, 1L, 25L, 26L, 17575L);
        List<String> strings = Lists.newArrayList("a", "b", "z", "ab", "zzz");

        List<String> actualStrings = Mappings.apply(Mappings.indexToString(), indices);
        Assert.assertEquals(actualStrings, strings);

        // and backwards
        List<Long> actualIndices = Mappings.apply(Mappings.stringToIndex(), actualStrings);
        Assert.assertEquals(actualIndices, indices);
    }
}
