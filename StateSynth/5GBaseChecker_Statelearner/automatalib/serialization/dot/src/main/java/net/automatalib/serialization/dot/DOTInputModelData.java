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

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.serialization.InputModelData;
import net.automatalib.ts.simple.SimpleTS;
import net.automatalib.words.Alphabet;

/**
 * A utility data class, that extends {@link InputModelData} by labeling information of the model's states.
 *
 * @param <S>
 *         the state type of the model
 * @param <I>
 *         the input symbol type
 * @param <M>
 *         the model type
 *
 * @author frohme
 */
public class DOTInputModelData<S, I, M extends SimpleTS<S, I>> extends InputModelData<I, M> {

    public final Mapping<S, String> stateLabels;

    public DOTInputModelData(M model, Alphabet<I> alphabet, Mapping<S, String> stateLabels) {
        super(model, alphabet);
        this.stateLabels = stateLabels;
    }
}
