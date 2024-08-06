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
package net.automatalib.serialization.saf;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.words.Alphabet;

/**
 * Serializer for the SAF (simple automaton format).
 *
 * @author frohme
 */
public class SAFOutput {

    private final DataOutput out;

    SAFOutput(OutputStream os) {
        this((DataOutput) new DataOutputStream(os));
    }

    SAFOutput(DataOutput out) {
        this.out = out;
    }

    SAFOutput(File file) throws IOException {
        this(IOUtil.asBufferedOutputStream(file));
    }

    public <I> void writeDFA(DFA<?, I> automaton, Alphabet<I> alphabet) throws IOException {
        writeAutomaton(automaton,
                       alphabet,
                       AutomatonType.DFA,
                       new AcceptanceEncoder(),
                       SinglePropertyEncoder.nullEncoder());
    }

    public <I, SP, TP> void writeAutomaton(UniversalAutomaton<?, I, ?, SP, TP> source,
                                           Alphabet<I> alphabet,
                                           AutomatonType expectedType,
                                           BlockPropertyEncoder<? super SP> spEncoder,
                                           SinglePropertyEncoder<? super TP> tpEncoder) throws IOException {
        writeHeader(expectedType);
        out.writeInt(alphabet.size());
        writeAutomatonBody(source, alphabet, expectedType.isDeterministic(), spEncoder, tpEncoder);
    }

    public void writeHeader(AutomatonType type) throws IOException {
        out.writeByte('S');
        out.writeByte('A');
        out.writeByte('F');
        out.writeByte(type.ordinal());
    }

    private <I, SP, TP> void writeAutomatonBody(UniversalAutomaton<?, I, ?, SP, TP> automaton,
                                                Alphabet<I> alphabet,
                                                boolean deterministic,
                                                BlockPropertyEncoder<? super SP> spDecoder,
                                                SinglePropertyEncoder<? super TP> tpDecoder) throws IOException {

        final int numStates = automaton.size();
        out.writeInt(numStates);

        if (deterministic) {
            encodeBodyDet(automaton, alphabet, spDecoder, tpDecoder);
        } else {
            encodeBodyNondet(automaton, alphabet, spDecoder, tpDecoder);
        }
    }

    private <S, I, SP, TP> void encodeBodyDet(UniversalAutomaton<S, I, ?, SP, TP> result,
                                              Alphabet<I> alphabet,
                                              BlockPropertyEncoder<? super SP> spEncoder,
                                              SinglePropertyEncoder<? super TP> tpEncoder) throws IOException {

        final Set<S> initials = result.getInitialStates();

        if (initials.size() != 1) {
            throw new IllegalArgumentException();
        }

        final List<S> states = new ArrayList<>(result.getStates());
        final S init = initials.iterator().next();

        encodeStatesDet(result, init, states, spEncoder);
        encodeTransitionsDet(result, alphabet, states, tpEncoder);
    }

    private <S, I, SP, TP> void encodeBodyNondet(UniversalAutomaton<S, I, ?, SP, TP> source,
                                                 Alphabet<I> alphabet,
                                                 BlockPropertyEncoder<? super SP> spEncoder,
                                                 SinglePropertyEncoder<? super TP> tpEncoder) throws IOException {

        final List<S> states = new ArrayList<>(source.getStates());
        final Set<S> initials = source.getInitialStates();

        encodeStatesNondet(source, initials, states, spEncoder);
        encodeTransitionsNondet(source, alphabet, states, tpEncoder);

    }

    private <S, SP> void encodeStatesDet(UniversalAutomaton<S, ?, ?, SP, ?> source,
                                         final S init,
                                         final List<S> states,
                                         BlockPropertyEncoder<? super SP> encoder) throws IOException {
        out.writeInt(states.indexOf(init));
        encodeStateProperties(source, states, encoder);
    }

    private <S, I, T, TP> void encodeTransitionsDet(UniversalAutomaton<S, I, T, ?, TP> source,
                                                    Alphabet<I> alphabet,
                                                    List<S> stateList,
                                                    SinglePropertyEncoder<? super TP> tpEncoder) throws IOException {
        for (S state : stateList) {
            for (int j = 0; j < alphabet.size(); j++) {
                final I sym = alphabet.getSymbol(j);

                final Collection<T> succs = source.getTransitions(state, sym);

                if (succs.size() > 1) {
                    throw new IllegalArgumentException("Not deterministic");
                }

                // undefined succ
                if (succs.isEmpty()) {
                    out.writeInt(-1);
                } else {
                    final T succ = succs.iterator().next();
                    final S succState = source.getSuccessor(succ);

                    assert succState != null;

                    final int tgt = stateList.indexOf(succState);

                    out.writeInt(tgt);
                    tpEncoder.writeProperty(out, source.getTransitionProperty(succ));
                }
            }
        }
    }

    private <S, SP> void encodeStatesNondet(UniversalAutomaton<S, ?, ?, SP, ?> source,
                                            final Collection<? extends S> initialStates,
                                            final List<S> states,
                                            BlockPropertyEncoder<? super SP> encoder) throws IOException {
        // 'writeInts'
        out.writeInt(initialStates.size());

        for (final S s : initialStates) {
            out.writeInt(states.indexOf(s));
        }
        // end 'writeInts'

        encodeStateProperties(source, states, encoder);
    }

    private <S, I, T, TP> void encodeTransitionsNondet(UniversalAutomaton<S, I, T, ?, TP> source,
                                                       Alphabet<I> alphabet,
                                                       List<S> stateList,
                                                       SinglePropertyEncoder<? super TP> tpEncoder) throws IOException {
        for (S state : stateList) {
            for (int j = 0; j < alphabet.size(); j++) {
                final I sym = alphabet.getSymbol(j);

                final Collection<T> succs = source.getTransitions(state, sym);

                out.writeInt(succs.size());

                for (final T t : succs) {
                    final S succState = source.getSuccessor(t);
                    out.writeInt(stateList.indexOf(succState));
                    tpEncoder.writeProperty(out, source.getTransitionProperty(t));
                }
            }
        }
    }

    private <S, SP> void encodeStateProperties(UniversalAutomaton<S, ?, ?, SP, ?> source,
                                               List<S> states,
                                               BlockPropertyEncoder<? super SP> encoder) throws IOException {
        encoder.start(out);

        for (final S s : states) {
            encoder.encodeProperty(out, source.getStateProperty(s));
        }

        encoder.finish(out);
    }

    public <I> void writeNFA(NFA<?, I> automaton, Alphabet<I> alphabet) throws IOException {
        writeAutomaton(automaton,
                       alphabet,
                       AutomatonType.NFA,
                       new AcceptanceEncoder(),
                       SinglePropertyEncoder.nullEncoder());
    }
}
