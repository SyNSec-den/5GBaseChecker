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
package net.automatalib.graphs.ads.impl;

import java.util.Collections;
import java.util.Map;

import net.automatalib.graphs.ads.RecursiveADSNode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An abstract implementation of a leaf node, that may be used by other ADS-extending classes.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 * @param <N>
 *         the concrete node type
 *
 * @author frohme
 */
public abstract class AbstractRecursiveADSLeafNode<S, I, O, N extends RecursiveADSNode<S, I, O, N>>
        implements RecursiveADSNode<S, I, O, N> {

    private @Nullable N parent;
    private S hypothesisState;

    public AbstractRecursiveADSLeafNode(@Nullable N parent, S hypothesisState) {
        this.parent = parent;
        this.hypothesisState = hypothesisState;
    }

    @Override
    public @Nullable I getSymbol() {
        return null;
    }

    @Override
    public void setSymbol(I symbol) {
        throw new UnsupportedOperationException("Cannot set symbol state on a leaf node");
    }

    @Override
    public @Nullable N getParent() {
        return this.parent;
    }

    @Override
    public void setParent(@Nullable N parent) {
        this.parent = parent;
    }

    @Override
    public Map<O, N> getChildren() {
        return Collections.emptyMap();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public S getHypothesisState() {
        return this.hypothesisState;
    }

    @Override
    public void setHypothesisState(S state) {
        this.hypothesisState = state;
    }

    @Override
    public String toString() {
        final S state = this.getHypothesisState();
        return state == null ? "<null>" : state.toString();
    }
}
