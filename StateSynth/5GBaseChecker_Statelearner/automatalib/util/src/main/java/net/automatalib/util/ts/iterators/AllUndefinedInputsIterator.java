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
package net.automatalib.util.ts.iterators;

import java.util.Iterator;

import net.automatalib.commons.util.collections.AbstractTwoLevelIterator;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.ts.TS;
import net.automatalib.util.ts.TS.TransRef;

public final class AllUndefinedInputsIterator<S, I> extends AbstractTwoLevelIterator<S, I, TransRef<S, I, ?>> {

    private final TransitionSystem<S, I, ?> ts;
    private final Iterable<? extends I> inputs;

    public AllUndefinedInputsIterator(Iterator<? extends S> stateIt,
                               TransitionSystem<S, I, ?> ts,
                               Iterable<? extends I> inputs) {
        super(stateIt);
        this.ts = ts;
        this.inputs = inputs;
    }

    @Override
    protected Iterator<I> l2Iterator(S state) {
        return TS.undefinedInputsIterator(ts, state, inputs.iterator());
    }

    @Override
    protected TransRef<S, I, Void> combine(S l1Object, I l2Object) {
        return new TransRef<>(l1Object, l2Object, null);
    }
}
