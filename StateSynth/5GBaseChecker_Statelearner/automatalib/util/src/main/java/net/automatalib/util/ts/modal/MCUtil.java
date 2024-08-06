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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.ModalContract;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalContractEdgeProperty;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.fsa.MutableDFAs;
import net.automatalib.util.automata.predicates.TransitionPredicates;
import net.automatalib.util.fixpoint.Closures;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public final class MCUtil {

    private MCUtil() {
        // prevent instantiation
    }

    public static <S1, I, T1, B extends MutableModalTransitionSystem<S2, I, T2, TP2>, S2, T2, TP2 extends MutableModalEdgeProperty> SystemComponent<B, S2> systemComponent(
            ModalContract<S1, I, T1, ?> modalContract,
            AutomatonCreator<B, I> creator,
            Function<? super T1, ? extends TP2> tpMapping,
            Supplier<? extends TP2> mayOnlySupplier) {

        B result = creator.createAutomaton(modalContract.getInputAlphabet());

        Mapping<S1, S2> mapping = AutomatonLowLevelCopy.rawCopy(AutomatonCopyMethod.DFS,
                                                                modalContract,
                                                                modalContract.getInputAlphabet(),
                                                                result,
                                                                i -> i,
                                                                sp -> null,
                                                                tpMapping,
                                                                s -> true,
                                                                (s, i, t) -> !modalContract.getTransitionProperty(t)
                                                                                           .isRed());

        S2 uniqueState = result.addState();

        for (S1 state : modalContract.getStates()) {
            for (I input : modalContract.getInputAlphabet()) {
                for (T1 transition : modalContract.getTransitions(state, input)) {
                    if (modalContract.getTransitionProperty(transition).isRed()) {
                        S2 source = mapping.get(state);
                        TP2 property = mayOnlySupplier.get();
                        assert property.isMayOnly();
                        result.addTransition(source, input, uniqueState, property);
                    }
                }
            }
        }

        for (I input : modalContract.getInputAlphabet()) {
            TP2 property = mayOnlySupplier.get();
            assert property.isMayOnly();
            result.addTransition(uniqueState, input, uniqueState, property);
        }

        return SystemComponent.of(result, uniqueState);
    }

    public static <I, B extends ModalTransitionSystem<S2, I, T2, ?>, S2, T2> DFA<?, I> redContextLanguage(
            SystemComponent<B, S2> system,
            Collection<I> inputs) {
        Pair<Map<Set<S2>, Integer>, CompactDFA<I>> res = Closures.simpleClosure(system.systemComponent,
                                                                                inputs,
                                                                                system.systemComponent.getInputAlphabet(),
                                                                                CompactDFA::new,
                                                                                TransitionPredicates.inputIn(inputs));

        CompactDFA<I> dfa = res.getSecond();
        Map<Set<S2>, Integer> mapping = res.getFirst();

        for (Map.Entry<Set<S2>, Integer> entry : mapping.entrySet()) {
            if (entry.getKey().contains(system.uniqueState)) {
                dfa.setStateProperty(entry.getValue(), Boolean.TRUE);
            }
        }

        MutableDFAs.complement(dfa, inputs);
        return Automata.invasiveMinimize(dfa, dfa.getInputAlphabet());
    }

    public static <B extends MutableModalTransitionSystem<S1, I, T, TP>, S1, I, T, TP extends MutableModalEdgeProperty, S2> B redContextComponent(
            DFA<S2, I> dfa,
            AutomatonCreator<B, I> creator,
            Collection<I> inputs,
            Supplier<? extends TP> mayOnlySupplier) {

        B result = creator.createAutomaton(Alphabets.fromCollection(inputs));

        AutomatonLowLevelCopy.rawCopy(AutomatonCopyMethod.STATE_BY_STATE,
                                      dfa,
                                      inputs,
                                      result,
                                      sp -> null,
                                      s -> mayOnlySupplier.get(),
                                      dfa::isAccepting,
                                      (s, i, t) -> true);

        return result;
    }

    public static <S, I, T, TP extends ModalContractEdgeProperty> DFA<?, I> greenContextLanguage(ModalContract<S, I, T, TP> modalContract) {
        Alphabet<I> alphabet = modalContract.getInputAlphabet();
        Alphabet<I> gamma = modalContract.getCommunicationAlphabet();
        assert alphabet.containsAll(gamma);

        Pair<Map<Set<S>, Integer>, CompactDFA<I>> res = Closures.closure(modalContract,
                                                                         gamma,
                                                                         CompactDFA::new,
                                                                         Closures.toClosureOperator(modalContract,
                                                                                                    alphabet,
                                                                                                    (s, i, t) -> !gamma.contains(
                                                                                                            i)),
                                                                         (s, i, t) -> gamma.contains(i) &&
                                                                                      !modalContract.getTransitionProperty(
                                                                                              t).isRed());

        CompactDFA<I> dfa = res.getSecond();
        Map<Set<S>, Integer> mapping = res.getFirst();

        // Use acceptance to remove unnecessary may transitions
        Set<S> keepStates = new HashSet<>();
        for (S src : modalContract.getStates()) {
            for (I input : gamma) {
                for (T t : modalContract.getTransitions(src, input)) {
                    TP property = modalContract.getTransitionProperty(t);
                    if (!property.isRed() && property.isMust()) {
                        keepStates.add(modalContract.getSuccessor(t));
                    }
                }
            }
        }

        for (Entry<Set<S>, Integer> entry : mapping.entrySet()) {
            if (!Collections.disjoint(entry.getKey(), keepStates)) {
                dfa.setStateProperty(entry.getValue(), Boolean.TRUE);
            }
        }

        MutableDFAs.complete(dfa, modalContract.getCommunicationAlphabet());
        Automata.invasiveMinimize(dfa, dfa.getInputAlphabet());

        // Reset acceptance to default semantics of MTS
        Set<Integer> nonAcceptingStates = dfa.getStates()
                                             .stream()
                                             .filter(s -> !dfa.isAccepting(s))
                                             .filter(s -> dfa.getInputAlphabet()
                                                             .stream()
                                                             .allMatch(i -> Objects.equals(dfa.getSuccessor(s, i), s)))
                                             .collect(Collectors.toSet());

        if (nonAcceptingStates.size() > 1) {
            throw new IllegalStateException("Error in minimization: Found identical suffixes with same acceptance");
        }

        for (Integer state : dfa.getStates()) {
            dfa.setAccepting(state, !nonAcceptingStates.contains(state));
        }

        MutableDFAs.complete(dfa, modalContract.getCommunicationAlphabet());
        return Automata.invasiveMinimize(dfa, dfa.getInputAlphabet());
    }

    public static <B extends MutableModalTransitionSystem<S1, I, T, TP>, S1, I, T, TP extends MutableModalEdgeProperty, S2> B greenContextComponent(
            DFA<S2, I> dfa,
            AutomatonCreator<B, I> creator,
            Collection<I> inputs,
            Supplier<? extends TP> mayOnlySupplier,
            Supplier<? extends TP> mustSupplier) {
        B result = creator.createAutomaton(Alphabets.fromCollection(inputs));

        AutomatonLowLevelCopy.rawCopy(AutomatonCopyMethod.STATE_BY_STATE,
                                      dfa,
                                      inputs,
                                      result,
                                      sp -> null,
                                      t -> dfa.isAccepting(dfa.getSuccessor(t)) ?
                                              mustSupplier.get() :
                                              mayOnlySupplier.get(),
                                      s -> true,
                                      TransitionPredicates.alwaysTrue());

        return result;
    }

    public static <I, T> DecompositionResult<ModalTransitionSystem<?, I, ?, ?>, CompactMTS<I>, I, Integer> decompose(
            ModalContract<?, I, T, ?> contract) {

        final SystemComponent<CompactMTS<I>, Integer> systemComponent = MCUtil.systemComponent(contract,
                                                                                               new CompactMTS.Creator<>(),
                                                                                               x -> new ModalEdgePropertyImpl(
                                                                                                       contract.getTransitionProperty(
                                                                                                               x)
                                                                                                               .getModalType()),
                                                                                               () -> new ModalEdgePropertyImpl(
                                                                                                       ModalType.MAY));

        final ModalTransitionSystem<?, I, ?, ?> redContext = generateRedContext(contract, systemComponent);
        final ModalTransitionSystem<?, I, ?, ?> greenContext = generateGreenContext(contract);
        final ModalTransitionSystem<?, I, ?, ?> context = MTSUtil.conjunction(greenContext, redContext);

        return new DecompositionResult<>(context, systemComponent, redContext, greenContext);
    }

    public static <I, T> ModalTransitionSystem<?, I, ?, ?> generateRedContext(ModalContract<?, I, T, ?> contract) {

        final SystemComponent<CompactMTS<I>, Integer> systemComponent = MCUtil.systemComponent(contract,
                                                                                               new CompactMTS.Creator<>(),
                                                                                               x -> new ModalEdgePropertyImpl(
                                                                                                       contract.getTransitionProperty(
                                                                                                               x)
                                                                                                               .getModalType()),
                                                                                               () -> new ModalEdgePropertyImpl(
                                                                                                       ModalType.MAY));

        return generateRedContext(contract, systemComponent);
    }

    public static <S, I, T, B extends ModalTransitionSystem<S, I, T, ?>> ModalTransitionSystem<?, I, ?, ?> generateRedContext(
            ModalContract<?, I, ?, ?> contract,
            SystemComponent<B, S> systemComponent) {

        final DFA<?, I> redLanguage = MCUtil.redContextLanguage(systemComponent, contract.getCommunicationAlphabet());

        return MCUtil.redContextComponent(redLanguage,
                                          new CompactMTS.Creator<>(),
                                          contract.getCommunicationAlphabet(),
                                          () -> new ModalEdgePropertyImpl(ModalType.MAY));
    }

    public static <I> ModalTransitionSystem<?, I, ?, ?> generateGreenContext(ModalContract<?, I, ?, ?> contract) {

        final DFA<?, I> greenLanguage = MCUtil.greenContextLanguage(contract);

        return MCUtil.greenContextComponent(greenLanguage,
                                            new CompactMTS.Creator<>(),
                                            contract.getCommunicationAlphabet(),
                                            () -> new ModalEdgePropertyImpl(ModalType.MAY),
                                            () -> new ModalEdgePropertyImpl(ModalType.MUST));
    }

    public static final class SystemComponent<A extends ModalTransitionSystem<S, ?, ?, ?>, S> {

        public final A systemComponent;
        public final S uniqueState;

        public SystemComponent(A systemComponent, S uniqueState) {
            this.systemComponent = systemComponent;
            this.uniqueState = uniqueState;
        }

        public static <A extends ModalTransitionSystem<S, I, T, TP>, S, I, T, TP extends ModalEdgeProperty> SystemComponent<A, S> of(
                A graph,
                S state) {
            return new SystemComponent<>(graph, state);
        }
    }

    public static class DecompositionResult<CT extends ModalTransitionSystem<?, I, ?, ?>, IT extends ModalTransitionSystem<S2, ?, ?, ?>, I, S2> {

        public final CT contextComponent;
        public final SystemComponent<IT, S2> systemComponent;
        public final CT redContext;
        public final CT greenContext;

        public DecompositionResult(CT contextComponent,
                                   SystemComponent<IT, S2> systemComponent,
                                   CT redContext,
                                   CT greenContext) {
            this.contextComponent = contextComponent;
            this.systemComponent = systemComponent;
            this.redContext = redContext;
            this.greenContext = greenContext;
        }
    }
}
