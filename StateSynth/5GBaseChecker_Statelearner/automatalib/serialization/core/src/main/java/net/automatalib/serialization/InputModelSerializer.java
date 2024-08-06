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
package net.automatalib.serialization;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import net.automatalib.commons.util.IOUtil;
import net.automatalib.ts.simple.SimpleTS;
import net.automatalib.words.Alphabet;

/**
 * A refinement of the {@link ModelSerializer} interface for arbitrary models that can react to inputs. Introduces a new
 * type variable for the input symbol type and limits the model type to {@link SimpleTS}s.
 *
 * @param <I>
 *         the type of input symbols
 * @param <M>
 *         the type of objects implementing classes can deserialize
 *
 * @author frohme
 */
public interface InputModelSerializer<I, M extends SimpleTS<?, I>> extends ModelSerializer<InputModelData<I, M>> {

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
     *
     * @throws IOException
     *         when writing to the output stream fails.
     */
    void writeModel(OutputStream os, M model, Alphabet<I> alphabet) throws IOException;

    /**
     * Writes the model to the given file.
     *
     * @param f
     *         the file to write to
     * @param model
     *         the model to write
     * @param alphabet
     *         the inputs of the model to which serialization should be limit
     *
     * @throws IOException
     *         when writing to the output stream fails.
     */
    default void writeModel(File f, M model, Alphabet<I> alphabet) throws IOException {
        try (OutputStream os = IOUtil.asBufferedOutputStream(f)) {
            writeModel(os, model, alphabet);
        }
    }

    @Override
    default void writeModel(OutputStream os, InputModelData<I, M> model) throws IOException {
        writeModel(os, model.model, model.alphabet);
    }
}
