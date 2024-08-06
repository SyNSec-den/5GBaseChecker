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
package net.automatalib.automata.base.compact;

import java.util.Arrays;

import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link AbstractCompactSimpleDeterministic}-based implementation for automata that need to store generic state
 * properties.
 *
 * @param <I>
 *         input symbol type
 * @param <SP>
 *         state property type
 *
 * @author frohme
 * @author Malte Isberner
 */
public class UniversalCompactSimpleDet<I, @Nullable SP> extends AbstractCompactSimpleDeterministic<I, SP> {

    private @Nullable Object[] stateProperties;

    public UniversalCompactSimpleDet(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public UniversalCompactSimpleDet(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
        this.stateProperties = new Object[stateCapacity];
    }

    public UniversalCompactSimpleDet(UniversalCompactSimpleDet<I, SP> other) {
        this(other.getInputAlphabet(), other);
    }

    protected UniversalCompactSimpleDet(Alphabet<I> alphabet, UniversalCompactSimpleDet<?, SP> other) {
        super(alphabet, other);
        this.stateProperties = other.stateProperties.clone();
    }

    @Override
    public void clear() {
        Arrays.fill(stateProperties, 0, size(), null);
        super.clear();
    }

    @Override
    public void setStateProperty(int stateId, @Nullable SP property) {
        stateProperties[stateId] = property;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SP getStateProperty(int stateId) {
        return (SP) stateProperties[stateId];
    }

    @Override
    protected void updateStateStorage(Payload payload) {
        this.stateProperties = updateStateStorage(this.stateProperties, null, payload);
        super.updateStateStorage(payload);
    }

}
