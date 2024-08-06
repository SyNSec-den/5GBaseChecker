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
package net.automatalib.automata.base.fast;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.commons.util.nid.AbstractMutableNumericID;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractFastState<T> extends AbstractMutableNumericID {

    private final ResizingArrayStorage<@Nullable T> transitions;

    public AbstractFastState(int initialNumOfInputs) {
        this.transitions = new ResizingArrayStorage<>(Object.class, initialNumOfInputs);
    }

    /**
     * See {@link ResizingArrayStorage#ensureCapacity(int)}.
     */
    public final boolean ensureInputCapacity(int capacity) {
        return this.transitions.ensureCapacity(capacity);
    }

    public final void setTransitionObject(int inputIdx, @Nullable T transition) {
        transitions.array[inputIdx] = transition;
    }

    public final void clearTransitionObjects() {
        for (int i = 0; i < transitions.array.length; i++) {
            clearTransitionObject(getTransitionObject(i));
            transitions.array[i] = null;
        }
    }

    protected void clearTransitionObject(final @Nullable T transition) {
        // do nothing in particular, but sub-classes may cleanup additional resources
    }

    public final @Nullable T getTransitionObject(int inputIdx) {
        return transitions.array[inputIdx];
    }

    @Override
    public String toString() {
        return "s" + getId();
    }
}
