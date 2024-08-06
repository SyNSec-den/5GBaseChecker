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
package net.automatalib.util.ts.modal.regression;

import java.io.IOException;
import java.io.InputStream;

import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.ts.modal.CompactMTS;

public class CompositionInstance {

    private static final InputModelDeserializer<String, CompactMTS<String>> PARSER = DOTParsers.mts();

    public final CompactMTS<String> input0;
    public final CompactMTS<String> input1;
    public final CompactMTS<String> merge;

    public CompositionInstance(CompositionTest compositionTest) throws IOException {
        input0 = loadMTSFromPath(compositionTest.input0);
        input1 = loadMTSFromPath(compositionTest.input1);
        merge = loadMTSFromPath(compositionTest.merge);
    }

    private static CompactMTS<String> loadMTSFromPath(String path) throws IOException {
        try (InputStream is = CompositionInstance.class.getResourceAsStream(path)) {
            return PARSER.readModel(is).model;
        }
    }

}
