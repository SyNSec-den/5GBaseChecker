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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.automatalib.commons.smartcollections.DefaultLinkedList;
import net.automatalib.commons.smartcollections.ElementReference;
import net.automatalib.commons.smartcollections.IntrusiveLinkedList;
import net.automatalib.commons.smartcollections.UnorderedCollection;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;

/**
 * Automaton minimizer. The automata are accessed via the {@link UniversalGraph} interface, and may be partially
 * defined. Note that undefined transitions are preserved, thus, they have no semantics that could be modeled otherwise
 * wrt. this algorithm.
 * <p>
 * The implemented algorithm is described in the paper "Minimizing incomplete automata" by Marie-Pierre Beal and Maxime
 * Crochemore.
 *
 * @param <S>
 *         state class.
 * @param <L>
 *         transition label class.
 *
 * @author Malte Isberner
 */
public final class Minimizer<S, L> {

    // The following attributes may be reused. Most of them are used
    // as local variables in the split() method, but storing them
    // as attributes helps to avoid costly re-allocations.
    private final DefaultLinkedList<Block<S, L>> splitters = new DefaultLinkedList<>();
    private final IntrusiveLinkedList<TransitionLabel<S, L>> letterList = new IntrusiveLinkedList<>();
    private final IntrusiveLinkedList<State<S, L>> stateList = new IntrusiveLinkedList<>();
    private final IntrusiveLinkedList<Block<S, L>> splitBlocks = new IntrusiveLinkedList<>();
    private final IntrusiveLinkedList<Block<S, L>> newBlocks = new IntrusiveLinkedList<>();
    private final IntrusiveLinkedList<State<S, L>> finalList = new IntrusiveLinkedList<>();
    // These attributes belong to a specific minimization process.
    private @Nullable MutableMapping<S, @Nullable State<S, L>> stateStorage;
    private @Nullable UnorderedCollection<Block<S, L>> partition;
    private int numBlocks;

    /**
     * Minimizes an automaton. The automaton is not minimized directly, instead, a {@link MinimizationResult} structure
     * is returned. The automaton is interfaced using an adapter implementing the {@link UniversalGraph} interface.
     *
     * @param <S>
     *         state class.
     * @param <L>
     *         transition label class.
     * @param graph
     *         the automaton interface.
     *
     * @return the result structure.
     */
    public static <S, L> MinimizationResult<S, L> minimize(UniversalGraph<S, ?, ?, L> graph) {
        return minimize(graph, graph.getNodes());
    }

    public static <S, L> MinimizationResult<S, L> minimize(UniversalGraph<S, ?, ?, L> graph,
                                                           Collection<? extends S> start) {
        return new Minimizer<S, L>().performMinimization(graph, start);
    }

    /**
     * Performs the minimization of an automaton.
     * <p>
     * The automaton is accessed via a {@link UniversalGraph}. The result of the minimization process is effectively a
     * partition on the set of states, each element (block) in this partition contains equivalent states that can be
     * merged in a minimized automaton.
     *
     * @param <E>
     *         edge identifier class.
     * @param graph
     *         the automaton interface.
     *
     * @return a {@link MinimizationResult} structure, containing the state partition.
     */
    public <E> MinimizationResult<S, L> performMinimization(UniversalGraph<S, E, ?, L> graph,
                                                            Collection<? extends S> initialNodes) {
        // Initialize the data structures (esp. state records) and build
        // the initial partition.
        Collection<Block<S, L>> initialBlocks = initialize(graph, initialNodes);

        // Add all blocks from the initial partition as an element
        // of the partition, and as a potential splitter.
        partition = new UnorderedCollection<>(initialBlocks.size());
        ///splitters.hintNextCapacity(initialBlocks.size());

        for (Block<S, L> block : initialBlocks) {
            if (block.isEmpty()) {
                continue;
            }
            addToPartition(block);
            addToSplitterQueue(block);
            numBlocks++;
        }

        // Split the blocks of the partition, until no splitters
        // remain
        while (!splitters.isEmpty()) {
            Block<S, L> block = splitters.choose();
            removeFromSplitterQueue(block);

            split(block);
            updateBlocks();
        }

        // Return the result.
        MinimizationResult<S, L> result = new MinimizationResult<>(stateStorage, partition);

        // Ensure the garbage collection isn't hampered
        stateStorage = null;
        partition = null;
        numBlocks = 0;

        return result;
    }

    public MinimizationResult<S, L> performMinimization(UniversalGraph<S, ?, ?, L> graph) {
        return performMinimization(graph, graph.getNodes());
    }

