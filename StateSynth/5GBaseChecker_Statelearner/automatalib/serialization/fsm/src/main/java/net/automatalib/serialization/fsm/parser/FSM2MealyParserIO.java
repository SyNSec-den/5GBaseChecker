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
import java.io.StreamTokenizer;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.automatalib.commons.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Parse a Mealy machine from an FSM source, with straightforward edge semantics.
 *
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 *
 * @author Jeroen Meijer
 */
public final class FSM2MealyParserIO<I, O> extends AbstractFSM2MealyParser<I, O> {

    /**
     * Constructs a new FSM2MealyParserIO. Use one of the static parse() methods to actually parse an FSM source.
     *
     * @param targetInputs
     *         An collection containing the inputs which should constitute the input alphabet of the parsed automaton.
     *         If {@code null}, the inputs will be automatically gathered from the read FSM file.
     * @param inputParser
     *         the input parser (see {@link #inputParser}).
     * @param outputParser
     *         the output parser (similar to {@code inputParser}.
     */
    private FSM2MealyParserIO(@Nullable Collection<? extends I> targetInputs,
                              Function<String, I> inputParser,
                              Function<String, O> outputParser) {
        super(targetInputs, inputParser, outputParser);
    }

    /**
     * Parse a transition.
     *
     * @throws FSMFormatException
     *         when the transition is illegal.
     * @throws IOException
     *         see {@link StreamTokenizer#nextToken()}.
     */
    @Override
    protected void parseTransition(StreamTokenizer streamTokenizer) throws IOException {
        try {

            // check we will read a state index
            if (streamTokenizer.nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMFormatException(EXPECT_NUMBER, streamTokenizer);
            }

            // read the source state index
            int from = Integer.parseInt(streamTokenizer.sval);

            // check if such a state exists
            if (!getStates().isEmpty() && !getStates().contains(from)) {
                throw new FSMFormatException(String.format(NO_SUCH_STATE, from), streamTokenizer);
            }

            // check we will read a state index
            if (streamTokenizer.nextToken() != StreamTokenizer.TT_WORD) {
                throw new FSMFormatException(EXPECT_NUMBER, streamTokenizer);
            }

            // read the target state index
            int to = Integer.parseInt(streamTokenizer.sval);

            // check if such a state exists
            if (!getStates().isEmpty() && !getStates().contains(to)) {
                throw new FSMFormatException(String.format(NO_SUCH_STATE, to), streamTokenizer);
            }

            // check we will read an input edge label
            if (streamTokenizer.nextToken() != '"') {
                throw new FSMFormatException(EXPECT_STRING, streamTokenizer);
            }

            // read the input, and convert the input string to actual input
            final I input = getInputParser().apply(streamTokenizer.sval);

            // add it to the set of inputs
            getInputs().add(input);

            // check we will read an output edge label
            if (streamTokenizer.nextToken() != '"') {
                throw new FSMFormatException(EXPECT_STRING, streamTokenizer);
            }

            // read the output, and convert the output string to actual output
            final O output = getOutputParser().apply(streamTokenizer.sval);

            // create the Mealy machine transition
            final Pair<O, Integer> prev = getTransitions().put(Pair.of(from, input), Pair.of(output, to));

            // check for non-determinism
            if (prev != null) {
                throw new FSMFormatException(String.format(NON_DETERMINISM_DETECTED, prev), streamTokenizer);
            }
        } catch (NumberFormatException nfe) {
            throw new FSMFormatException(nfe, streamTokenizer);
        }
    }

    @Override
    protected void checkTransitions(StreamTokenizer streamTokenizer) {
        // Only if no states are defined we add all from the transitions we found.
        // This is necessary because states are not necessarily defined in FSMs.
        if (getStates().isEmpty()) {
            getStates().addAll(getTransitions().keySet().stream().map(Pair::getFirst).collect(Collectors.toList()));
        }
    }

    public static <I, O> FSM2MealyParserIO<I, O> getParser(@Nullable Collection<? extends I> targetInputs,
                                                           Function<String, I> inputParser,
                                                           Function<String, O> outputParser) {
        return new FSM2MealyParserIO<>(targetInputs, inputParser, outputParser);
    }

    public static <I, O> FSM2MealyParserIO<I, O> getParser(Function<String, I> inputParser,
                                                           Function<String, O> outputParser) {
        return getParser(null, inputParser, outputParser);
    }

    public static <E> FSM2MealyParserIO<E, E> getParser(@Nullable Collection<? extends E> targetInputs,
                                                        Function<String, E> edgeParser) {
        return getParser(targetInputs, edgeParser, edgeParser);
    }

    public static <E> FSM2MealyParserIO<E, E> getParser(Function<String, E> edgeParser) {
        return getParser(edgeParser, edgeParser);
    }
}
