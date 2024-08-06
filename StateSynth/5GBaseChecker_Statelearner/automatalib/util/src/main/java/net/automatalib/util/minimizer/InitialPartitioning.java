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

/**
 * Interface for the initial partitioning data structure.
 * <p>
 * This interface is needed to generalize the optimized (if the number of partitions is known and the classification can
 * be done using integers) and non-optimized variants of initial partitionings.
 *
 * @param <S>
 *         state class.
 * @param <L>
 *         transition label class.
 *
 * @author Malte Isberner
 */
interface InitialPartitioning<S, L> {

    /**
     * Retrieves the initial block for a given state. If no such block exists, it will be created.
     *
     * @param origState
     *         the original state.
     *
     * @return the block for the state in the initial partitioning.
     */
    Block<S, L> getBlock(S origState);

    /**
     * Retrieves all blocks in the initial partitioning.
     *
     * @return the initial blocks.
     */
    Collection<Block<S, L>> getInitialBlocks();
}
