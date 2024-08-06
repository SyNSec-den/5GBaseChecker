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

/**
 * A generic interface for formalizing an arbitrary serializer for a given model type.
 *
 * @param <M>
 *         the type of objects implementing classes can serialize
 *
 * @author frohme
 */
public interface ModelSerializer<M> {

    /**
     * Writes the model to the given output stream.
     * <p>
     * Note: the output stream will <b>not</b> be closed.
     *
     * @param os
     *         the output stream to write to
     * @param model
     *         the model to write
     *
     * @throws IOException
     *         when writing to the output stream fails.
     */
    void writeModel(OutputStream os, M model) throws IOException;

    /**
     * Writes the model to the given file.
     *
     * @param f
     *         the file to write to
     * @param model
     *         the model to write
     *
     * @throws IOException
     *         when writing to the output stream fails.
     */
    default void writeModel(File f, M model) throws IOException {
        try (OutputStream os = IOUtil.asBufferedOutputStream(f)) {
            writeModel(os, model);
        }
    }

}
