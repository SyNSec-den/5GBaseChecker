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

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.transition.ModalContractEdgeProperty.EdgeColor;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import net.automatalib.util.ts.modal.MCUtil.SystemComponent;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MCUtilMonitorTest {

    private CompactMC<Character> monitor;

    @BeforeClass
    public void setUp() {
        monitor = new CompactMC<>(Alphabets.characters('a', 'd'), Alphabets.fromArray('b', 'd'));
        Integer s0 = monitor.addInitialState();
        Integer s1 = monitor.addState();
        Integer s2 = monitor.addState();
        Integer s3 = monitor.addState();

        monitor.addContractTransition(s0, 'd', s0, ModalType.MUST, false, EdgeColor.RED);
        monitor.addContractTransition(s0, 'a', s1, ModalType.MUST, false, EdgeColor.NONE);
        monitor.addContractTransition(s1, 'b', s2, ModalType.MUST, false, EdgeColor.GREEN);
        monitor.addContractTransition(s2, 'c', s3, ModalType.MUST, false, EdgeColor.NONE);
        monitor.addContractTransition(s3, 'd', s0, ModalType.MUST, false, EdgeColor.GREEN);
    }

    @Test
    public void testSystemComponent() {
        SystemComponent<CompactMTS<Character>, Integer> system = MCUtil.systemComponent(monitor,
                                                                                        CompactMTS::new,
                                                                                        t -> null,
                                                                                        () -> new ModalEdgePropertyImpl(
                                                                                                ModalType.MAY));

        Assertions.assertThat(system.systemComponent.getInitialStates()).hasSize(1);

        Integer m0 = system.systemComponent.getInitialStates().iterator().next();

        Assertions.assertThat(system.systemComponent.getTransitions(m0, 'd'))
                  .hasSize(1)
                  .allMatch(t -> t.getProperty().isMayOnly());

        Assertions.assertThat(system.systemComponent.getTransitions(m0, 'a'))
                  .hasSize(1)
                  .allMatch(t -> t.getProperty().isMust());
    }

    @Test
    public void testRedContextLanguage() {
        SystemComponent<CompactMTS<Character>, Integer> system = MCUtil.systemComponent(monitor,
                                                                                        CompactMTS::new,
                                                                                        t -> null,
                                                                                        () -> new ModalEdgePropertyImpl(
                                                                                                ModalType.MAY));

        DFA<?, Character> dfa = MCUtil.redContextLanguage(system, monitor.getCommunicationAlphabet());

        Assertions.assertThatThrownBy(() -> dfa.accepts(Lists.charactersOf("ab")))
                  .isInstanceOf(IllegalArgumentException.class);

        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bb")), "accept \"bb\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bdb")), "accept \"bdb\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("d")), "do not accept \"d\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("dbbdd")), "do not accept \"dbbdd\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bbdddbd")), "accept \"bbdddbd\"");

    }

    @Test
    public void testRedContextComponent() {
        SystemComponent<CompactMTS<Character>, Integer> system = MCUtil.systemComponent(monitor,
                                                                                        CompactMTS::new,
                                                                                        t -> null,
                                                                                        () -> new ModalEdgePropertyImpl(
                                                                                                ModalType.MAY));

        DFA<?, Character> dfa = MCUtil.redContextLanguage(system, monitor.getCommunicationAlphabet());

        CompactMTS<Character> redContext = MCUtil.redContextComponent(dfa,
                                                                      CompactMTS::new,
                                                                      monitor.getCommunicationAlphabet(),
                                                                      () -> new ModalEdgePropertyImpl(ModalType.MAY));

        Assertions.assertThat(allTransitions(redContext, monitor.getCommunicationAlphabet()))
                  .hasSize(5)
                  .allMatch(t -> t.getProperty().isMayOnly());

        Set<Integer> inits = redContext.getInitialStates();
        Set<Integer> bSuccs = redContext.getSuccessors(inits, Word.fromSymbols('b'));
        Set<Integer> bbSuccs = redContext.getSuccessors(inits, Word.fromCharSequence("bb"));
        Set<Integer> bdSuccs = redContext.getSuccessors(inits, Word.fromCharSequence("bd"));

        Assertions.assertThat(inits).hasSize(1);
        Assertions.assertThat(bSuccs).hasSize(1);
        Assertions.assertThat(bbSuccs).hasSize(1);
        Assertions.assertThat(bdSuccs).hasSize(1);

        int init = inits.iterator().next(); // s0 (without invasive minimization)
        int b = bSuccs.iterator().next(); // s1 (without invasive minimization)
        int bb = bbSuccs.iterator().next(); // s2 (without invasive minimization)
        int bd = bdSuccs.iterator().next(); // s0 (without invasive minimization)

        // the following tests require fixed state-labels
        Assertions.assertThat(allOutgoingTransitions(redContext, monitor.getCommunicationAlphabet(), init))
                  .hasSize(1)
                  .allMatch(t -> t.getTarget() == b);

        Assertions.assertThat(allOutgoingTransitions(redContext, monitor.getCommunicationAlphabet(), b))
                  .hasSize(2)
                  .anyMatch(t -> t.getTarget() == bb)
                  .anyMatch(t -> t.getTarget() == bd);

        Assertions.assertThat(allOutgoingTransitions(redContext, monitor.getCommunicationAlphabet(), bb))
                  .hasSize(2)
                  .allMatch(t -> t.getTarget() == bb);
    }

    @Test
    public void testGreenContextLanguage() {
        DFA<?, Character> dfa = MCUtil.greenContextLanguage(monitor);

        Assertions.assertThatThrownBy(() -> dfa.accepts(Lists.charactersOf("ab")))
                  .isInstanceOf(IllegalArgumentException.class);

        Assert.assertTrue(dfa.accepts(Lists.charactersOf("")), "accept \"\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("b")), "accept \"b\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bd")), "accept \"bd\"");
        Assert.assertTrue(dfa.accepts(Lists.charactersOf("bdb")), "accept \"bdb\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("d")), "do not accept \"d\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("dbbdd")), "do not accept \"dbbdd\"");
        Assert.assertFalse(dfa.accepts(Lists.charactersOf("bbdddbd")), "do not accept \"bbdddbd\"");
    }

    @Test
    public void testGreenContextComponent() {
        DFA<?, Character> dfa = MCUtil.greenContextLanguage(monitor);

        CompactMTS<Character> greenContext = MCUtil.greenContextComponent(dfa,
                                                                          CompactMTS::new,
                                                                          monitor.getCommunicationAlphabet(),
                                                                          () -> new ModalEdgePropertyImpl(ModalType.MAY),
                                                                          () -> new ModalEdgePropertyImpl(ModalType.MUST));

        Assertions.assertThat(greenContext.getInputAlphabet())
                  .containsExactlyInAnyOrderElementsOf(monitor.getCommunicationAlphabet());

        Assertions.assertThat(allTransitions(greenContext, monitor.getCommunicationAlphabet())).hasSize(6);

        Assertions.assertThat(greenContext.getInitialStates()).hasSize(1);

        Integer initial = greenContext.getInitialStates().iterator().next();

        Assertions.assertThat(greenContext.getTransitions(initial, 'd'))
                  .allMatch(t -> t.getProperty().isMayOnly())
                  .hasSize(1);

        Integer sink = greenContext.getTransitions(initial, 'd').iterator().next().getTarget();

        Assertions.assertThat(allOutgoingTransitions(greenContext, monitor.getCommunicationAlphabet(), sink))
                  .allMatch(t -> t.getProperty().isMayOnly())
                  .hasSize(2);

        Assertions.assertThat(allIncomingTransitions(greenContext, monitor.getCommunicationAlphabet(), sink))
                  .allMatch(t -> t.getProperty().isMayOnly())
                  .hasSize(4);
    }

    private static <S, I, T, SP, TP> Set<T> allOutgoingTransitions(UniversalAutomaton<S, I, T, SP, TP> ts,
                                                                   Collection<I> inputs,
                                                                   S state) {
        Set<T> transitions = Sets.newHashSetWithExpectedSize(inputs.size());
        for (I symbol : inputs) {
            transitions.addAll(ts.getTransitions(state, symbol));
        }
        return transitions;
    }

    private static <S, I, T, SP, TP> Set<T> allIncomingTransitions(UniversalAutomaton<S, I, T, SP, TP> ts,
                                                                   Collection<I> inputs,
                                                                   S state) {
        Set<T> transitions = Sets.newHashSetWithExpectedSize(inputs.size());
        for (S s : ts.getStates()) {
            for (I symbol : inputs) {
                ts.getTransitions(s, symbol)
                  .stream()
                  .filter(t -> state.equals(ts.getSuccessor(t)))
                  .forEachOrdered(transitions::add);
            }
        }
        return transitions;
    }

    private static <S, I, T, SP, TP> Set<T> allTransitions(UniversalAutomaton<S, I, T, SP, TP> ts,
                                                           Collection<I> inputs) {
        Set<T> transitions = Sets.newHashSetWithExpectedSize(ts.size() * inputs.size());
        for (S state : ts.getStates()) {
            for (I symbol : inputs) {
                transitions.addAll(ts.getTransitions(state, symbol));
            }
        }
        return transitions;
    }
}