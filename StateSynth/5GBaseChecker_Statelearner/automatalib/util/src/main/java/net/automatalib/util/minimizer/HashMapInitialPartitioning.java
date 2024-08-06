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
package net.automatalib.util.minimizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.graphs.UniversalIndefiniteGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @param <S>
 *         state class.
 * @param <L>
 *         transition
 *
 * @author Malte Isberner
 */
class HashMapInitialPartitioning<S, L> implements InitialPartitioning<S, L> {

    private final Map<@Nullable Object, Block<S, L>> initialBlockMap = new HashMap<>();
    private final UniversalIndefiniteGraph<S, ?, ?, L> graph;

    private int numExistingBlocks;

    HashMapInitialPartitioning(UniversalIndefiniteGraph<S, ?, ?, L> graph) {
        this.graph = graph;
    }

    @Override
    public Block<S, L> getBlock(S origState) {
        @Nullable Object clazz = graph.getNodeProperty(origState);
        return initialBlockMap.computeIfAbsent(clazz, k -> new Block<>(numExistingBlocks++));
    }

    @Override
    public Collection<Block<S, L>> getInitialBlocks() {
        return initialBlockMap.values();
    }

}
