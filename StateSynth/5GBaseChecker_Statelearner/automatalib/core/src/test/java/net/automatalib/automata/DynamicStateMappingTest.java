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
package net.automatalib.automata;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.util.TestUtil;
import net.automatalib.commons.util.mappings.MutableMapping;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class DynamicStateMappingTest {

    @Test
    public void testDeterministic() {
        testAutomaton(TestUtil.constructMealy());
    }

    @Test
    public void testNonDeterministic() {
        testAutomaton(TestUtil.constructNFA());
    }

    private static <S, I, T, SP, TP> void testAutomaton(ShrinkableAutomaton<S, I, T, SP, TP> automaton) {

        final List<S> states = new ArrayList<>(automaton.getStates());

        final S s0 = states.get(0);
        final S s1 = states.get(1);
        final S s2 = states.get(2);

        final MutableMapping<S, Integer> mapping = automaton.createDynamicStateMapping();

        mapping.put(s0, 0);
        mapping.put(s1, 1);
        mapping.put(s2, 2);

        automaton.removeState(s0);

        Assert.assertNull(mapping.get(s0));
        Assert.assertEquals(mapping.get(s1).intValue(), 1);
        Assert.assertEquals(mapping.get(s2).intValue(), 2);

        automaton.removeState(s1, s2);

        Assert.assertNull(mapping.get(s0));
        Assert.assertNull(mapping.get(s1));
        Assert.assertEquals(mapping.get(s2).intValue(), 2);

        automaton.removeState(s2);

        Assert.assertNull(mapping.get(s0));
        Assert.assertNull(mapping.get(s1));
        Assert.assertNull(mapping.get(s2));

        final S s3 = automaton.addState();
        mapping.put(s3, 3);
        Assert.assertEquals(mapping.get(s3).intValue(), 3);
    }
}
