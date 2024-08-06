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
package net.automatalib.serialization.dot;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import net.automatalib.commons.util.IOUtil;
import net.automatalib.graphs.Graph;
import net.automatalib.serialization.ModelSerializer;

public final class DOTSerializationProvider<N, E> implements ModelSerializer<Graph<N, E>> {

    private static final DOTSerializationProvider<?, ?> INSTANCE = new DOTSerializationProvider<>();

    private DOTSerializationProvider() {
        // prevent instantiation
    }

    @SuppressWarnings("unchecked")
    public static <N, E> DOTSerializationProvider<N, E> getInstance() {
        return (DOTSerializationProvider<N, E>) INSTANCE;
    }

    @Override
    public void writeModel(OutputStream os, Graph<N, E> model) throws IOException {
        try (Writer w = IOUtil.asBufferedNonClosingUTF8Writer(os)) {
            GraphDOT.write(model, w);
        }
    }
}
