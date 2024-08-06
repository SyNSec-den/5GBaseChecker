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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import net.automatalib.ts.modal.CompactMMC;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class MTSUtilTest {

    @Test
    public void testCut() throws IOException {
        final CompactMMC<String> contract = TestUtils.loadMMCFromPath("/modal/contract_monitor.dot");

        final Set<Integer> reachableSubset =
                MTSUtil.reachableSubset(contract, contract.getInputAlphabet(), ImmutableSet.of(0, 1, 5));

        Assertions.assertThat(reachableSubset).isSubsetOf(0, 1, 5);
    }

    @Test
    public void testCutEmptyStates() throws IOException {
        final CompactMMC<String> contract = TestUtils.loadMMCFromPath("/modal/contract_monitor.dot");

        final Set<Integer> reachableSubset =
                MTSUtil.reachableSubset(contract, contract.getInputAlphabet(), Collections.emptySet());

        Assertions.assertThat(reachableSubset).isEmpty();
    }

    @Test
    public void testCutEmptyLabels() throws IOException {
        final CompactMMC<String> contract = TestUtils.loadMMCFromPath("/modal/contract_monitor.dot");

        final Set<Integer> reachableSubset =
                MTSUtil.reachableSubset(contract, Collections.emptyList(), new HashSet<>(contract.getStates()));

        Assertions.assertThat(reachableSubset).containsExactlyInAnyOrder(0);
    }

    @Test
    public void testCut1() throws IOException {
        // very fragile since DOTParser does not preserve state ids
        final CompactMMC<String> input = TestUtils.loadMMCFromPath("/modal/contract_incomplete.dot");

        final Set<Integer> reachableSubset = MTSUtil.reachableSubset(input,
                                                                     Arrays.asList("a", "b", "d"),
                                                                     ImmutableSet.of(1, 3, 4, 6, 8, 12, 15, 18));

        Assertions.assertThat(reachableSubset).containsExactlyInAnyOrder(1, 3, 8, 12, 18);
    }

    @Test
    public void testCut2() throws IOException {
        // very fragile since DOTParser does not preserve state ids
        final CompactMMC<String> input = TestUtils.loadMMCFromPath("/modal/contract_monitor.dot");

        final Set<Integer> reachableSubset =
                MTSUtil.reachableSubset(input, Collections.singleton("d"), ImmutableSet.of(0, 1, 2, 3));

        Assertions.assertThat(reachableSubset).containsExactlyInAnyOrder(0);
    }
}
