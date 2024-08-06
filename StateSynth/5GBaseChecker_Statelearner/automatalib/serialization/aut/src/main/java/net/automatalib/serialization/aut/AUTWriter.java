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
package net.automatalib.serialization.aut;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.words.Alphabet;

/**
 * A utility class that exports automata to the AUT format (see http://cadp.inria.fr/man/aut.html for further
 * information).
 *
 * @author frohme
 */
public final class AUTWriter {

    private AUTWriter() {
        // prevent instantiation
    }

    public static <S, I> void writeAutomaton(SimpleAutomaton<S, I> automaton, Alphabet<I> alphabet, OutputStream os)
            throws IOException {
        writeAutomaton(automaton, alphabet, str -> "\"" + str + "\"", os);
    }

    public static <S, I> void writeAutomaton(SimpleAutomaton<S, I> automaton,
                                             Alphabet<I> alphabet,
                                             Function<I, String> inputTransformer,
                                             OutputStream os) throws IOException {

        final Set<TransitionTriple<S, I>> transitions = new HashSet<>();

        for (final S s : automaton.getStates()) {
            for (final I i : alphabet) {
                final Set<S> succs = automaton.getSuccessors(s, i);

                if (!succs.isEmpty()) {
                    for (final S succ : succs) {
                        transitions.add(new TransitionTriple<>(s, i, succ));
                    }

                }
            }
        }

        try (Writer w = IOUtil.asBufferedNonClosingUTF8Writer(os)) {
            writeHeader(automaton, transitions, w);
            writeTransitions(automaton, transitions, inputTransformer, w);
        }
    }

    private static <S, I> void writeHeader(SimpleAutomaton<S, I> automaton,
                                           Set<TransitionTriple<S, I>> transitions,
                                           Appendable appendable) throws IOException {

        final Set<S> inits = automaton.getInitialStates();

        if (inits.size() != 1) {
            throw new IllegalArgumentException("Automaton needs to exactly specify a single initial state");
        }

        final S init = inits.iterator().next();

        final StateIDs<S> stateIds = automaton.stateIDs();

        appendable.append("des (");
        appendable.append(Integer.toString(stateIds.getStateId(init)));
        appendable.append(", ");
        appendable.append(Integer.toString(transitions.size()));
        appendable.append(", ");
        appendable.append(Integer.toString(automaton.size()));
        appendable.append(')');
        appendable.append(System.lineSeparator());
    }

    private static <S, I> void writeTransitions(SimpleAutomaton<S, I> automaton,
                                                Set<TransitionTriple<S, I>> transitions,
                                                Function<I, String> inputTransformer,
                                                Appendable appendable) throws IOException {

        final StateIDs<S> stateIds = automaton.stateIDs();

        for (final TransitionTriple<S, I> trans : transitions) {
            appendable.append('(');
            appendable.append(Integer.toString(stateIds.getStateId(trans.src)));
            appendable.append(", ");
            appendable.append(inputTransformer.apply(trans.input));
            appendable.append(", ");
            appendable.append(Integer.toString(stateIds.getStateId(trans.dest)));
            appendable.append(')');
            appendable.append(System.lineSeparator());
        }
    }

    private static class TransitionTriple<S, I> {

        final S src;
        final I input;
        final S dest;

        TransitionTriple(S src, I input, S dest) {
            this.src = src;
            this.input = input;
            this.dest = dest;
        }
    }

}
