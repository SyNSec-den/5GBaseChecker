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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import net.automatalib.words.SPAAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represent a {@link ContextFreeModalProcessSystem}-based view on the instrumented language of a given
 * {@link SPA}, which allows to model-check language properties of an {@link SPA} with tools such as M3C.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class CFMPSView<I> implements ContextFreeModalProcessSystem<I, Void> {

    private final SPA<?, I> spa;
    private final Map<I, ProceduralModalProcessGraph<?, I, ?, Void, ?>> pmpgs;

    public CFMPSView(SPA<?, I> spa) {
        this.spa = spa;

        final SPAAlphabet<I> spaAlphabet = spa.getInputAlphabet();
        final Map<I, DFA<?, I>> procedures = spa.getProcedures();
        this.pmpgs = Maps.newHashMapWithExpectedSize(procedures.size());
        for (Entry<I, DFA<?, I>> e : procedures.entrySet()) {
            this.pmpgs.put(e.getKey(), new MPGView<>(spaAlphabet, e.getKey(), e.getValue()));
        }
    }

    @Override
    public Map<I, ProceduralModalProcessGraph<?, I, ?, Void, ?>> getPMPGs() {
        return this.pmpgs;
    }

    @Override
    public @Nullable I getMainProcess() {
        return this.spa.getInitialProcedure();
    }

    private static class MPGView<S, I>
            implements ProceduralModalProcessGraph<S, I, SPAEdge<I, S>, Void, ProceduralModalEdgeProperty> {

        private static final Object INIT = new Object();
        private static final Object END = new Object();

        private final SPAAlphabet<I> spaAlphabet;
        private final I procedure;
        private final DFA<S, I> dfa;
        private final S dfaInit;

        private final S init;
        private final S end;

        // we make sure to handle 'init' and 'end' correctly
        @SuppressWarnings("unchecked")
        MPGView(SPAAlphabet<I> spaAlphabet, I procedure, DFA<S, I> dfa) {
            this.spaAlphabet = spaAlphabet;
            this.procedure = procedure;
            this.dfa = dfa;

            final S dfaInit = this.dfa.getInitialState();
            if (dfaInit == null) {
                throw new IllegalArgumentException("Empty DFAs cannot be mapped to ModalProcessGraphs");
            }
            this.dfaInit = dfaInit;

            this.init = (S) INIT;
            this.end = (S) END;
        }

        @Override
        public Collection<SPAEdge<I, S>> getOutgoingEdges(S node) {
            if (node == init) {
                return Collections.singletonList(new SPAEdge<>(this.procedure, this.dfaInit, ProceduralType.INTERNAL));
            } else if (node == end) {
                return Collections.emptyList();
            } else {
                final List<SPAEdge<I, S>> result = new ArrayList<>(this.spaAlphabet.size());

                for (I i : this.spaAlphabet.getInternalAlphabet()) {
                    final S succ = this.dfa.getSuccessor(node, i);
                    if (succ != null) {
                        result.add(new SPAEdge<>(i, succ, ProceduralType.INTERNAL));
                    }
                }

                for (I i : this.spaAlphabet.getCallAlphabet()) {
                    final S succ = this.dfa.getSuccessor(node, i);
                    if (succ != null) {
                        result.add(new SPAEdge<>(i, succ, ProceduralType.PROCESS));
                    }
                }

                if (this.dfa.isAccepting(node)) {
                    result.add(new SPAEdge<>(this.spaAlphabet.getReturnSymbol(),
                                             this.getFinalNode(),
                                             ProceduralType.INTERNAL));
                }

                return result;
            }
        }

        @Override
        public S getTarget(SPAEdge<I, S> edge) {
            return edge.succ;
        }

        @Override
        public Collection<S> getNodes() {
            final List<S> nodes = new ArrayList<>(dfa.size() + 2);
            nodes.add(this.init);
            nodes.add(this.end);
            nodes.addAll(dfa.getStates());
            return nodes;
        }

        @Override
        public Set<Void> getNodeProperty(S node) {
            return Collections.emptySet();
        }

        @Override
        public ProceduralModalEdgeProperty getEdgeProperty(SPAEdge<I, S> edge) {
            return edge;
        }

        @Override
        public I getEdgeLabel(SPAEdge<I, S> edge) {
            return edge.input;
        }

        @Override
        public S getFinalNode() {
            return this.end;
        }

        @Override
        public S getInitialNode() {
            return this.init;
        }
    }

    private static class SPAEdge<I, S> implements ProceduralModalEdgeProperty {

        private final I input;
        private final S succ;
        private final ProceduralType type;

        SPAEdge(I input, S succ, ProceduralType type) {
            this.input = input;
            this.succ = succ;
            this.type = type;
        }

        @Override
        public ModalType getModalType() {
            return ModalType.MUST;
        }

        @Override
        public ProceduralType getProceduralType() {
            return this.type;
        }
    }

}
