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
package net.automatalib.util.partitionrefinement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.simple.SimpleDeterministicAutomaton;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class provides several methods to initialize a {@link PaigeTarjan} partition refinement data structure from
 * common sources, e.g., automata.
 * <p>
 * The counterpart of this class is {@link PaigeTarjanExtractors}, which provides methods to translate the contents of
 * the partition refinement data structure after coarsest stable partition computation back to such structures.
 *
 * @author Malte Isberner
 */
public final class PaigeTarjanInitializers {

    private PaigeTarjanInitializers() {}

    /**
     * Initializes the partition refinement data structure from a given abstracted deterministic automaton, using a
     * predefined initial partitioning mode.
     *
     * @param pt
     *         the partition refinement data structure
     * @param absAutomaton
     *         the abstraction of the input automaton
     * @param ip
     *         the initial partitioning mode
     * @param pruneUnreachable
     *         whether to prune unreachable states during initialization
     */
    public static void initCompleteDeterministic(PaigeTarjan pt,
                                                 UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> absAutomaton,
                                                 AutomatonInitialPartitioning ip,
                                                 boolean pruneUnreachable) {
        initCompleteDeterministic(pt, absAutomaton, ip.initialClassifier(absAutomaton), pruneUnreachable);
    }

    /**
     * Initializes the partition refinement data structure from a given abstracted deterministic automaton, partitioning
     * states according to the given classification function.
     * <p>
     * The return value of the {@code initialClassification} function can be any valid {@code Object} reference (even
     * {@code null}). States are initially placed in the same partition class if their return values from applying
     * {@code initialClassification} have the same hash code (determined according to {@link Objects#hashCode(Object)})
     * and are equal (determined according to {@link Objects#equals(Object, Object)}.
     *
     * @param pt
     *         the partition refinement data structure
     * @param absAutomaton
     *         the abstraction of the input automaton
     * @param initialClassification
     *         the function determining the initial classification
     * @param pruneUnreachable
     *         whether to prune unreachable states during initialization
     */
    public static void initCompleteDeterministic(PaigeTarjan pt,
                                                 SimpleDeterministicAutomaton.FullIntAbstraction absAutomaton,
                                                 IntFunction<?> initialClassification,
                                                 boolean pruneUnreachable) {

        if (pruneUnreachable) {
            initCompleteDeterministicPrune(pt, absAutomaton, initialClassification);
        } else {
            initCompleteDeterministicNoPrune(pt, absAutomaton, initialClassification);
        }
    }

    private static void initCompleteDeterministicPrune(PaigeTarjan pt,
                                                       SimpleDeterministicAutomaton.FullIntAbstraction absAutomaton,
                                                       IntFunction<?> initialClassification) {

        int numStates = absAutomaton.size();
        int numInputs = absAutomaton.numInputs();

        int posDataLow = numStates;
        int predOfsDataLow = posDataLow + numStates;
        int numTransitions = numStates * numInputs;
        int predDataLow = predOfsDataLow + numTransitions + 1;
        int dataSize = predDataLow + numTransitions;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStates];

        Map<@Nullable Object, Block> blockMap = new HashMap<>();

        int init = absAutomaton.getIntInitialState();
        Object initClass = initialClassification.apply(init);

        Block initBlock = pt.createBlock();
        initBlock.high = 1;
        blockForState[init] = initBlock;
        blockMap.put(initClass, initBlock);

        int[] statesBuff = new int[numStates];
        statesBuff[0] = init;

        int statesPtr = 0;
        int reachableStates = 1;

