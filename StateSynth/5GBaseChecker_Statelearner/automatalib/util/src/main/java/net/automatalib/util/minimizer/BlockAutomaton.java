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

import java.util.Arrays;
import java.util.List;

/**
 * A "block automaton", i.e. an automaton-style representation of the minimization result in which each block forms a
 * state.
 *
 * @param <S>
 *         state class.
 * @param <L>
 *         transition label class.
 *
 * @author Malte Isberner
 */
public class BlockAutomaton<S, L> {

    // Edges array
    private final BlockEdge<S, L>[][] edges;

    /**
     * Constructor. Creates the block automaton.
     *
     * @param minResult
     *         the minimization result.
     */
    @SuppressWarnings("unchecked")
    BlockAutomaton(MinimizationResult<S, L> minResult) {
        edges = new BlockEdge[minResult.getNumBlocks()][];

        for (Block<S, L> block : minResult.getBlocks()) {
            int id = block.getId();
            State<S, L> rep = block.getStates().choose();
            List<Edge<S, L>> outgoing = rep.getOutgoing();
            BlockEdge<S, L>[] array = new BlockEdge[outgoing.size()];
            int i = 0;
            for (Edge<S, L> e : outgoing) {
                array[i++] =
                        new BlockEdge<>(block, e.getTarget().getBlock(), e.getTransitionLabel().getOriginalLabel());
            }
            edges[id] = array;
        }
    }

    /**
     * Retrieves a list of outgoing edges of a block (state).
     *
     * @param block
     *         the block (state).
     *
     * @return the outgoing edges of the given block (state).
     */
    public List<BlockEdge<S, L>> getOutgoingEdges(Block<S, L> block) {
        return Arrays.asList(edges[block.getId()]);
    }

    /**
     * Retrieves an array of outgoing edges of a block (state).
     *
     * @see #getOutgoingEdges(Block)
     */
    public BlockEdge<S, L>[] getOutgoingEdgeArray(Block<S, L> block) {
        return edges[block.getId()];
    }
}
