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

import net.automatalib.ts.modal.CompactMMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.words.impl.Alphabets;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MCUtilTest {

    @Test
    public void decomposeTest() throws IOException {
        CompactMMC<String> contract = TestUtils.loadMMCFromPath("/modal/contract_monitor_reference.dot");
        CompactMTS<String> expected = TestUtils.loadMTSFromPath("/modal/context_monitor_reference.dot");

        final ModalTransitionSystem<?, String, ?, ?> context = MCUtil.decompose(contract).contextComponent;
        final Collection<String> inputs = Arrays.asList("b", "d");

        Assertions.assertThat(context.getInputAlphabet()).containsExactlyInAnyOrderElementsOf(inputs);
        TestUtils.assertIsRefinementEquivalentTo(context, expected, inputs);
    }

    @Test
    public void redContext() throws IOException {
        CompactMMC<String> contract = TestUtils.loadMMCFromPath("/modal/monitor_var_red.dot",
                                                                Alphabets.fromArray("a", "b", "c", "d"),
                                                                Alphabets.fromArray("b", "d"));

        final ModalTransitionSystem<?, String, ?, ?> result = MCUtil.generateRedContext(contract);
        Assert.assertNotNull(result);
    }
}
