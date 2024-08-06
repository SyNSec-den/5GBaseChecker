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

import net.automatalib.ts.simple.SimpleTS;
import net.automatalib.words.Alphabet;

/**
 * A utility data class, that allows to pair a model that can react to input symbols with a corresponding alphabet.
 *
 * @param <I>
 *         the input symbol type
 * @param <M>
 *         the model type
 *
 * @author frohme
 */
public class InputModelData<I, M extends SimpleTS<?, I>> {

    public final M model;
    public final Alphabet<I> alphabet;

    public InputModelData(M model, Alphabet<I> alphabet) {
        this.model = model;
        this.alphabet = alphabet;
    }
}
