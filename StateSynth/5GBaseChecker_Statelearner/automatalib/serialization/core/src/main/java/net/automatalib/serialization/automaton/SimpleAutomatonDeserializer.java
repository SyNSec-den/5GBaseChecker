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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Function;

import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.serialization.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;

/**
 * A refining interface of {@link InputModelDeserializer} that binds the model to {@link SimpleAutomaton}s. It also adds
 * new functionality to read systems of arbitrary input type given a transform function from the default input type
 * (specified by implementing class).
 * <p>
 * <b>Note:</b> These model-specific interfaces may be omitted if Java starts supporting higher-kinded generics (or we
 * switch to a language that supports these).
 *
 * @param <S>
 *         The state type of the de-serialized hypothesis
 * @param <I>
 *         The default input symbol type
 *
 * @author frohme
 */
public interface SimpleAutomatonDeserializer<S, I> extends InputModelDeserializer<I, SimpleAutomaton<S, I>> {

    /**
     * Reads the contents from the given input stream and de-serializes it into a {@link InputModelData} that contains
     * the model and its alphabet definition.
     * <p>
     * Note: the input stream will <b>not</b> be closed.
     *
     * @param is
     *         the input stream to read data from
     * @param inputTransformer
     *         a function to transform the inputs of {@code this} serializer to inputs of the returned model
     * @param <I2>
     *         the input symbol type of the model
     *
     * @return {@link InputModelData} that contains the model and its alphabet definition
     *
     * @throws IOException
     *         if an error occurred while reading from the stream
     * @throws FormatException
     *         if the content of the stream was not in the expected format
     */
    <I2> InputModelData<I2, SimpleAutomaton<S, I2>> readModel(InputStream is, Function<I, I2> inputTransformer)
            throws IOException;

    /**
     * Reads the contents from the given URL and de-serializes it into a {@link InputModelData} that contains the model
     * and its alphabet definition.
     *
     * @param url
     *         the url to read data from
     * @param inputTransformer
     *         a function to transform the inputs of {@code this} serializer to inputs of the returned model
     * @param <I2>
     *         the input symbol type of the model
     *
     * @return {@link InputModelData} that contains the model and its alphabet definition
     *
     * @throws IOException
     *         if an error occurred while reading from the stream
     * @throws FormatException
     *         if the content of the stream was not in the expected format
     */
    default <I2> InputModelData<I2, SimpleAutomaton<S, I2>> readModel(URL url, Function<I, I2> inputTransformer)
            throws IOException {
        try (InputStream is = IOUtil.asBufferedInputStream(url.openStream())) {
            return readModel(is, inputTransformer);
        }
    }

    /**
     * Reads the contents from the given file and de-serializes it into a {@link InputModelData} that contains the model
     * and its alphabet definition.
     *
     * @param f
     *         the file to read data from
     * @param inputTransformer
     *         a function to transform the inputs of {@code this} serializer to inputs of the returned model
     * @param <I2>
     *         the input symbol type of the model
     *
     * @return {@link InputModelData} that contains the model and its alphabet definition
     *
     * @throws IOException
     *         if an error occurred while reading from the stream
     * @throws FormatException
     *         if the content of the stream was not in the expected format
     */
    default <I2> InputModelData<I2, SimpleAutomaton<S, I2>> readModel(File f, Function<I, I2> inputTransformer)
            throws IOException {
        try (InputStream is = IOUtil.asBufferedInputStream(f)) {
            return readModel(is, inputTransformer);
        }
    }

    /**
     * Reads the contents from the given byte buffer and de-serializes it into a {@link InputModelData} that contains
     * the model and its alphabet definition.
     *
     * @param buf
     *         the buffer to read data from
     * @param inputTransformer
     *         a function to transform the inputs of {@code this} serializer to inputs of the returned model
     * @param <I2>
     *         the input symbol type of the model
     *
     * @return {@link InputModelData} that contains the model and its alphabet definition
     *
     * @throws IOException
     *         if an error occurred while reading from the stream
     * @throws FormatException
     *         if the content of the stream was not in the expected format
     */
    default <I2> InputModelData<I2, SimpleAutomaton<S, I2>> readModel(byte[] buf, Function<I, I2> inputTransformer)
            throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(buf)) {
            return readModel(is, inputTransformer);
        }
    }

    @Override
    default InputModelData<I, SimpleAutomaton<S, I>> readModel(InputStream is) throws IOException {
        return readModel(is, Function.identity());
    }
}
