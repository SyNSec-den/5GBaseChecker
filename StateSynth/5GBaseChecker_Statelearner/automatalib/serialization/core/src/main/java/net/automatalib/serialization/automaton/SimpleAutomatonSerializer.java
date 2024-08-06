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
package net.automatalib.serialization.automaton;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.words.Alphabet;

/**
 * A refining interface of {@link InputModelSerializer} that binds the model to {@link SimpleAutomaton}s. It also adds
 * new functionality to dynamically serialize systems of arbitrary input type if a transformer to the default input type
 * (specified by implementing class) is given.
 * <p>
 * <b>Note:</b> These model-specific interfaces may be omitted if Java starts supporting higher-kinded generics (or we
 * switch to a language that supports these).
 *
 * @param <I>
 *         The default input symbol type
 *
 * @author frohme
 */
public interface SimpleAutomatonSerializer<I> extends InputModelSerializer<I, SimpleAutomaton<?, I>> {

    /**
     * Writes the model to the given output stream.
     * <p>
     * Note: the output stream will <b>not</b> be closed.
     *
     * @param os
     *         the output stream to write to
     * @param model
     *         the model to write
     * @param alphabet
     *         the inputs of the model to which serialization should be limit
     * @param inputTransformer
     *         a function to transform the inputs of the model to the inputs of {@code this} serializer.
     * @param <I2>
     *         the input symbol type of the model
     *
     * @throws IOException
     *         when writing to the output stream fails.
     */
    <I2> void writeModel(OutputStream os,
                         SimpleAutomaton<?, I2> model,
                         Alphabet<I2> alphabet,
                         Function<I2, I> inputTransformer) throws IOException;

    /**
     * Writes the model to the given file.
     *
     * @param f
     *         the file to write to
     * @param model
     *         the model to write
     * @param alphabet
     *         the inputs of the model to which serialization should be limit
     * @param inputTransformer
     *         a function to transform the inputs of the model to the inputs of {@code this} serializer.
     * @param <I2>
     *         the input symbol type of the model
     *
     * @throws IOException
     *         when writing to the output stream fails.
     */
    default <I2> void writeModel(File f,
                                 SimpleAutomaton<?, I2> model,
                                 Alphabet<I2> alphabet,
                                 Function<I2, I> inputTransformer) throws IOException {
        try (OutputStream os = IOUtil.asBufferedOutputStream(f)) {
            writeModel(os, model, alphabet, inputTransformer);
        }
    }

    @Override
    default void writeModel(OutputStream os, SimpleAutomaton<?, I> model, Alphabet<I> alphabet) throws IOException {
        writeModel(os, model, alphabet, Function.identity());
    }
}