    /**
     * Builds the initial data structures and performs the initial partitioning.
     */
    private <E> Collection<Block<S, L>> initialize(UniversalGraph<S, E, ?, L> graph,
                                                   Collection<? extends S> initialNodes) {
        if (initialNodes.isEmpty()) {
            return Collections.emptyList();
        }

        Iterable<? extends S> origStates = GraphTraversal.depthFirstOrder(graph, initialNodes);

        Map<L, TransitionLabel<S, L>> transitionMap = new HashMap<>();

        final MutableMapping<S, @Nullable State<S, L>> mapping = graph.createStaticNodeMapping();

        int numStates = 0;
        for (S origState : origStates) {
            State<S, L> state = new State<>(numStates++, origState);
            mapping.put(origState, state);
            stateList.add(state);
        }

        InitialPartitioning<S, L> initPartitioning = new HashMapInitialPartitioning<>(graph);

        for (State<S, L> state : stateList) {
            S origState = state.getOriginalState();

            Block<S, L> block = initPartitioning.getBlock(origState);
            block.addState(state);

            for (E edge : graph.getOutgoingEdges(origState)) {
                S origTarget = graph.getTarget(edge);
                State<S, L> target = mapping.get(origTarget);
                L label = graph.getEdgeProperty(edge);
                TransitionLabel<S, L> transition = transitionMap.computeIfAbsent(label, TransitionLabel::new);
                Edge<S, L> edgeObj = new Edge<>(state, target, transition);
                state.addOutgoingEdge(edgeObj);
                target.addIncomingEdge(edgeObj);
            }
        }
        stateList.quickClear();
        stateStorage = mapping;

        return initPartitioning.getInitialBlocks();
    }

    /**
     * Adds a block to the partition.
     */
    @RequiresNonNull("partition")
    private void addToPartition(Block<S, L> block) {
        ElementReference ref = partition.referencedAdd(block);
        block.setPartitionReference(ref);
    }

    /**
     * Adds a block as a potential splitter.
     */
    private void addToSplitterQueue(Block<S, L> block) {
        ElementReference ref = splitters.referencedAdd(block);
        block.setSplitterQueueReference(ref);
    }

    /**
     * Removes a block from the splitter queue. This is done when it is split completely and thus no longer existant.
     */
    private boolean removeFromSplitterQueue(Block<S, L> block) {
        ElementReference ref = block.getSplitterQueueReference();
        if (ref == null) {
            return false;
        }

        splitters.remove(ref);
        block.setSplitterQueueReference(null);

        return true;
    }

    /**
     * This method realizes the core of the actual minimization, the "split" procedure.
     * <p>
     * A split separates in each block the states, if any, which have different transition characteristics wrt. a
     * specified block, the splitter.
     * <p>
     * This method does not perform actual splits, but instead it modifies the splitBlocks attribute to containing the
     * blocks that could potentially be split. The information on the subsets into which a block is split is contained
     * in the sub-blocks of the blocks in the result list.
     * <p>
     * The actual splitting is performed by the method updateBlocks().
     */
    private void split(Block<S, L> splitter) {
        // STEP 1: Collect the states that have outgoing edges
        // pointing to states inside the currently considered blocks.
        // Also, a list of transition labels occuring on these
        // edges is created.
        for (State<S, L> state : splitter.getStates()) {
            for (Edge<S, L> edge : state.getIncoming()) {
                TransitionLabel<S, L> transition = edge.getTransitionLabel();
                State<S, L> newState = edge.getSource();
                // Blocks that only contain a single state cannot
                // be split any further, and thus are of no
                // interest.
                if (newState.isSingletonBlock()) {
                    continue; //continue;
                }
                if (transition.addToSet(newState)) {
                    letterList.add(transition);
                }
            }
        }

        // STEP 2: Build the signatures. A signature of a state
        // is a sequence of the transition labels of its outgoing
        // edge that point into the considered split block.
        // The iteration over the label list in the outer loop
        // guarantees a consistent ordering of the transition labels.
        for (TransitionLabel<S, L> letter : letterList) {
            for (State<S, L> state : letter.getSet()) {
                if (state.addToSignature(letter)) {
                    stateList.add(state);
                    state.setSplitPoint(false);
                }
            }
            letter.clearSet();
        }
        letterList.clear();

        // STEP 3: Discriminate the states. This is done by weak
        // sorting the states. At the end of the weak sort, the finalList
        // will contain the states in such an order that only states belonging
        // to the same block having the same signature will be contiguous.

        // First, initialize the buckets of each block. This is done
        // for grouping the states by their corresponding block.
        for (State<S, L> state : stateList) {
            Block<S, L> block = state.getBlock();
            if (block.addToBucket(state)) {
                splitBlocks.add(block);
            }
        }
        stateList.clear();

        for (Block<S, L> block : splitBlocks) {
            stateList.concat(block.getBucket());
        }

        // Now, the states are ordered according to their signatures
        int i = 0;

        while (!stateList.isEmpty()) {
            for (State<S, L> state : stateList) {
                TransitionLabel<S, L> letter = state.getSignatureLetter(i);
                if (letter == null) {
                    finalList.pushBack(state);
                } else if (letter.addToBucket(state)) {
                    letterList.add(letter);
                }

                // If this state was the first to be added to the respective
                // bucket, or it differs from the previous entry in the previous
                // letter, it is a split point.
                final State<S, L> prev = state.getPrev();
                if (prev == null) {
                    state.setSplitPoint(true);
                } else if (i > 0 && prev.getSignatureLetter(i - 1) != state.getSignatureLetter(i - 1)) {
                    state.setSplitPoint(true);
                }
            }
            stateList.clear();

            for (TransitionLabel<S, L> letter : letterList) {
                stateList.concat(letter.getBucket());
            }
            letterList.clear();

            i++;
        }

        Block<S, L> prevBlock = null;

        State<S, L> prev = null;
        for (State<S, L> state : finalList) {
            Block<S, L> currBlock = state.getBlock();
            if (currBlock != prevBlock) {
                currBlock.createSubBlock();
                prevBlock = currBlock;
            } else if (state.isSplitPoint()) {
                currBlock.createSubBlock();
            }
            currBlock.addToSubBlock(state);
            if (prev != null) {
                prev.reset();
            }
            prev = state;
        }
        if (prev != null) {
            prev.reset();
        }
        finalList.clear();

        // Step 4 of the algorithm is done in the method
        // updateBlocks()
    }