        while (statesPtr < reachableStates) {
            int curr = statesBuff[statesPtr++];
            int predCountBase = predOfsDataLow;

            for (int i = 0; i < numInputs; i++) {
                int succ = absAutomaton.getSuccessor(curr, i);
                if (succ < 0) {
                    throw new IllegalArgumentException("Automaton must not be partial");
                }

                Block succBlock = blockForState[succ];
                if (succBlock == null) {
                    Object succClass = initialClassification.apply(succ);
                    succBlock = blockMap.get(succClass);
                    if (succBlock == null) {
                        succBlock = pt.createBlock();
                        succBlock.high = 0;
                        blockMap.put(succClass, succBlock);
                    }
                    succBlock.high++;
                    blockForState[succ] = succBlock;
                    statesBuff[reachableStates++] = succ;
                }
                data[predCountBase + succ]++;
                predCountBase += numStates;
            }
        }

        int curr = 0;
        for (Block b : pt.blockList()) {
            curr += b.high;
            b.high = curr;
            b.low = curr;
        }

        data[predOfsDataLow] += predDataLow;
        prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < reachableStates; i++) {
            int stateId = statesBuff[i];
            Block b = blockForState[stateId];
            int pos = --b.low;
            data[pos] = stateId;
            data[posDataLow + stateId] = pos;

            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = absAutomaton.getSuccessor(stateId, j);
                assert succ >= 0;

                data[--data[predOfsBase + succ]] = stateId;
                predOfsBase += numStates;
            }
        }

        pt.setBlockData(data);
        pt.setPosData(data, posDataLow);
        pt.setPredOfsData(data, predOfsDataLow);
        pt.setPredData(data);
        pt.setBlockForState(blockForState);
        pt.setSize(numStates, numInputs);
    }

    private static void initCompleteDeterministicNoPrune(PaigeTarjan pt,
                                                         SimpleDeterministicAutomaton.FullIntAbstraction absAutomaton,
                                                         IntFunction<?> initialClassification) {
        int numStates = absAutomaton.size();
        int numInputs = absAutomaton.numInputs();

        int posDataLow = numStates;
        int predOfsDataLow = posDataLow + numStates;
        int numTransitions = numStates * numInputs;
        int predDataLow = predOfsDataLow + numTransitions + 1;
        int dataSize = predDataLow + numTransitions;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStates];

        Map<@Nullable Object, Block> blockMap = new HashMap<>();

        for (int i = 0; i < numStates; i++) {
            Object classification = initialClassification.apply(i);
            Block block = blockMap.get(classification);
            if (block == null) {
                block = pt.createBlock();
                block.high = 0;
                blockMap.put(classification, block);
            }
            block.high++;
            blockForState[i] = block;

            int predCountBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = absAutomaton.getSuccessor(i, j);
                if (succ < 0) {
                    throw new IllegalArgumentException("Automaton must not be partial");
                }

                data[predCountBase + succ]++;
                predCountBase += numStates;
            }
        }

        int curr = 0;
        for (Block b : pt.blockList()) {
            curr += b.high;
            b.high = curr;
            b.low = curr;
        }

        data[predOfsDataLow] += predDataLow;
        prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < numStates; i++) {
            Block b = blockForState[i];
            int pos = --b.low;
            data[pos] = i;
            data[posDataLow + i] = pos;
            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = absAutomaton.getSuccessor(i, j);
                assert succ >= 0;

                data[--data[predOfsBase + succ]] = i;
                predOfsBase += numStates;
            }
        }

        pt.setBlockData(data);
        pt.setPosData(data, posDataLow);
        pt.setPredOfsData(data, predOfsDataLow);
        pt.setPredData(data);
        pt.setBlockForState(blockForState);
        pt.setSize(numStates, numInputs);
    }

    public static void prefixSum(int[] array, int startInclusive, int endExclusive) {
        Arrays.parallelPrefix(array, startInclusive, endExclusive, Integer::sum);
    }

    /**
     * Initializes the partition refinement data structure from a given deterministic automaton, initializing the
     * initial partition according to the given classification function.
     * <p>
     * This method can be used for automata with partially defined transition functions.
     *
     * @param pt
     *         the partition refinement data structure
     * @param absAutomaton
     *         the abstraction of the input automaton
     * @param initialClassification
     *         the initial classification function
     * @param sinkClassification
     *         determines how a sink is being classified.
     */
    public static void initDeterministic(PaigeTarjan pt,
                                         SimpleDeterministicAutomaton.FullIntAbstraction absAutomaton,
                                         IntFunction<?> initialClassification,
                                         Object sinkClassification) {

        int numStates = absAutomaton.size();
        int numInputs = absAutomaton.numInputs();

        int sinkId = numStates;
        int numStatesWithSink = numStates + 1;
        int posDataLow = numStatesWithSink;
        int predOfsDataLow = posDataLow + numStatesWithSink;
        int numTransitionsFull = numStatesWithSink * numInputs;
        int predDataLow = predOfsDataLow + numTransitionsFull + 1;
        int dataSize = predDataLow + numTransitionsFull;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStatesWithSink];

        Map<@Nullable Object, Block> blockMap = new HashMap<>();

        int initId = absAutomaton.getIntInitialState();

        Object initClass = initialClassification.apply(initId);

        Block initBlock = pt.createBlock();
        initBlock.high = 1;
        blockForState[initId] = initBlock;
        blockMap.put(initClass, initBlock);

        int[] statesBuff = new int[numStatesWithSink];
        statesBuff[0] = initId;

        int statesPtr = 0;
        int reachableStates = 1;

        boolean partial = false;
        while (statesPtr < reachableStates) {
            int currId = statesBuff[statesPtr++];
            if (currId == sinkId) {
                continue;
            }

            int predCountBase = predOfsDataLow;

            for (int i = 0; i < numInputs; i++) {

                int succ = absAutomaton.getSuccessor(currId, i);
                int succId;
                if (succ < 0) {
                    succId = sinkId;
                    partial = true;
                } else {
                    succId = succ;
                }
                Block succBlock = blockForState[succId];
                if (succBlock == null) {
                    Object succClass;
                    if (succ < 0) {
                        succClass = sinkClassification;
                    } else {
                        succClass = initialClassification.apply(succ);
                    }
                    succBlock = blockMap.get(succClass);
                    if (succBlock == null) {
                        succBlock = pt.createBlock();
                        succBlock.high = 0;
                        blockMap.put(succClass, succBlock);
                    }
                    succBlock.high++;
                    blockForState[succId] = succBlock;
                    statesBuff[reachableStates++] = succId;
                }
                data[predCountBase + succId]++;
                predCountBase += numStatesWithSink;
            }
        }

        if (partial) {
            int predCountIdx = predOfsDataLow + sinkId;
            for (int i = 0; i < numInputs; i++) {
                data[predCountIdx]++;
                predCountIdx += numStatesWithSink;
            }
        }

        int curr = 0;
        for (Block b : pt.blockList()) {
            curr += b.high;
            b.high = curr;
            b.low = curr;
        }

        data[predOfsDataLow] += predDataLow;
        prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < reachableStates; i++) {
            int stateId = statesBuff[i];
            Block b = blockForState[stateId];
            int pos = --b.low;
            data[pos] = stateId;
            data[posDataLow + stateId] = pos;

            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                final int succId;

                if (stateId == sinkId) { // state is new artificial sink
                    succId = sinkId;
                } else {
                    int succ = absAutomaton.getSuccessor(stateId, j);
                    if (succ < 0) {
                        succId = sinkId;
                    } else {
                        succId = succ;
                    }
                }

                data[--data[predOfsBase + succId]] = stateId;
                predOfsBase += numStatesWithSink;
            }
        }

        pt.setBlockData(data);
        pt.setPosData(data, posDataLow);
        pt.setPredOfsData(data, predOfsDataLow);
        pt.setPredData(data);
        pt.setSize(numStatesWithSink, numInputs);
        pt.setBlockForState(blockForState);

        pt.removeEmptyBlocks();
    }

}
