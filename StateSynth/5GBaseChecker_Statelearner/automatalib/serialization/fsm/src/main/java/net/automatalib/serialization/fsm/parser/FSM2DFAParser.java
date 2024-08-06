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
package net.automatalib.serialization.fsm.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.ModelDeserializer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Parses an FSM to a DFA.
 *
 * @param <I>
 *         the input type
 */
public final class FSM2DFAParser<I> extends AbstractFSMParser<I> implements ModelDeserializer<CompactDFA<I>> {

    // some Exception messages
    public static final String ACCEPT_NOT_FOUND = "accepting state label (%s) not found";
    public static final String ACCEPT_VALUE_NOT_FOUND = "accepting state label value (%s) not found";
    public static final String ACCEPT_INDEX_NOT_FOUND = "index for accepting state label (%d) not found";

    /**
     * The index at which the data definition contains acceptance information.
     */
    private int acceptIndex = -1;

    /**
     * The string index indicating whether the state is accepting.
     */
    private int acceptValue = -1;

    /**
     * A sorted map of states indices, and whether they are accepting.
     */
    private final SortedMap<Integer, Boolean> states = new TreeMap<>();

    /**
     * The map of transitions. Like a {@link net.automatalib.automata.fsa.DFA}, a transition is a pair of a state
     * index, and an input, that maps to a next state index.
     */
    private final Map<Pair<Integer, I>, Integer> transitions = new HashMap<>();

    /**
     * The variable name in the data definition that tells whether the state is accepting.
     */
    private final String acceptingDataVariableName;

    /**
     * The string indicating whether the state is accepting.
     */
    private final String acceptingDataValue;

    /**
     * Constructs an FSM2DFAParser. To parse a DFA use one of the parse() methods.
     *
     * @param targetInputs
     *         An collection containing the inputs which should constitute the input alphabet of the parsed automaton.
     *         If {@code null}, the inputs will be automatically gathered from the read FSM file.
     * @param inputParser
     *         the input parser (see {@link AbstractFSMParser#inputParser}).
     * @param acceptingDataVariableName
     *         the variable name for acceptance (see {@link #acceptingDataVariableName})
     * @param acceptingDataValue
     *         the string for acceptance (see {@link #acceptingDataValue})
     */
    private FSM2DFAParser(@Nullable Collection<? extends I> targetInputs,
                          Function<String, I> inputParser,
                          String acceptingDataVariableName,
                          String acceptingDataValue) {
        super(targetInputs, inputParser);
        this.acceptingDataVariableName = acceptingDataVariableName;
        this.acceptingDataValue = acceptingDataValue;
    }

    /**
     * Parse a data definition.
     * <p>
     * This line is only parsed when we still have to find {@link #acceptIndex}, and {@link #acceptValue}.
     *
     * @throws FSMFormatException
     *         when the data definition is illegal.
     * @throws IOException
     *         see {@link StreamTokenizer#nextToken()}.
     */
    @Override
    protected void parseDataDefinition(StreamTokenizer streamTokenizer) throws IOException {
        if (acceptIndex == -1 && acceptValue == -1) {

            // check we will read an identifier.
            if (streamTokenizer.nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMFormatException(EXPECT_IDENTIFIER, streamTokenizer);
            }

            final String dataVariableName = streamTokenizer.sval;

            if (acceptingDataVariableName.equals(dataVariableName)) {
                acceptIndex = getPartLineNumber();

                // skip a (
                if (streamTokenizer.nextToken() != '(') {
                    throw new FSMFormatException(String.format(EXPECT_CHAR, '('), streamTokenizer);
                }

                // skip a number
                if (streamTokenizer.nextToken() != StreamTokenizer.TT_WORD) {
                    throw new FSMFormatException(EXPECT_NUMBER, streamTokenizer);
                }

                // skip a )
                if (streamTokenizer.nextToken() != ')') {
                    throw new FSMFormatException(String.format(EXPECT_CHAR, ')'), streamTokenizer);
                }

                // skip an identifier
                if (streamTokenizer.nextToken() != StreamTokenizer.TT_WORD) {
                    throw new FSMFormatException(EXPECT_IDENTIFIER, streamTokenizer);
                }

                int dataValueIndex = 0;

                // find the string containing the acceptance information
                while (streamTokenizer.nextToken() == '"' && acceptValue == -1) {
                    final String dataValue = streamTokenizer.sval;
                    if (acceptingDataValue.equals(dataValue)) {
                        acceptValue = dataValueIndex;
                    } else {
                        dataValueIndex++;
                    }
                }
                // push back the EOL, or EOF we accidentally read
                streamTokenizer.pushBack();

                // throw an Exception when the string containing acceptance information is not found in the current line
                if (acceptValue == -1) {
                    throw new FSMFormatException(String.format(ACCEPT_VALUE_NOT_FOUND, acceptingDataValue),
                                                 streamTokenizer);
                }
            }
        }
    }

    /**
     * Checks the data definition by ensuring the index in the state vector containing acceptance information is
     * defined.
     *
     * @throws FSMFormatException
     *         when the acceptance information could not be found.
     */
    @Override
    protected void checkDataDefinitions(StreamTokenizer streamTokenizer) {
        if (acceptIndex == -1) {
            throw new FSMFormatException(String.format(ACCEPT_NOT_FOUND, acceptingDataVariableName),
                                         streamTokenizer);
        }
    }

