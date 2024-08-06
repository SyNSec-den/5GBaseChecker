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
package net.automatalib.automata.vpda;

import java.util.List;

import com.google.common.collect.Iterables;
import net.automatalib.automata.concepts.FiniteRepresentation;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.vpda.SEVPAGraphView.SevpaViewEdge;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.VPDAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Interface for k-SEVPAs (k-module single entry visibly push-down automata), a visibly push-down automaton of specific
 * structure and semantics. Additionally -- unless specified other by an implementation -- this interface only accepts
 * well-matched words.
 * <p>
 * For more information on the semantics of VPAs see e.g. "Congruences for Visibly Pushdown Languages" by Alur, Kumar,
 * Madhusudan and Viswanathan.
 *
 * @param <L>
 *         location type
 * @param <I>
 *         input alphabet type
 *
 * @author Malte Isberner
 */
public interface SEVPA<L, I> extends DeterministicAcceptorTS<State<L>, I>,
                                     SuffixOutput<I, Boolean>,
                                     InputAlphabetHolder<I>,
                                     GraphViewable,
                                     FiniteRepresentation {

    @Override
    VPDAlphabet<I> getInputAlphabet();

    int encodeStackSym(L srcLoc, I callSym);

    @Nullable L getInternalSuccessor(L loc, I intSym);

    L getLocation(int id);

    int getLocationId(L loc);

    List<L> getLocations();

    int getNumStackSymbols();

    L getModuleEntry(I callSym);

    @Nullable L getReturnSuccessor(L loc, I retSym, int stackSym);

    @Override
    default Boolean computeOutput(Iterable<? extends I> input) {
        return accepts(input);
    }

    @Override
    default Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
        State<L> state = this.getState(Iterables.concat(prefix, suffix));
        return state != null && isAccepting(state);
    }

    @Override
    default boolean isAccepting(State<L> state) {
        return state.getLocation() != null && isAcceptingLocation(state.getLocation()) &&
               state.getStackContents() == null;
    }

    boolean isAcceptingLocation(L loc);

    @Override
    default State<L> getInitialState() {
        return new State<>(getInitialLocation(), null);
    }

    L getInitialLocation();

    @Override
    default Graph<L, SevpaViewEdge<L, I>> graphView() {
        return new SEVPAGraphView<>(this);
    }
}