    /**
     * This method performs the actual splitting of blocks, using the sub block information stored in each block
     * object.
     */
    @RequiresNonNull("partition")
    private void updateBlocks() {
        for (Block<S, L> block : splitBlocks) {
            // Ignore blocks that have no elements in their sub blocks.
            int inSubBlocks = block.getElementsInSubBlocks();
            if (inSubBlocks == 0) {
                continue;
            }

            boolean blockRemains = inSubBlocks < block.size();

            boolean reuseBlock = !blockRemains;

            List<UnorderedCollection<State<S, L>>> subBlocks = block.getSubBlocks();
            // If there is only one sub block which contains all elements of
            // the block, then no split needs to be performed.
            if (!blockRemains && subBlocks.size() == 1) {
                block.clearSubBlocks();
                continue;
            }

            Iterator<UnorderedCollection<State<S, L>>> subBlockIt = subBlocks.iterator();

            if (reuseBlock) {
                UnorderedCollection<State<S, L>> first = subBlockIt.next();
                block.getStates().swap(first);
                updateBlockReferences(block);
            }

            while (subBlockIt.hasNext()) {
                UnorderedCollection<State<S, L>> subBlockStates = subBlockIt.next();

                if (blockRemains) {
                    for (State<S, L> state : subBlockStates) {
                        block.removeState(state.getBlockReference());
                    }
                }

                Block<S, L> subBlock = new Block<>(numBlocks++, subBlockStates);
                updateBlockReferences(subBlock);
                newBlocks.add(subBlock);
                addToPartition(subBlock);
            }

            newBlocks.add(block);
            block.clearSubBlocks();

            // If the split block previously was in the queue, add all newly
            // created blocks to the queue. Otherwise, it's enough to add
            // all but the largest
            if (removeFromSplitterQueue(block)) {
                addAllToSplitterQueue(newBlocks);
            } else {
                addAllButLargest(newBlocks);
            }
            newBlocks.clear();
        }

        splitBlocks.clear();
    }

    /**
     * Sets the blockReference-attribute of each state in the collection to the corresponding ElementReference of the
     * collection.
     */
    private static <S, L> void updateBlockReferences(Block<S, L> block) {
        UnorderedCollection<State<S, L>> states = block.getStates();
        for (ElementReference ref : states.references()) {
            State<S, L> state = states.get(ref);
            state.setBlockReference(ref);
            state.setBlock(block);
        }
    }

    /**
     * Adds all but the largest block of a given collection to the splitter queue.
     */
    private void addAllToSplitterQueue(Collection<Block<S, L>> blocks) {
        for (Block<S, L> block : blocks) {
            addToSplitterQueue(block);
        }
    }

    private void addAllButLargest(Collection<Block<S, L>> blocks) {
        Block<S, L> largest = null;

        for (Block<S, L> block : blocks) {
            if (largest == null) {
                largest = block;
            } else if (block.size() > largest.size()) {
                addToSplitterQueue(largest);
                largest = block;
            } else {
                addToSplitterQueue(block);
            }
        }
    }

}
