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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.fixpoint.WorksetMappingAlgorithm;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.util.graphs.traversal.DFSVisitor;
import net.automatalib.util.graphs.traversal.GraphTraversal;

/**
 * @author msc
 */
class ModalConjunction<A extends MutableModalTransitionSystem<S, I, T, ?>, S, S0, S1, I, T, T0, T1, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty>
        implements WorksetMappingAlgorithm<Pair<S0, S1>, S, A> {

    private static final float LOAD_FACTOR = 0.5f;

    private final ModalTransitionSystem<S0, I, T0, TP0> mts0;
    private final ModalTransitionSystem<S1, I, T1, TP1> mts1;

    private final A result;

    ModalConjunction(ModalTransitionSystem<S0, I, T0, TP0> mts0,
                     ModalTransitionSystem<S1, I, T1, TP1> mts1,
                     AutomatonCreator<A, I> creator) {

        if (!mts0.getInputAlphabet().equals(mts1.getInputAlphabet())) {
            throw new IllegalArgumentException("conjunction input mts have to have the same input alphabet");
        }

        this.mts0 = mts0;
        this.mts1 = mts1;

        this.result = creator.createAutomaton(mts0.getInputAlphabet());
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
    public Collection<Pair<S0, S1>> update(Map<Pair<S0, S1>, S> mapping, Pair<S0, S1> currentStatePair) {
        S mappedState = mapping.get(currentStatePair);
        assert mappedState != null;

        ArrayList<Pair<S0, S1>> discovered = new ArrayList<>();

        for (I sym : mts0.getInputAlphabet()) {

            Collection<T0> transitions0 = mts0.getTransitions(currentStatePair.getFirst(), sym);
            Collection<T1> transitions1 = mts1.getInputAlphabet().containsSymbol(sym) ?
                    mts1.getTransitions(currentStatePair.getSecond(), sym) :
                    Collections.emptySet();

            for (T0 transition0 : transitions0) {

                if (mts0.getTransitionProperty(transition0).isMust() && transitions1.isEmpty()) {
                    throw new IllegalConjunctionException(String.format(
                            "Error in conjunction: States <%s,%s> for label=%s with outgoing transitions t0=%s, t1=%s. " +
                            "Error for transition %s (t0), leading trace: %s",
                            currentStatePair.getFirst(),
                            currentStatePair.getSecond(),
                            sym,
                            transitions0,
                            transitions1,
                            transition0,
                            traceError(mts0, transition0)));
                }
                for (T1 transition1 : transitions1) {

                    Pair<S0, S1> newTuple = Pair.of(mts0.getSuccessor(transition0), mts1.getSuccessor(transition1));

                    S newState = mapping.get(newTuple);
                    if (newState == null) {
                        newState = result.addState();
                        mapping.put(newTuple, newState);
                        discovered.add(newTuple);
                    }

                    T newT = result.createTransition(newState);
                    result.addTransition(mappedState, sym, newT);

                    if (mts0.getTransitionProperty(transition0).isMust() ||
                        mts1.getTransitionProperty(transition1).isMust()) {
                        result.getTransitionProperty(newT).setMust();
                    } else {
                        result.getTransitionProperty(newT).setMayOnly();
                    }
                }
            }
        }

        for (I sym : mts1.getInputAlphabet()) {

            Collection<T0> transitions0 = mts0.getInputAlphabet().containsSymbol(sym) ?
                    mts0.getTransitions(currentStatePair.getFirst(), sym) :
                    Collections.emptySet();
            Collection<T1> transitions1 = mts1.getTransitions(currentStatePair.getSecond(), sym);

            for (T1 transition1 : transitions1) {

                if (mts1.getTransitionProperty(transition1).isMust() && transitions0.isEmpty()) {
                    throw new IllegalConjunctionException(String.format(
                            "Error in conjunction: States <%s,%s> for label=%s with outgoing transitions t0=%s, t1=%s. " +
                            "Error for transition %s (t1), leading trace: %s",
                            currentStatePair.getFirst(),
                            currentStatePair.getSecond(),
                            sym,
                            transitions0,
                            transitions1,
                            transition1,
                            traceError(mts1, transition1)));
                }
            }
        }
        return discovered;
    }

    @Override
    public A result() {
        return result;
    }

    protected static <S, I, T, TP extends ModalEdgeProperty> String traceError(ModalTransitionSystem<S, I, T, TP> mts,
                                                                               T transition) {
        EdgeTracer<S, I, T> finder = new EdgeTracer<>(transition);

        GraphTraversal.dfs(mts.transitionGraphView(mts.getInputAlphabet()), mts.getInitialStates(), finder);

        StringBuilder sb = new StringBuilder();
        for (S state : finder.getStateSequence()) {
            sb.append(state);
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static class EdgeTracer<S, I, T> implements DFSVisitor<S, TransitionEdge<I, T>, Void> {

        final Deque<S> stateStack;
        final T targetTransition;
        boolean freeze;

        EdgeTracer(T transition) {
            targetTransition = transition;
            stateStack = new ArrayDeque<>();
            freeze = false;
        }

        @Override
        public Void initialize(S node) {
            return null;
        }

        @Override
        public void explore(S node, Void data) {
            if (!freeze) {
                stateStack.push(node);
            }
        }

        @Override
        public void finish(S node, Void data) {
            if (!freeze) {
                S top = stateStack.pop();
                assert top == node;
            }
        }

        public void testEdge(TransitionEdge<I, T> edge) {
            if (edge.getTransition() == targetTransition) {
                freeze = true;
            }
        }

        public List<S> getStateSequence() {
            return new ArrayList<>(stateStack);
        }

        @Override
        public Void treeEdge(S srcNode, Void srcData, TransitionEdge<I, T> edge, S tgtNode) {
            testEdge(edge);
            return null;
        }

        @Override
        public void backEdge(S srcNode, Void srcData, TransitionEdge<I, T> edge, S tgtNode, Void tgtData) {
            testEdge(edge);
        }

        @Override
        public void crossEdge(S srcNode, Void srcData, TransitionEdge<I, T> edge, S tgtNode, Void tgtData) {
            testEdge(edge);
        }

        @Override
        public void forwardEdge(S srcNode, Void srcData, TransitionEdge<I, T> edge, S tgtNode, Void tgtData) {
            testEdge(edge);
        }

        @Override
        public void backtrackEdge(S srcNode, Void srcDate, TransitionEdge<I, T> edge, S tgtNode, Void tgtData) {
            testEdge(edge);
        }
    }
}
