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
package net.automatalib.util.ts.modal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.automatalib.commons.util.Pair;
import net.automatalib.ts.modal.CompactMMC;
import net.automatalib.util.ts.modal.Subgraphs.SubgraphType;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class SubgraphsTest {

    @Test
    public void hideSymbolsTest() throws IOException {
        final CompactMMC<String> contract = TestUtils.loadMMCFromPath("/modal/contract_monitor.dot");
        final CompactMMC<String> expected = TestUtils.loadMMCFromPath("/modal/monitor_hidden.dot");

        final Collection<String> inputs = Arrays.asList("b", "d");

        final Pair<Map<Set<Integer>, Integer>, CompactMMC<String>> result =
                Subgraphs.subgraphView(new CompactMMC.Creator<>(), SubgraphType.HIDE_UNKNOWN_LABELS, contract, inputs);

        final CompactMMC<String> mmc = result.getSecond();

        Assertions.assertThat(mmc.getInputAlphabet()).containsExactlyInAnyOrder("b", "d");
        TestUtils.assertIsRefinementEquivalentTo(mmc, expected, inputs);
    }

    @Test
    public void skipSymbolsTest() throws IOException {
        final CompactMMC<String> contract = TestUtils.loadMMCFromPath("/modal/contract_monitor.dot");
        final CompactMMC<String> expected = TestUtils.loadMMCFromPath("/modal/monitor_skipped.dot");

        final Collection<String> inputs = Arrays.asList("a", "b", "d");

        final Pair<Map<Set<Integer>, Integer>, CompactMMC<String>> result =
                Subgraphs.subgraphView(new CompactMMC.Creator<>(),
                                       SubgraphType.DISREGARD_UNKNOWN_LABELS,
                                       contract,
                                       inputs);

        final CompactMMC<String> mmc = result.getSecond();

        Assertions.assertThat(mmc.getInputAlphabet()).containsExactlyInAnyOrder("a", "b", "d");
        TestUtils.assertIsRefinementEquivalentTo(mmc, expected, inputs);
    }
}
