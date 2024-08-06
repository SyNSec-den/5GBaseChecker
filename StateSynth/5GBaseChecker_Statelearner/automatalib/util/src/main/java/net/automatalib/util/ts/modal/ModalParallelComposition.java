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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.fixpoint.WorksetMappingAlgorithm;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;

/**
 * @author msc
 */
class ModalParallelComposition<A extends MutableModalTransitionSystem<S, I, ?, ?>, S, S0, S1, I, T0, T1, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty>
        implements WorksetMappingAlgorithm<Pair<S0, S1>, S, A> {

    private static final float LOAD_FACTOR = 0.5f;

    private final ModalTransitionSystem<S0, I, T0, TP0> mts0;
    private final ModalTransitionSystem<S1, I, T1, TP1> mts1;

    private final A result;

    ModalParallelComposition(ModalTransitionSystem<S0, I, T0, TP0> mts0,
                             ModalTransitionSystem<S1, I, T1, TP1> mts1,
                             AutomatonCreator<A, I> output) {

        this.mts0 = mts0;
        this.mts1 = mts1;

        final Alphabet<I> alphabet;

        if (mts0.getInputAlphabet().equals(mts1.getInputAlphabet())) {
            alphabet = mts0.getInputAlphabet();
        } else {
            final GrowingAlphabet<I> growingAlphabet = new GrowingMapAlphabet<>(mts0.getInputAlphabet());
            growingAlphabet.addAll(mts1.getInputAlphabet());
            alphabet = growingAlphabet;
        }

        result = output.createAutomaton(alphabet);

    }

    @Override
    public int expectedElementCount() {
        return (int) (LOAD_FACTOR * mts0.size() * mts1.size());
    }

    @Override
    public Collection<Pair<S0, S1>> initialize(Map<Pair<S0, S1>, S> mapping) {
        Collection<Pair<S0, S1>> initialElements =
                new ArrayList<>(mts0.getInitialStates().size() * mts1.getInitialStates().size());
        for (final S0 s0 : mts0.getInitialStates()) {
            for (final S1 s1 : mts1.getInitialStates()) {
                final Pair<S0, S1> init = Pair.of(s0, s1);
                final S newState = result.addInitialState();

                mapping.put(init, newState);
                initialElements.add(init);
            }
        }
        return initialElements;
    }

    @Override
    public Collection<Pair<S0, S1>> update(Map<Pair<S0, S1>, S> mapping, Pair<S0, S1> currentTuple) {
        List<Pair<S0, S1>> discovered = new ArrayList<>();
        List<TransitionData<S0, S1, I>> transitions = generateNewTransitions(currentTuple);

        for (TransitionData<S0, S1, I> transition : transitions) {

            S mappedTarget;
            if (mapping.containsKey(transition.target)) {
                mappedTarget = mapping.get(transition.target);
            } else {
                mappedTarget = result.addState();
                mapping.put(transition.target, mappedTarget);
                discovered.add(transition.target);
            }
            result.addModalTransition(mapping.get(currentTuple), transition.label, mappedTarget, transition.property);
        }
        return discovered;
    }

    protected List<TransitionData<S0, S1, I>> generateNewTransitions(Pair<S0, S1> productState) {
        List<TransitionData<S0, S1, I>> newTransitions = new ArrayList<>();

        for (I symbol : mts0.getInputAlphabet()) {
            for (T0 transition : mts0.getTransitions(productState.getFirst(), symbol)) {

                if (!mts1.getInputAlphabet().contains(symbol)) {
                    newTransitions.add(new TransitionData<>(symbol,
                                                            Pair.of(mts0.getSuccessor(transition),
                                                                    productState.getSecond()),
                                                            mts0.getTransitionProperty(transition).getModalType()));
                } else {
                    for (T1 partnerTransition : mts1.getTransitions(productState.getSecond(), symbol)) {
                        newTransitions.add(new TransitionData<>(symbol,
                                                                Pair.of(mts0.getSuccessor(transition),
                                                                        mts1.getSuccessor(partnerTransition)),
                                                                minimalCompatibleType(mts0.getTransitionProperty(
                                                                        transition).getModalType(),
                                                                                      mts1.getTransitionProperty(
                                                                                              partnerTransition)
                                                                                          .getModalType())));
                    }
                }
            }
        }

        Set<I> alphabetDifference = new HashSet<>(mts1.getInputAlphabet());
        alphabetDifference.removeAll(mts0.getInputAlphabet());

        for (I symbol : alphabetDifference) {
            for (T1 transition : mts1.getTransitions(productState.getSecond(), symbol)) {
                newTransitions.add(new TransitionData<>(symbol,
                                                        Pair.of(productState.getFirst(), mts1.getSuccessor(transition)),
                                                        mts1.getTransitionProperty(transition).getModalType()));
            }
        }

        return newTransitions;
    }

    private static ModalEdgeProperty.ModalType minimalCompatibleType(ModalType arg0, ModalType arg1) {
        if (arg0 == ModalType.MUST && arg1 == ModalType.MUST) {
            return ModalType.MUST;
        } else {
            return ModalType.MAY;
        }
    }

    @Override
    public A result() {
        return result;
    }

    private static class TransitionData<S0, S1, I> {

        private final I label;
        private final Pair<S0, S1> target;
        private final ModalType property;

        TransitionData(I label, Pair<S0, S1> target, ModalType property) {
            this.label = label;
            this.target = target;
            this.property = property;
        }
    }

}