    /**
     * Parse a state vector.
     * <p>
     * This method will only search for whether the state is accepting or not. The state index will be equal to the
     * current {@link #getPartLineNumber()}.
     *
     * @throws FSMFormatException
     *         when the current line is an illegal state vector.
     * @throws IOException
     *         see {@link StreamTokenizer#nextToken()}.
     */
    @Override
    protected void parseStateVector(StreamTokenizer streamTokenizer) throws IOException {
        Boolean accepting = null;
        for (int i = 0;
             i <= acceptIndex && streamTokenizer.nextToken() == StreamTokenizer.TT_WORD && accepting == null;
             i++) {
            if (i == acceptIndex) {
                try {
                    accepting = acceptValue == Integer.parseInt(streamTokenizer.sval);
                } catch (NumberFormatException nfe) {
                    throw new FSMFormatException(nfe, streamTokenizer);
                }
            }
        }
        if (accepting == null) {
            throw new FSMFormatException(String.format(ACCEPT_INDEX_NOT_FOUND, acceptIndex), streamTokenizer);
        } else {
            states.put(getPartLineNumber(), accepting);
        }
    }

    /**
     * Does nothing.
     */
    @Override
    protected void checkStateVectors(StreamTokenizer streamTokenizer) {}

    /**
     * Parse a transition by searching the current line for the source state, target state and the input.
     *
     * @throws FSMFormatException
     *         when the current line is an illegal transition.
     * @throws IOException
     *         see {@link StreamTokenizer#nextToken()}.
     */
    @Override
    protected void parseTransition(StreamTokenizer streamTokenizer) throws IOException {
        try {
            // check whether we will read a number
            if (streamTokenizer.nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMFormatException(EXPECT_NUMBER, streamTokenizer);
            }

            // read the source state index
            int from = Integer.parseInt(streamTokenizer.sval);

            // check if such a state exists
            if (!states.isEmpty() && !states.containsKey(from)) {
                throw new FSMFormatException(String.format(NO_SUCH_STATE, from), streamTokenizer);
            }

            // check whether we will read a number
            if (streamTokenizer.nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMFormatException(EXPECT_NUMBER, streamTokenizer);
            }

            // read the target state
            int to = Integer.parseInt(streamTokenizer.sval);

            // check if such a state exists
            if (!states.isEmpty() && !states.containsKey(to)) {
                throw new FSMFormatException(String.format(NO_SUCH_STATE, to), streamTokenizer);
            }

            // check we will read a string
            if (streamTokenizer.nextToken() != '"') {
                throw new FSMFormatException(EXPECT_STRING, streamTokenizer);
            }

            // read the input on the transition
            final I input = getInputParser().apply(streamTokenizer.sval);

            // add it to the set of inputs
            getInputs().add(input);

            // add the new transition
            final Integer prev = transitions.put(Pair.of(from, input), to);
            if (prev != null) {
                throw new FSMFormatException(String.format(NON_DETERMINISM_DETECTED, prev), streamTokenizer);
            }
        } catch (NumberFormatException nfe) {
            throw new FSMFormatException(nfe, streamTokenizer);
        }
    }

    /**
     * Do nothing.
     */
    @Override
    protected void checkTransitions(StreamTokenizer streamTokenizer) {
        // Only if no states are defined we add all from the transitions we found.
        // This is necessary because states are not necessarily defined in FSMs.
        if (states.isEmpty()) {
            transitions.keySet().forEach(e -> states.put(e.getFirst(), true));
        }
    }

    /**
     * Constructs the actual {@link net.automatalib.automata.fsa.DFA}.
     *
     * @return the DFA represented by the FSM file.
     *
     * @throws FSMFormatException
     *         see {@link #parse(Reader)}.
     * @throws IOException
     *         see {@link #parse(Reader)}.
     */
    private CompactDFA<I> parseDFA(Reader reader) throws IOException {

        parse(reader);

        final Alphabet<I> alphabet;

        if (targetInputs != null) {
            alphabet = Alphabets.fromCollection(targetInputs);
        } else {
            alphabet = Alphabets.fromCollection(getInputs());
        }

        final CompactDFA<I> dfa = new CompactDFA<>(alphabet);

        // add all the states
        states.forEach((key, value) -> dfa.addState(value));

        // set the initial state (substract one because the StreamTokenizer starts counting at 1
        dfa.setInitialState(states.firstKey() - 1);

        // add all the transitions
        transitions.entrySet()
                   .stream()
                   .filter(e -> alphabet.containsSymbol(e.getKey().getSecond()))
                   .forEach(e -> dfa.addTransition(e.getKey().getFirst() - 1,
                                                   e.getKey().getSecond(),
                                                   e.getValue() - 1));

        // clear our state for next parse call
        states.clear();
        transitions.clear();

        return dfa;
    }

    @Override
    public CompactDFA<I> readModel(InputStream is) throws IOException {
        try (Reader r = IOUtil.asUncompressedBufferedNonClosingUTF8Reader(is)) {
            return parseDFA(r);
        }
    }

    public static <I> FSM2DFAParser<I> getParser(@Nullable Collection<? extends I> targetInputs,
                                                 Function<String, I> inputParser,
                                                 String acceptingDataVariableName,
                                                 String acceptingDataValue) {
        return new FSM2DFAParser<>(targetInputs, inputParser, acceptingDataVariableName, acceptingDataValue);
    }

    public static <I> FSM2DFAParser<I> getParser(Function<String, I> inputParser,
                                                 String acceptingDataVariableName,
                                                 String acceptingDataValue) {
        return getParser(null, inputParser, acceptingDataVariableName, acceptingDataValue);
    }
}
