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
package net.automatalib.automata.graphs;

import java.util.Objects;

import net.automatalib.ts.UniversalTransitionSystem;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TransitionEdge<I, T> {

    private final I input;
    private final T transition;

    public TransitionEdge(I input, T transition) {
        this.input = input;
        this.transition = transition;
    }

    public I getInput() {
        return input;
    }

    public T getTransition() {
        return transition;
    }

    public <TP> Property<I, TP> property(UniversalTransitionSystem<?, ?, T, ?, TP> uts) {
        return new Property<>(input, uts.getTransitionProperty(transition));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransitionEdge)) {
            return false;
        }

        final TransitionEdge<?, ?> that = (TransitionEdge<?, ?>) o;
        return Objects.equals(input, that.input) && Objects.equals(transition, that.transition);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(input);
        result = 31 * result + Objects.hashCode(transition);
        return result;
    }

    public static final class Property<I, TP> {

        private final I input;
        private final TP property;

        public Property(I input, TP property) {
            this.input = input;
            this.property = property;
        }

        public I getInput() {
            return input;
        }

        public TP getProperty() {
            return property;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Property)) {
                return false;
            }

            final Property<?, ?> that = (Property<?, ?>) o;
            return Objects.equals(input, that.input) && Objects.equals(property, that.property);
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + Objects.hashCode(input);
            result = 31 * result + Objects.hashCode(property);
            return result;
        }
    }
}
