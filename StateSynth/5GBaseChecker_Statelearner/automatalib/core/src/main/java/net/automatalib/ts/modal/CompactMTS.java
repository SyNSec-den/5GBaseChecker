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
package net.automatalib.ts.modal;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactMTS<I> extends AbstractCompactMTS<I, MutableModalEdgeProperty> {

    public CompactMTS(Alphabet<I> alphabet) {
        super(alphabet);
    }

    public CompactMTS(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
    }

    @Override
    protected MutableModalEdgeProperty getDefaultTransitionProperty() {
        return buildModalProperty(ModalType.MUST);
    }

    @Override
    protected MutableModalEdgeProperty buildModalProperty(ModalType type) {
        return new ModalEdgePropertyImpl(type);
    }

    public static final class Creator<I> implements AutomatonCreator<CompactMTS<I>, I> {

        private final @Nullable Alphabet<I> defaultInputAlphabet;

        public Creator() {
            this(null);
        }

        public Creator(@Nullable Alphabet<I> defaultInputAlphabet) {
            this.defaultInputAlphabet = defaultInputAlphabet;
        }

        @Override
        public CompactMTS<I> createAutomaton(Alphabet<I> alphabet) {
            return this.createAutomaton(alphabet, DEFAULT_INIT_CAPACITY);
        }

        @Override
        public CompactMTS<I> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return new CompactMTS<>(
                    defaultInputAlphabet != null ? defaultInputAlphabet : alphabet,
                    sizeHint,
                    DEFAULT_RESIZE_FACTOR);
        }
    }
}
