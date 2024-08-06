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

import net.automatalib.commons.util.mappings.Mapping;

/**
 * The result structure of a minimization process. The result of a minimization process is a partition on the original
 * set of states. This is represented by a collection of {@link Block}s containing states that are equivalent and thus
 * can be merged.
 * <p>
 * Since all states in a block are equivalent (and thus especially have the same set of outgoing edge labels), a
 * minimized automaton can be created from this partition by instantiating a state for each block. The edges between
 * those states are created in the following way: For each state/block, an original state is chosen arbitrarily from
 * this block. The edges are created according to the edges of this state, only that they point to the states
 * representing the blocks the respective target states belong to (using {@link #getBlockForState(Object)}).
 * <p>
 * The blocks in the result partition are guaranteed to have contiguous IDs (see {@link Block#getId()}), starting at 0.
 * This allows an efficient construction of the resulting automaton.
 * <p>
 * A more convenient way to obtain a representation of the resulting, minimized automaton is using a {@link
 * BlockAutomaton}.
 *
 * @param <S>
 *         state class.
 * @param <L>
 *         transition label class.
 *
 * @author Malte Isberner
 */
public final class MinimizationResult<S, L> {

    // the state storage, used for retrieving the State records
    // for an original state
    private final Mapping<S, State<S, L>> stateStorage;
    // the blocks in the final partition
    private final Collection<Block<S, L>> blocks;

    /**
     * Constructor.
     *
     * @param stateStorage
     *         the state storage,
     * @param blocks
     *         the final partition.
     */
    MinimizationResult(Mapping<S, State<S, L>> stateStorage, Collection<Block<S, L>> blocks) {
        this.stateStorage = stateStorage;
        this.blocks = blocks;
    }

    /**
     * Retrieves all (original) states in a block.
     *
     * @param block
     *         the block.
     *
     * @return a collection containing all original states in this block.
     */
    public static <S, L> Collection<S> getStatesInBlock(Block<S, L> block) {
        return new OriginalStateCollection<>(block.getStates());
    }

    /**
     * Retrieves the number of blocks in the final partition.
     *
     * @return the number of blocks.
     */
    public int getNumBlocks() {
        return blocks.size();
    }

    /**
     * Retrieves all blocks in the final partition.
     *
     * @return the final partition.
     */
    public Collection<Block<S, L>> getBlocks() {
        return blocks;
    }

    /**
     * Chooses a representative (i.e., an arbitrary element of the set of states) from a block.
     *
     * @param block
     *         the block.
     *
     * @return an arbitrary element of the state set of the given block.
     */
    public S getRepresentative(Block<S, L> block) {
        return block.getStates().choose().getOriginalState();
    }

    /**
     * Retrieves the block to which a given original state belongs.
     *
     * @param origState
     *         the original state.
     *
     * @return the corresponding block.
     */
    public Block<S, L> getBlockForState(S origState) {
        State<S, L> state = stateStorage.get(origState);
        return state.getBlock();
    }

    /**
     * Creates a {@link BlockAutomaton} using this results structure.
     *
     * @return the block automaton.
     */
    public BlockAutomaton<S, L> asBlockAutomaton() {
        return new BlockAutomaton<>(this);
    }
}
