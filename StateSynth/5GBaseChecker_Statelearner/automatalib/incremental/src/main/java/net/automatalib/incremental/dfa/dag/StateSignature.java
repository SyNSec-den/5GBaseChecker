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
package net.automatalib.incremental.dfa.dag;

import java.util.Arrays;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.incremental.dfa.Acceptance;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Signature of a state. A signature consists of the list of all successor states for all alphabet symbols, and the
 * acceptance status.
 *
 * @author Malte Isberner
 */
final class StateSignature {

    public final ResizingArrayStorage<State> successors;
    public Acceptance acceptance;
    private int hashCode;

    StateSignature(int numSuccs, Acceptance acceptance) {
        this.successors = new ResizingArrayStorage<>(State.class, numSuccs);
        this.acceptance = acceptance;
        updateHashCode();
    }

    StateSignature(StateSignature other) {
        this.successors = new ResizingArrayStorage<>(other.successors);
        this.acceptance = other.acceptance;
        updateHashCode();
    }

    public void updateHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + acceptance.hashCode();
        result = prime * result + Arrays.hashCode(successors.array);
        hashCode = result;
    }

    public StateSignature duplicate() {
        return new StateSignature(this);
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

        final StateSignature other = (StateSignature) obj;

        return hashCode == other.hashCode && acceptance == other.acceptance &&
               Arrays.equals(successors.array, other.successors.array);
    }

}
