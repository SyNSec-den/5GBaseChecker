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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.words.Alphabet;

/**
 * Write a Mealy machine with straightforward IO semantics.
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public final class Mealy2ETFWriterIO<I, O> extends AbstractETFWriter<I, MealyMachine<?, I, ?, O>> {

    private static final Mealy2ETFWriterIO<?, ?> INSTANCE = new Mealy2ETFWriterIO<>();

    /**
     * Write the edge type. An edge has two edge labels: input of type input, and output of type output.
     *
     * @param pw the Writer.
     */
    @Override
    protected void writeEdge(PrintWriter pw) {
        pw.println("begin edge");
        pw.println("input:input");
        pw.println("output:output");
        pw.println("end edge");
    }

    /**
     * Write ETF parts specific for Mealy machines with IO semantics.
     *
     * Writes:
     *  - the initial state,
     *  - the valuations for the state ids,
     *  - the transitions,
     *  - the input alphabet (for the input labels on edges),
     *  - the output alphabet (for the output labels on edges).
     *
     * @param pw the Writer.
     * @param mealy the Mealy machine to write.
     * @param inputs the alphabet.
     */
    @Override
    protected void writeETF(PrintWriter pw, MealyMachine<?, I, ?, O> mealy, Alphabet<I> inputs) {
        writeETFInternal(pw, mealy, inputs);
    }

    private <S, T> void writeETFInternal(PrintWriter pw, MealyMachine<S, I, T, O> mealy, Alphabet<I> inputs) {
        final StateIDs<S> stateIDs = mealy.stateIDs();

        // write the initial state
        final S init = mealy.getInitialState();
        if (init != null) {
            pw.println("begin init");
            pw.printf("%d%n", stateIDs.getStateId(init));
            pw.println("end init");
        }

        // write the state ids
        pw.println("begin sort id");
        mealy.getStates().forEach(s -> pw.printf("\"%s\"%n", s));
        pw.println("end sort");

        // create a new bi-map that contains indices for the output alphabet
        final BiMap<O, Integer> outputIndices = HashBiMap.create();

        // write the transitions
        pw.println("begin trans");
        for (S s : mealy.getStates()) {
            for (I i : inputs) {
                final T t = mealy.getTransition(s, i);
                if (t != null) {
                    final O o = mealy.getTransitionOutput(t);
                    outputIndices.computeIfAbsent(o, ii -> outputIndices.size());
                    final S n = mealy.getSuccessor(t);
                    pw.printf("%s/%s %d %d%n",
                              stateIDs.getStateId(s),
                              stateIDs.getStateId(n),
                              inputs.getSymbolIndex(i),
                              outputIndices.get(o));
                }
            }
        }
        pw.println("end trans");

        // write the letters in the input alphabet
        pw.println("begin sort input");
        inputs.forEach(i -> pw.printf("\"%s\"%n", i));
        pw.println("end sort");

        // write the letters in the output alphabet
        pw.println("begin sort output");
        for (int i = 0; i < outputIndices.size(); i++) {
            pw.printf("\"%s\"%n", outputIndices.inverse().get(i));
        }
        pw.println("end sort");
    }

    @Override
    public void writeModel(OutputStream os, MealyMachine<?, I, ?, O> model, Alphabet<I> alphabet) {
        try (PrintWriter pw = new PrintWriter(IOUtil.asBufferedNonClosingUTF8Writer(os))) {
            write(pw, model, alphabet);
        }
    }

    @SuppressWarnings("unchecked")
    public static <I, O> Mealy2ETFWriterIO<I, O> getInstance() {
        return (Mealy2ETFWriterIO<I, O>) INSTANCE;
    }
}
