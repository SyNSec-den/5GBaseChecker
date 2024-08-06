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
package net.automatalib.incremental.dfa.dag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.IntDisjointSets;
import net.automatalib.commons.util.UnionFind;
import net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.helper.DelegateVisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractIncrementalDFADAGBuilder<I> extends AbstractIncrementalDFABuilder<I> {

    protected final Map<StateSignature, State> register = new HashMap<>();
    protected final State init;
    protected State sink;

    public AbstractIncrementalDFADAGBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
        StateSignature sig = new StateSignature(alphabetSize, Acceptance.DONT_KNOW);
        this.init = new State(sig);
        register.put(sig, init);
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!this.inputAlphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(this.inputAlphabet).addSymbol(symbol);
        }

        final int newAlphabetSize = this.inputAlphabet.size();
        // even if the symbol was already in the alphabet, we need to make sure to be able to store the new symbol
        if (alphabetSize < newAlphabetSize) {
            register.values().forEach(n -> n.ensureInputCapacity(newAlphabetSize));
            alphabetSize = newAlphabetSize;
        }
    }

    @Override
    public @Nullable Word<I> findSeparatingWord(DFA<?, I> target,
                                                Collection<? extends I> inputs,
                                                boolean omitUndefined) {
        return doFindSeparatingWord(target, inputs, omitUndefined);
    }

    private <S> @Nullable Word<I> doFindSeparatingWord(DFA<S, I> target,
                                                       Collection<? extends I> inputs,
                                                       boolean omitUndefined) {
        int thisStates = register.size();
        Map<State, Integer> stateIds = new HashMap<>();
        if (sink != null) {
            stateIds.put(sink, 0);
            thisStates++;
        }
        int targetStates = target.size();
        if (!omitUndefined) {
            targetStates++;
        }

        S init2 = target.getInitialState();

        if (init2 == null && omitUndefined) {
            return null;
        }

        State init1 = init;

        boolean acc = init2 != null && target.isAccepting(init2);
        if (init1.getAcceptance().conflicts(acc)) {
            return Word.epsilon();
        }

        IntDisjointSets uf = new UnionFind(thisStates + targetStates);
        StateIDs<S> tgtIds = target.stateIDs();
        int id1 = getStateId(init1, stateIds);
        int id2 = (init2 != null ? tgtIds.getStateId(init2) : targetStates - 1) + thisStates;

        uf.link(id1, id2);

        Queue<Record<@Nullable S, I>> queue = new ArrayDeque<>();

        queue.add(new Record<>(init1, init2));

        I lastSym = null;

        Record<@Nullable S, I> current;

        explore:
        while ((current = queue.poll()) != null) {
            State state1 = current.state1;
            @Nullable S state2 = current.state2;

            for (I sym : inputs) {
                @Nullable S succ2 = (state2 != null) ? target.getSuccessor(state2, sym) : null;
                if (succ2 == null && omitUndefined) {
                    continue;
                }

                int idx = inputAlphabet.getSymbolIndex(sym);
                State succ1 = state1 != sink ? state1.getSuccessor(idx) : sink;

                if (succ1 == null) {
                    continue;
                }

                id1 = getStateId(succ1, stateIds);
                id2 = (succ2 != null ? tgtIds.getStateId(succ2) : targetStates - 1) + thisStates;

                int r1 = uf.find(id1), r2 = uf.find(id2);

                if (r1 == r2) {
                    continue;
                }

                if (succ1 == sink) {
                    if (succ2 == null) {
                        continue;
                    }
                    if (target.isAccepting(succ2)) {
                        lastSym = sym;
                        break explore;
                    }
                } else {
                    boolean succ2acc = succ2 != null && target.isAccepting(succ2);
                    if (succ1.getAcceptance().conflicts(succ2acc)) {
                        lastSym = sym;
                        break explore;
                    }
                }

                uf.link(r1, r2);

                queue.add(new Record<>(succ1, succ2, sym, current));
            }
        }

        if (current == null) {
            return null;
        }

        int ceLength = current.depth;
        if (lastSym != null) {
            ceLength++;
        }

        @SuppressWarnings("nullness") // we make sure to set each index to a value of type I
        WordBuilder<I> wb = new WordBuilder<>(null, ceLength);

        int index = ceLength;

        if (lastSym != null) {
            wb.setSymbol(--index, lastSym);
        }

        while (current.reachedFrom != null) {
            final I reachedVia = current.reachedVia;
            wb.setSymbol(--index, reachedVia);
            current = current.reachedFrom;
        }

        return wb.toWord();
    }

    private static int getStateId(State s, Map<State, Integer> idMap) {
        Integer id = idMap.get(s);
        if (id != null) {
            return id;
        }
        id = idMap.size();
        idMap.put(s, id);
        return id;
    }

    protected abstract @Nullable State getState(Word<? extends I> word);

    protected void updateInitSignature(Acceptance acc) {
        StateSignature sig = init.getSignature();
        sig.acceptance = acc;
    }

    protected void updateInitSignature(int idx, State succ) {
        StateSignature sig = init.getSignature();
        State oldSucc = sig.successors.array[idx];
        if (oldSucc == succ) {
            return;
        }
        if (oldSucc != null) {
            oldSucc.decreaseIncoming();
        }
        sig.successors.array[idx] = succ;
        succ.increaseIncoming();
    }

    protected void updateInitSignature(Acceptance acc, int idx, State succ) {
        StateSignature sig = init.getSignature();
        State oldSucc = sig.successors.array[idx];
        Acceptance oldAcc = sig.acceptance;
        if (oldSucc == succ && oldAcc == acc) {
            return;
        }
        if (oldSucc != null) {
            oldSucc.decreaseIncoming();
        }
        sig.successors.array[idx] = succ;
        succ.increaseIncoming();
        sig.acceptance = acc;
    }

    /**
     * Updates the signature for a given state.
     *
     * @param state
     *         the state
     * @param acc
     *         the new acceptance value
     *
     * @return the canonical state for the updated signature
     */
    protected State updateSignature(State state, Acceptance acc) {
        assert state != init;
        StateSignature sig = state.getSignature();
        if (sig.acceptance == acc) {
            return state;
        }
        register.remove(sig);
        sig.acceptance = acc;
        sig.updateHashCode();
        return replaceOrRegister(state);
    }

    /**
     * Updates the signature for a given state.
     *
     * @param state
     *         the state
     * @param idx
     *         the index of the transition to change
     * @param succ
     *         the new successor for the above index
     *
     * @return the canonical state for the updated signature
     */
    protected State updateSignature(State state, int idx, State succ) {
        assert state != init;

        StateSignature sig = state.getSignature();
        if (sig.successors.array[idx] == succ) {
            return state;
        }
        register.remove(sig);
        if (sig.successors.array[idx] != null) {
            sig.successors.array[idx].decreaseIncoming();
        }

        sig.successors.array[idx] = succ;
        succ.increaseIncoming();
        sig.updateHashCode();
        return replaceOrRegister(state);
    }

    protected State updateSignature(State state, Acceptance acc, int idx, State succ) {
        assert state != init;

        StateSignature sig = state.getSignature();
        if (sig.successors.array[idx] == succ && sig.acceptance == acc) {
            return state;
        }
        register.remove(sig);
        sig.successors.array[idx] = succ;
        sig.acceptance = acc;
        succ.increaseIncoming();
        sig.updateHashCode();
        return replaceOrRegister(state);
    }

    /**
     * Returns the canonical state for the given state's signature, or registers the state as canonical if no state with
     * that signature exists.
     *
     * @param state
     *         the state
     *
     * @return the canonical state for the given state's signature
     */
    protected State replaceOrRegister(State state) {
        StateSignature sig = state.getSignature();
        State other = register.get(sig);
        if (other != null) {
            if (state != other) {
                for (int i = 0; i < sig.successors.array.length; i++) {
                    State succ = sig.successors.array[i];
                    if (succ != null) {
                        succ.decreaseIncoming();
                    }
                }
            }
            return other;
        }

        register.put(sig, state);
        return state;
    }

    /**
     * Returns (and possibly creates) the canonical state for the given signature.
     *
     * @param sig
     *         the signature
     *
     * @return the canonical state for the given signature
     */
    protected State replaceOrRegister(StateSignature sig) {
        State state = register.get(sig);
        if (state != null) {
            return state;
        }

        state = new State(sig);
        register.put(sig, state);
        for (int i = 0; i < sig.successors.array.length; i++) {
            State succ = sig.successors.array[i];
            if (succ != null) {
                succ.increaseIncoming();
            }
        }
        return state;
    }

    protected State hiddenClone(State other) {
        assert other != init;

        StateSignature sig = other.getSignature().duplicate();
        for (int i = 0; i < alphabetSize; i++) {
            State succ = sig.successors.array[i];
            if (succ != null) {
                succ.increaseIncoming();
            }
        }
        return new State(sig);
    }

    protected void hide(State state) {
        assert state != init;

        StateSignature sig = state.getSignature();
        register.remove(sig);
    }

    protected State unhide(State state, Acceptance acc, int idx, State succ) {
        assert state != init;

        StateSignature sig = state.getSignature();
        sig.acceptance = acc;
        State prevSucc = sig.successors.array[idx];
        if (prevSucc != null) {
            prevSucc.decreaseIncoming();
        }
        sig.successors.array[idx] = succ;
        if (succ != null) {
            succ.increaseIncoming();
        }
        sig.updateHashCode();

        return replaceOrRegister(state);
    }

    protected State unhide(State state, int idx, State succ) {
        assert state != init;

        StateSignature sig = state.getSignature();
        State prevSucc = sig.successors.array[idx];
        if (prevSucc != null) {
            prevSucc.decreaseIncoming();
        }
        sig.successors.array[idx] = succ;
        if (succ != null) {
            succ.increaseIncoming();
        }
        sig.updateHashCode();

        return replaceOrRegister(state);
    }

    /**
     * Clones a state, changing the signature.
     *
     * @param other
     *         the state to clone
     * @param acc
     *         the new acceptance value
     *
     * @return the canonical state for the derived signature
     */
    protected State clone(State other, Acceptance acc) {
        assert other != init;

        StateSignature sig = other.getSignature();
        if (sig.acceptance == acc) {
            return other;
        }
        sig = sig.duplicate();
        sig.acceptance = acc;
        sig.updateHashCode();
        return replaceOrRegister(sig);
    }

    /**
     * Clones a state, changing the signature.
     *
     * @param other
     *         the state to clone
     * @param idx
     *         the index of the transition to change
     * @param succ
     *         the new successor state
     *
     * @return the canonical state for the derived signature
     */
    protected State clone(State other, int idx, State succ) {
        assert other != init;

        StateSignature sig = other.getSignature();
        if (sig.successors.array[idx] == succ) {
            return other;
        }
        sig = sig.duplicate();
        sig.successors.array[idx] = succ;
        sig.updateHashCode();
        return replaceOrRegister(sig);
    }

    protected State clone(State other, Acceptance acc, int idx, State succ) {
        assert other != init;

        StateSignature sig = other.getSignature();
        if (sig.successors.array[idx] == succ && sig.acceptance == acc) {
            return other;
        }
        sig = sig.duplicate();
        sig.successors.array[idx] = succ;
        sig.acceptance = acc;
        return replaceOrRegister(sig);
    }

    @Override
    public GraphView asGraph() {
        return new GraphView();
    }

    @Override
    public TransitionSystemView asTransitionSystem() {
        return new TransitionSystemView();
    }

    private static final class Record<S, I> {

        public final State state1;
        public final S state2;
        public final I reachedVia;
        public final @Nullable Record<S, I> reachedFrom;
        public final int depth;

        @SuppressWarnings("nullness") // we will only access reachedVia after checking reachedFrom for null
        Record(State state1, S state2) {
            this.state1 = state1;
            this.state2 = state2;
            this.reachedVia = null;
            this.reachedFrom = null;
            this.depth = 0;
        }

        Record(State state1, S state2, I reachedVia, Record<S, I> reachedFrom) {
            this.state1 = state1;
            this.state2 = state2;
            this.reachedVia = reachedVia;
            this.reachedFrom = reachedFrom;
            this.depth = reachedFrom.depth + 1;
        }
    }

    public class GraphView extends AbstractGraphView<I, State, EdgeRecord> {

        @Override
        public int size() {
            return register.size() + ((sink == null) ? 0 : 1);
        }

        @Override
        public Collection<State> getNodes() {
            if (sink == null) {
                return Collections.unmodifiableCollection(register.values());
            }
            List<State> result = new ArrayList<>(register.size() + 1);
            result.addAll(register.values());
            result.add(sink);
            return result;
        }

        @Override
        public Collection<EdgeRecord> getOutgoingEdges(State node) {
            if (node.isSink()) {
                return Collections.emptySet();
            }
            StateSignature sig = node.getSignature();
            List<EdgeRecord> result = new ArrayList<>();
            for (int i = 0; i < alphabetSize; i++) {
                if (sig.successors.array[i] != null) {
                    result.add(new EdgeRecord(node, i));
                }
            }
            return result;
        }

        @Override
        public State getTarget(EdgeRecord edge) {
            int idx = edge.transIdx;
            return edge.source.getSuccessor(idx);
        }

        @Override
        public I getInputSymbol(EdgeRecord edge) {
            return inputAlphabet.getSymbol(edge.transIdx);
        }

        @Override
        public Acceptance getAcceptance(State node) {
            return node.getAcceptance();
        }

        @Override
        public State getInitialNode() {
            return init;
        }

        @Override
        public VisualizationHelper<State, EdgeRecord> getVisualizationHelper() {
            return new DelegateVisualizationHelper<State, EdgeRecord>(super.getVisualizationHelper()) {

                private int id;

                @Override
                public boolean getNodeProperties(State node, Map<String, String> properties) {
                    if (!super.getNodeProperties(node, properties)) {
                        return false;
                    }
                    properties.put(NodeAttrs.LABEL, "n" + (id++));
                    if (node.isConfluence()) {
                        String shape = (node.getAcceptance() == Acceptance.TRUE) ?
                                NodeShapes.DOUBLEOCTAGON :
                                NodeShapes.OCTAGON;
                        properties.put(NodeAttrs.SHAPE, shape);
                    }
                    return true;
                }
            };
        }
    }

    public class TransitionSystemView extends AbstractTransitionSystemView<State, I, State> {

        @Override
        public State getSuccessor(State transition) {
            return transition;
        }

        @Override
        public State getTransition(State state, I input) {
            if (state == sink) {
                return state;
            }
            int idx = inputAlphabet.getSymbolIndex(input);
            return state.getSuccessor(idx);
        }

        @Override
        public State getInitialState() {
            return init;
        }

        @Override
        public Acceptance getAcceptance(State state) {
            return state.getAcceptance();
        }
    }

}
