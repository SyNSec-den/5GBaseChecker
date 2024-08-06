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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.vpda.SEVPAGraphView.SevpaViewEdge;
import net.automatalib.graphs.Graph;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.VPDAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SEVPAGraphView<L, I> implements Graph<L, SevpaViewEdge<L, I>> {

    private final SEVPA<L, I> sevpa;
    private final VPDAlphabet<I> alphabet;

    public SEVPAGraphView(SEVPA<L, I> sevpa) {
        this.sevpa = sevpa;
        this.alphabet = sevpa.getInputAlphabet();
    }

    @Override
    public Collection<L> getNodes() {
        return Collections.unmodifiableCollection(sevpa.getLocations());
    }

    @Override
    public Collection<SevpaViewEdge<L, I>> getOutgoingEdges(final L location) {

        final List<SevpaViewEdge<L, I>> result = new ArrayList<>();

        // all call transitions
        for (final I i : alphabet.getCallAlphabet()) {
            final L succ = sevpa.getModuleEntry(i);
            if (succ != null) {
                result.add(new SevpaViewEdge<>(i, succ));
            }
        }

        // all internal transitions
        for (final I i : alphabet.getInternalAlphabet()) {
            final L succ = sevpa.getInternalSuccessor(location, i);
            if (succ != null) {
                result.add(new SevpaViewEdge<>(i, succ));
            }
        }

        // all return transitions for every possible stack contents
        for (final I i : alphabet.getReturnAlphabet()) {
            for (final L loc : sevpa.getLocations()) {
                for (final I stackSymbol : alphabet.getCallAlphabet()) {
                    final int sym = sevpa.encodeStackSym(loc, stackSymbol);
                    final L succ = sevpa.getReturnSuccessor(location, i, sym);

                    if (succ != null) {
                        result.add(new SevpaViewEdge<>(i, succ, sevpa.getLocationId(loc), stackSymbol));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public L getTarget(final SevpaViewEdge<L, I> edge) {
        return edge.target;
    }

    @Override
    public VisualizationHelper<L, SevpaViewEdge<L, I>> getVisualizationHelper() {
        return new DefaultVisualizationHelper<L, SevpaViewEdge<L, I>>() {

            @Override
            protected Collection<L> initialNodes() {
                return Collections.singleton(sevpa.getInitialLocation());
            }

            @Override
            public boolean getNodeProperties(final L node, final Map<String, String> properties) {
                super.getNodeProperties(node, properties);

                properties.put(NodeAttrs.SHAPE,
                               sevpa.isAcceptingLocation(node) ? NodeShapes.DOUBLECIRCLE : NodeShapes.CIRCLE);
                properties.put(NodeAttrs.LABEL, "L" + sevpa.getLocationId(node));

                return true;
            }

            @Override
            public boolean getEdgeProperties(final L src,
                                             final SevpaViewEdge<L, I> edge,
                                             final L tgt,
                                             final Map<String, String> properties) {

                final I input = edge.input;

                if (alphabet.isReturnSymbol(input)) {
                    properties.put(EdgeAttrs.LABEL, input + "/(L" + edge.callLocId + ',' + edge.callSymbol + ')');
                } else {
                    properties.put(EdgeAttrs.LABEL, String.valueOf(input));
                }

                return true;
            }
        };
    }

    public static class SevpaViewEdge<S, I> {

        public final I input;
        public final S target;

        public final int callLocId;
        public final @Nullable I callSymbol;

        SevpaViewEdge(I internalAction, S target) {
            this(internalAction, target, -1, null);
        }

        SevpaViewEdge(I returnAction, S target, int callLocId, @Nullable I callSymbol) {
            this.input = returnAction;
            this.target = target;
            this.callLocId = callLocId;
            this.callSymbol = callSymbol;
        }

    }
}
