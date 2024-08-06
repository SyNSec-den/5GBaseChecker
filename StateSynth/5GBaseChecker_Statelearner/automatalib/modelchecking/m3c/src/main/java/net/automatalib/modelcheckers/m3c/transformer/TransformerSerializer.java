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
package net.automatalib.modelcheckers.m3c.transformer;

import java.util.List;

/**
 * Utility interface for serializing {@link AbstractPropertyTransformer} implementations.
 *
 * @param <T>
 *         the concrete transformer class
 * @param <L>
 *         the label class
 * @param <AP>
 *         the atomic proposition class
 *
 * @author frohme
 */
public interface TransformerSerializer<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    /**
     * Serializes a given transformer to {@link List}-based {@link String} representation.
     *
     * @param transformer
     *         the property transformer to be serialized.
     *
     * @return the serialized property transformer.
     */
    List<String> serialize(T transformer);

    /**
     * Deserializes a given transformer from its {@link List}-based {@link String} representation.
     *
     * @param data
     *         a serialized property transformer.
     *
     * @return the deserialized property transformer.
     */
    T deserialize(List<String> data);

}
