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
package net.automatalib.serialization.etf.writer;

import java.io.OutputStream;
import java.io.PrintWriter;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.words.Alphabet;

/**
 * Write a DFA to ETF.
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type.
 */
public final class DFA2ETFWriter<I> extends AbstractETFWriter<I, DFA<?, I>> {

    private static final DFA2ETFWriter<?> INSTANCE = new DFA2ETFWriter<>();

    /**
     * Writes the type of the edge. A DFA edge contains one label, named 'letter', of type 'letter.
     *
     * @param pw the Writer.
     */
    @Override
    protected void writeEdge(PrintWriter pw) {
        pw.println("begin edge");
        pw.println("letter:letter");
        pw.println("end edge");
    }

    /**
     * Write DFA specific parts in the ETF.
     *
     *  - initial state,
     *  - the valuations for the state 'id',
     *  - the letters in the alphabet,
     *  - the transitions,
     *  - the state labels (rejecting/accepting),
     *  - the mapping from states to state labels.
     *
     * @param pw the Writer.
     * @param dfa the DFA to write.
     * @param inputs the alphabet.
     */
    @Override
    protected void writeETF(PrintWriter pw, DFA<?, I> dfa, Alphabet<I> inputs) {
        writeETFInternal(pw, dfa, inputs);
    }

    private <S> void writeETFInternal(PrintWriter pw, DFA<S, I> dfa, Alphabet<I> inputs) {
        final StateIDs<S> stateIDs = dfa.stateIDs();

        // write the initial state
        final S init = dfa.getInitialState();
        if (init != null) {
            pw.println("begin init");
            pw.printf("%d%n", stateIDs.getStateId(init));
            pw.println("end init");
        }

        // write the valuations of the state ids
        pw.println("begin sort id");
        dfa.getStates().forEach(s -> pw.printf("\"%s\"%n", s));
        pw.println("end sort");

        // write the letters from the alphabet
        pw.println("begin sort letter");
        for (I i : inputs) {
            pw.print("\"");
            pw.print(i);
            pw.println("\"");
        }
        pw.println("end sort");

        // write the transitions
        pw.println("begin trans");
        for (S s : dfa.getStates()) {
            for (I i : inputs) {
                S t = dfa.getSuccessor(s, i);
                if (t != null) {
                    pw.printf(
                            "%d/%d %d%n",
                            stateIDs.getStateId(s),
                            stateIDs.getStateId(t),
                            inputs.getSymbolIndex(i));
                }
            }
        }
        pw.println("end trans");

        // write the two state label valuations
        pw.println("begin sort label");
        pw.println("\"reject\"");
        pw.println("\"accept\"");
        pw.println("end sort");

        // write the state labels for each state, e.g. whether it is accepting/rejecting.
        pw.println("begin map label:label");
        for (S s : dfa.getStates()) {
            final int stateId = stateIDs.getStateId(s);
            pw.printf("%d %d%n", stateId, dfa.isAccepting(s) ? 1 : 0);
        }
        pw.println("end map");
    }

    @Override
    public void writeModel(OutputStream os, DFA<?, I> model, Alphabet<I> alphabet) {
        try (PrintWriter pw = new PrintWriter(IOUtil.asBufferedNonClosingUTF8Writer(os))) {
            write(pw, model, alphabet);
        }
    }

    @SuppressWarnings("unchecked")
    public static <I> DFA2ETFWriter<I> getInstance() {
        return (DFA2ETFWriter<I>) INSTANCE;
    }

}
