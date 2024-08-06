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
package net.automatalib.serialization.taf.writer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import net.automatalib.automata.Automaton;
import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.strings.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class provides methods to write automata in the TAF format.
 *
 * @author Malte Isberner
 */
public final class TAFWriter {

    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    private final Appendable out;
    private int indent;

    private TAFWriter(Appendable out) {
        this.out = out;
    }

    public static <I> void writeAny(FiniteAlphabetAutomaton<?, I, ?> automaton, Appendable out) throws IOException {
        writeAny(automaton, automaton.getInputAlphabet(), out);
    }

    @SuppressWarnings("unchecked")
    public static <I> void writeAny(Automaton<?, I, ?> automaton, Collection<? extends I> inputs, Appendable out)
            throws IOException {
        if (automaton instanceof DFA) {
            writeDFA((DFA<?, I>) automaton, inputs, out);
        } else if (automaton instanceof MealyMachine) {
            writeMealy((MealyMachine<?, I, ?, ?>) automaton, inputs, out);
        } else {
            throw new IllegalArgumentException("Unknown type " + automaton.getClass().getSimpleName());
        }
    }

    public static <I> void dfaToString(DFA<?, I> dfa, Collection<? extends I> inputs) {
        try {
            StringBuilder sb = new StringBuilder();
            writeDFA(dfa, inputs, sb);
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    public static <S, I> void writeDFA(DFA<S, I> dfa, Collection<? extends I> inputs, Appendable out)
            throws IOException {
        TAFWriter writer = new TAFWriter(out);
        writer.doWriteAutomaton(dfa,
                                inputs,
                                "dfa",
                                s -> dfa.isAccepting(s) ? Collections.singleton("accepting") : Collections.emptySet());
    }

    public static <I> void writeDFA(DFA<?, I> dfa, Collection<? extends I> inputs, File out) throws IOException {
        try (Writer bw = IOUtil.asBufferedUTF8Writer(out)) {
            writeDFA(dfa, inputs, bw);
        }
    }

    private <S, I, T, TP> void doWriteAutomaton(UniversalDeterministicAutomaton<S, I, T, ?, TP> automaton,
                                                Collection<? extends I> inputs,
                                                String type,
                                                Function<S, ? extends Collection<? extends String>> spExtractor)
            throws IOException {

        begin(type, inputs);

        S init = automaton.getInitialState();
        StateIDs<S> ids = automaton.stateIDs();
        for (S state : automaton) {
            Set<String> options = new HashSet<>(spExtractor.apply(state));
            if (Objects.equals(init, state)) {
                options.add("initial");
            }
            int id = ids.getStateId(state);
            String name = "s" + id;

            beginState(name, options);

            final Map<Pair<S, TP>, List<I>> groupedTransitions = Maps.newHashMapWithExpectedSize(inputs.size());
            for (I i : inputs) {
                final T t = automaton.getTransition(state, i);

                if (t != null) {
                    final S succ = automaton.getSuccessor(t);
                    final TP tp = automaton.getTransitionProperty(t);
                    final Pair<S, TP> key = Pair.of(succ, tp);

                    groupedTransitions.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
                }
            }

            for (Map.Entry<Pair<S, TP>, List<I>> group : groupedTransitions.entrySet()) {
                S tgt = group.getKey().getFirst();
                int tgtId = ids.getStateId(tgt);
                String tgtName = "s" + tgtId;
                TP transProp = group.getKey().getSecond();
                writeTransition(group.getValue(), tgtName, transProp);
            }

            endState();
        }

        end();
    }

    private void begin(String type, Collection<?> inputs) throws IOException {
        writeIndent();
        out.append(type).append(' ');
        writeStringCollection(inputs);
        out.append(" {").append(System.lineSeparator());
        indent++;
    }

    private void beginState(String name, Set<String> options) throws IOException {
        writeIndent();
        out.append(name).append(' ');
        if (!options.isEmpty()) {
            out.append(options.toString()).append(' ');
        }
        out.append('{').append(System.lineSeparator());
        indent++;
    }

    private void writeTransition(Collection<?> symbols, String target, @Nullable Object output) throws IOException {
        writeIndent();
        writeStringCollection(symbols);
        if (output != null) {
            out.append(" / ").append(StringUtil.enquoteIfNecessary(output.toString()));
        }
        out.append(" -> ").append(target).append(System.lineSeparator());
    }

    private void endState() throws IOException {
        --indent;
        writeIndent();
        out.append('}').append(System.lineSeparator());
    }

    private void end() throws IOException {
        --indent;
        writeIndent();
        out.append('}').append(System.lineSeparator());
    }

    private void writeIndent() throws IOException {
        for (int i = 0; i < indent; i++) {
            out.append('\t');
        }
    }

    private void writeStringCollection(Collection<?> symbols) throws IOException {
        if (symbols.isEmpty()) {
            out.append("{}");
        } else if (symbols.size() == 1) {
            Object sym = symbols.iterator().next();
            StringUtil.enquoteIfNecessary(String.valueOf(sym), out, ID_PATTERN);
        } else {
            out.append('{');
            boolean first = true;
            for (Object sym : symbols) {
                if (first) {
                    first = false;
                } else {
                    out.append(',');
                }
                StringUtil.enquoteIfNecessary(String.valueOf(sym), out, ID_PATTERN);
            }
            out.append('}');
        }
    }

    public static <I> void writeMealy(MealyMachine<?, I, ?, ?> mealy, Collection<? extends I> inputs, File out)
            throws IOException {
        try (Writer bw = IOUtil.asBufferedUTF8Writer(out)) {
            writeMealy(mealy, inputs, bw);
        }
    }

    public static <I> void writeMealy(MealyMachine<?, I, ?, ?> mealy, Collection<? extends I> inputs, Appendable out)
            throws IOException {
        TAFWriter writer = new TAFWriter(out);
        writer.doWriteAutomaton(mealy, inputs, "mealy", s -> Collections.emptySet());
    }

    public static <I> void mealyToString(MealyMachine<?, I, ?, ?> mealy, Collection<? extends I> inputs) {
        try {
            StringBuilder sb = new StringBuilder();
            writeMealy(mealy, inputs, sb);
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

}
