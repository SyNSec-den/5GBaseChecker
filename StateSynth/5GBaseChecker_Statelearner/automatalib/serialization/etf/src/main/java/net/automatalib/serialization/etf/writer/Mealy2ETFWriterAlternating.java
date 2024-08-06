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
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;

/**
 * Write a Mealy machine with alternating edge semantics.
 * Alternating means that a new edge (and state) is added to the LTS. So, instead of having two labels on one edge,
 * input and output are alternated. Having alternating edge semantics may change the outcomes of temporal formulae.
 *
 * @see <a href="http://rers-challenge.org/2017/index.php?page=problemDescS#">RERS 2017</a>
 *
 * @author Jeroen Meijer
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public final class Mealy2ETFWriterAlternating<I, O> extends AbstractETFWriter<I, MealyMachine<?, I, ?, O>> {

    private static final Mealy2ETFWriterAlternating<?, ?> INSTANCE = new Mealy2ETFWriterAlternating<>();

    /**
     * With alternating edge semantics, there are only edges with one label. Both input and output of the Mealy
     * machine is generalized to a label named 'letter', of type 'letter'.
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
     * Write the specific parts of the ETF for Mealy machines with alternating edge semantics.
     *
     * Writes:
     *  - the initial state,
     *  - the transitions,
     *  - the valuations for the state ids,
     *  - the letters from the alphabet.
     *
     * Note that in this context, the alphabet that is written to ETF is not just the inputs, it is the union of
     * inputs and outputs, of type 'letter'.
     *
     * @param pw the Writer.
     * @param mealy the MealyMachine to write to ETF.
     * @param inputs the alphabet, the input alphabet.
     */
    @Override
    protected void writeETF(PrintWriter pw, MealyMachine<?, I, ?, O> mealy, Alphabet<I> inputs) {
        writeETFInternal(pw, mealy, inputs);
    }

    private <S, T> void writeETFInternal(PrintWriter pw, MealyMachine<S, I, T, O> mealy, Alphabet<I> inputs) {

        // create a bi-mapping from states to integers
        final BiMap<S, Integer> oldStates = HashBiMap.create();
        mealy.getStates().forEach(s -> oldStates.put(s, oldStates.size()));

        // write the initial state, using the bi-map
        pw.println("begin init");
        pw.printf("%d%n", oldStates.get(mealy.getInitialState()));
        pw.println("end init");

        // create a bi-map for transitions containing output
        final BiMap<Pair<O, S>, Integer> outputTransitions = HashBiMap.create();

        // create a bi-map that maps output to integers
        final BiMap<O, Integer> outputIndices = HashBiMap.create();

        /*
         Write the transitions (here is where the horror begins).
         The key to writing transitions with alternating semantics is that one have of see if appropriate
         intermediate states, and output transitions have already been created. If this is the case, that state, and
         output transitions has to be reused.
         */
        pw.println("begin trans");
        for (S s : mealy.getStates()) {
            for (I i : inputs) {
                T t = mealy.getTransition(s, i);
                if (t != null) {
                    final S n = mealy.getSuccessor(t);
                    final O o = mealy.getTransitionOutput(t);

                    // construct a triple that serves as a key in the outputTransitions bi-map
                    final Pair<O, S> outputTransition = Pair.of(o, n);

                    // compute the integer value of the intermediate state (this may be a new state)
                    final Integer intermediateState = outputTransitions.computeIfAbsent(
                            outputTransition, ii -> {

                                // the output may also be a new letter in the alphabet.
                                final Integer outputIndex = outputIndices.computeIfAbsent(
                                        o,
                                        iii -> inputs.size() + outputIndices.size());

                                /*
                                Write the output transition. Note that this will only be done if the output
                                transition was not written before.
                                */
                                final Integer res = oldStates.size() + outputTransitions.size();
                                pw.printf("%d/%d %d%n", res, oldStates.get(n), outputIndex);
                                return res;
                            });

                    // always write the input transition to the output transition
                    pw.printf(
                            "%d/%d %d%n",
                            oldStates.get(s),
                            intermediateState,
                            inputs.getSymbolIndex(i));
                }
            }
        }
        pw.println("end trans");

        // write all state ids, including the newly created intermediate states
        pw.println("begin sort id");
        for (int i = 0; i < oldStates.size(); i++) {
            pw.printf("\"%s\"%n", oldStates.inverse().get(i));
        }

        final Map<Integer, Pair<O, S>> inverseTransitions = outputTransitions.inverse();
        for (int i = 0; i < outputTransitions.size(); i++) {
            final Pair<O, S> t = inverseTransitions.get(oldStates.size() + i);
            assert t != null;
            pw.printf("\"(%s,%s)\"%n", t.getFirst(), t.getSecond());
        }
        pw.println("end sort");

        // write all the letters in the new alphabet
        pw.println("begin sort letter");
        inputs.forEach(i -> pw.printf("\"%s\"%n", i));
        for (int i = 0; i < outputIndices.size(); i++) {
            pw.printf("\"%s\"%n", outputIndices.inverse().get(inputs.size() + i));
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
    public static <I, O> Mealy2ETFWriterAlternating<I, O> getInstance() {
        return (Mealy2ETFWriterAlternating<I, O>) INSTANCE;
    }
}
