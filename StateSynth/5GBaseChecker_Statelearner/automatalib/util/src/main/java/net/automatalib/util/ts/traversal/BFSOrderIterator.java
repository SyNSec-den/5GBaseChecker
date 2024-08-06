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
package net.automatalib.util.ts.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.ts.simple.SimpleTS;
import net.automatalib.util.traversal.VisitedState;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BFSOrderIterator<S, I> implements Iterator<S> {

    private final Collection<? extends I> inputs;
    private final SimpleTS<S, I> ts;
    private final Queue<S> bfsQueue = new ArrayDeque<>();
    private final MutableMapping<S, @Nullable VisitedState> seen;

    public BFSOrderIterator(SimpleTS<S, I> ts, Collection<? extends I> inputs) {
        this.ts = ts;
        this.inputs = inputs;
        Collection<S> initial = ts.getInitialStates();
        bfsQueue.addAll(initial);
        seen = ts.createStaticStateMapping();
        for (S state : initial) {
            seen.put(state, VisitedState.VISITED);
        }
    }

    @Override
    public boolean hasNext() {
        return !bfsQueue.isEmpty();
    }

    @Override
    public S next() {
        S state = bfsQueue.poll();

        if (state == null) {
            throw new NoSuchElementException();
        }

        for (I input : inputs) {
            Collection<S> succs = ts.getSuccessors(state, input);
            for (S succ : succs) {
                if (seen.put(succ, VisitedState.VISITED) != VisitedState.VISITED) {
                    bfsQueue.add(succ);
                }
            }
        }

        return state;
    }

}
