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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.fixpoint.Worksets;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.graphs.Graphs;
import net.automatalib.util.graphs.sssp.SSSPResult;
import net.automatalib.util.ts.modal.Subgraphs.SubgraphType;
import net.automatalib.words.impl.Alphabets;

/**
 * @author msc
 * @author frohme
 */
public final class MTSUtil {

    private MTSUtil() {
        // prevent instantiation
    }

    public static <S0, S1, I> CompactMTS<I> conjunction(ModalTransitionSystem<S0, I, ?, ?> mts0,
                                                        ModalTransitionSystem<S1, I, ?, ?> mts1) {
        return conjunction(mts0, mts1, CompactMTS::new);
    }

    public static <A extends MutableModalTransitionSystem<S, I, T, ?>, S, S0, S1, I, T> A conjunction(
            ModalTransitionSystem<S0, I, ?, ?> mts0,
            ModalTransitionSystem<S1, I, ?, ?> mts1,
            AutomatonCreator<A, I> creator) {
        return conjunctionWithMapping(mts0, mts1, creator).getSecond();
    }

    public static <A extends MutableModalTransitionSystem<S, I, T, ?>, S, S0, S1, I, T> Pair<Map<Pair<S0, S1>, S>, A> conjunctionWithMapping(
            ModalTransitionSystem<S0, I, ?, ?> mts0,
            ModalTransitionSystem<S1, I, ?, ?> mts1,
            AutomatonCreator<A, I> creator) {
        return Worksets.map(new ModalConjunction<>(mts0, mts1, creator));
    }

    public static <S0, S1, I> CompactMTS<I> compose(ModalTransitionSystem<S0, I, ?, ?> mts0,
                                                    ModalTransitionSystem<S1, I, ?, ?> mts1) {
        return compose(mts0, mts1, CompactMTS::new);
    }

    public static <A extends MutableModalTransitionSystem<S, I, ?, ?>, S, S0, S1, I> A compose(ModalTransitionSystem<S0, I, ?, ?> mts0,
                                                                                               ModalTransitionSystem<S1, I, ?, ?> mts1,
                                                                                               AutomatonCreator<A, I> creator) {
        return composeWithMapping(mts0, mts1, creator).getSecond();
    }

    public static <A extends MutableModalTransitionSystem<S, I, ?, ?>, S, S0, S1, I> Pair<Map<Pair<S0, S1>, S>, A> composeWithMapping(
            ModalTransitionSystem<S0, I, ?, ?> mts0,
            ModalTransitionSystem<S1, I, ?, ?> mts1,
            AutomatonCreator<A, I> creator) {
        return Worksets.map(new ModalParallelComposition<>(mts0, mts1, creator));
    }

    public static <AS, BS, I> boolean isRefinementOf(ModalTransitionSystem<AS, I, ?, ?> a,
                                                     ModalTransitionSystem<BS, I, ?, ?> b,
                                                     Collection<I> input) {

        final Set<Pair<AS, BS>> refinement = ModalRefinement.refinementRelation(a, b, input);

        final Set<AS> statesA = new HashSet<>(a.getInitialStates());
        final Set<BS> statesB = new HashSet<>(b.getInitialStates());

        for (Pair<AS, BS> p : refinement) {
            statesA.remove(p.getFirst());
            statesB.remove(p.getSecond());
        }

        return statesA.isEmpty() && statesB.isEmpty();
    }

    public static <S, I> Set<S> reachableSubset(UniversalFiniteAlphabetAutomaton<S, I, ?, ?, ?> ts,
                                                Collection<I> inputs,
                                                Set<S> states) {
        Pair<Map<Set<S>, Integer>, CompactDFA<I>> graphView =
                Subgraphs.subgraphView(new CompactDFA.Creator<>(), SubgraphType.DISREGARD_UNKNOWN_LABELS, ts, inputs);

        CompactDFA<I> dfa = graphView.getSecond();
        Integer init = dfa.getInitialState();
        assert init != null;

        SSSPResult<Integer, ?> ssspResult = Graphs.findSSSP(dfa.transitionGraphView(), init, e -> 1);

        HashSet<S> reachableStates = new HashSet<>();
        for (Map.Entry<Set<S>, Integer> entry : graphView.getFirst().entrySet()) {
            Set<S> reachableSubset = Sets.intersection(states, entry.getKey());
            if (!reachableSubset.isEmpty() &&
                ssspResult.getShortestPathDistance(entry.getValue()) != Graphs.INVALID_DISTANCE) {
                reachableStates.addAll(reachableSubset);
            }
        }

        return reachableStates;
    }

    public static <S, I, T> ModalTransitionSystem<?, I, ?, ?> toLTS(ModalTransitionSystem<S, I, T, ?> mts,
                                                                    TransitionPredicate<S, I, T> transFilter) {
        return toLTS(mts, transFilter, Function.identity());
    }

    public static <S, I, T> ModalTransitionSystem<?, I, ?, ?> toLTS(ModalTransitionSystem<S, I, T, ?> mts,
                                                                    TransitionPredicate<S, I, T> transFilter,
                                                                    Function<I, I> inputMapping) {

        CompactMTS<I> result = new CompactMTS<>(Alphabets.fromList(mts.getInputAlphabet()
                                                                      .stream()
                                                                      .map(inputMapping)
                                                                      .collect(Collectors.toList())));

        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS,
                                   mts,
                                   mts.getInputAlphabet(),
                                   result,
                                   inputMapping,
                                   sp -> null,
                                   tp -> new ModalEdgePropertyImpl(ModalType.MUST),
                                   sf -> true,
                                   transFilter);

        return result;
    }
}
