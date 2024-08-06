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
package net.automatalib.incremental.mealy.dag;

import java.util.Arrays;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

final class StateSignature<O> {

    public final ResizingArrayStorage<State<O>> successors;
    public final ResizingArrayStorage<O> outputs;
    private int hashCode;

    StateSignature(int numSuccs) {
        this.successors = new ResizingArrayStorage<>(State.class, numSuccs);
        this.outputs = new ResizingArrayStorage<>(Object.class, numSuccs);
        updateHashCode();
    }

    StateSignature(StateSignature<O> other) {
        this.successors = new ResizingArrayStorage<>(other.successors);
        this.outputs = new ResizingArrayStorage<>(other.outputs);
        updateHashCode();
    }

    public StateSignature<O> duplicate() {
        return new StateSignature<>(this);
    }

    public void updateHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(outputs.array);
        result = prime * result + Arrays.hashCode(successors.array);
        hashCode = result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StateSignature)) {
            return false;
        }

        final StateSignature<?> other = (StateSignature<?>) obj;

        return hashCode == other.hashCode && Arrays.equals(outputs.array, other.outputs.array) &&
               Arrays.equals(successors.array, other.successors.array);
    }

}
