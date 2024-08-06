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
package net.automatalib.automata.spa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.visualization.SPAVisualizationHelper;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.Triple;
import net.automatalib.graphs.Graph;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Graph representation of an {@link SPA} that displays all states of its sub-procedures once, i.e. without
 * incorporating execution semantics such as stack contents.
 *
 * @param <S>
 *         common procedural state type
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class SPAGraphView<S, I> implements Graph<Pair<I, S>, Triple<I, I, S>> {

    private final Alphabet<I> callAlphabet;
    private final Collection<I> proceduralAlphabet;
    private final Map<I, DFA<S, I>> subModels;

    // cast is fine, because we make sure to only query states belonging to the respective procedures
    @SuppressWarnings("unchecked")
    public SPAGraphView(Alphabet<I> callAlphabet,
                        Collection<I> proceduralAlphabet,
                        Map<I, ? extends DFA<? extends S, I>> subModels) {
        this.callAlphabet = callAlphabet;
        this.proceduralAlphabet = proceduralAlphabet;
        this.subModels = (Map<I, DFA<S, I>>) subModels;
    }

    @Override
    public Collection<Pair<I, S>> getNodes() {
        final List<Pair<I, S>> result = new ArrayList<>();

        for (final Map.Entry<I, DFA<S, I>> e : subModels.entrySet()) {
            final I procedure = e.getKey();
            for (S s : e.getValue()) {
                result.add(Pair.of(procedure, s));
            }
        }

        return result;
    }

    @Override
    public Collection<Triple<I, I, S>> getOutgoingEdges(Pair<I, S> node) {
        final I procedure = node.getFirst();
        final S state = node.getSecond();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which procedures exist
        final @NonNull DFA<S, I> subModel = subModels.get(procedure);

        final List<Triple<I, I, S>> result = new ArrayList<>(this.proceduralAlphabet.size());

        for (final I i : this.proceduralAlphabet) {
            final S next = subModel.getSuccessor(state, i);

            if (next != null) {
                result.add(Triple.of(procedure, i, next));
            }
        }

        return result;
    }

    @Override
    public Pair<I, S> getTarget(Triple<I, I, S> edge) {
        return Pair.of(edge.getFirst(), edge.getThird());
    }

    @Override
    public VisualizationHelper<Pair<I, S>, Triple<I, I, S>> getVisualizationHelper() {
        return new SPAVisualizationHelper<>(callAlphabet, subModels);
    }

}
