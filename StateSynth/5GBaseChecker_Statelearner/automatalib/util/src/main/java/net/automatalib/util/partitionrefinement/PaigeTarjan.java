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
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An implementation of the Paige/Tarjan partition refinement algorithm.
 * <p>
 * To ensure maximal performance, this class is designed in a very low-level fashion, exposing most of its internal
 * fields directly. It should only ever be used directly, and its use should be hidden behind a facade such that neither
 * this class nor any of its methods/referenced objects are exposed at an API level.
 * <p>
 * This class stores most of its internal data in several, or possibly even a single, (mostly {@code int}) array(s), to
 * achieve maximal cache efficiency. The layout of of these arrays is described in the documentation of the respective
 * public fields: <ul> <li>{@link #blockData}</li> <li>{@link #predOfsData}</li> <li>{@link #predData}</li> <li>{@link
 * #blockForState}</li> </ul> The {@link PaigeTarjanInitializers} provides methods for initializing this data structure
 * for common cases (e.g., DFA minimization). Similarly, the {@link PaigeTarjanExtractors} class provides methods for
 * transforming the resulting data structure.
 *
 * @author Malte Isberner
 */
public class PaigeTarjan {

    /**
     * The number of input symbols.
     */
    public int numInputs;
    /**
     * The number of states.
     */
    public int numStates;
    /**
     * The array storing the raw block data, i.e., the states contained in a certain block. It is assumed that the
     * positions {@link Block#low} and {@link Block#high} refer to this array.
     */
    public int[] blockData;
    /**
     * The array storing the position data, i.e., for each state, its index in the {@link #blockData} array.
     * <p>
     * The layout of this array is assumed to be the following: for the state {@code i}, where <code>0 &lt;= i &lt;
     * {@link #numStates}</code>, the index of {@code i} in {@link #blockData} is <code>{@link #posData}[{@link
     * #posDataLow} + i]</code>.
     */
    public int[] posData;
    /**
     * The lowest index storing position data in the {@link #posData} array.
     */
    public int posDataLow;
    /**
     * The array storing the predecessor offset data, i.e., for each state and input symbol, the delimiting offsets of
     * the respective predecessor list. The offsets are assumed to refer to the {@link #predData} array.
     * <p>
     * The layout of this array is assumed to be the following: for state {@code i} and input symbol {@code j}, where
     * <code>0 &lt;= i &lt; {@link #numStates}</code> and <code>0 &lt;= j &lt; {@link #numInputs}</code>, the offset (in
     * the {@link #predData} array) of the first {@code j}-predecessor of {@code i} is <code>{@link #predOfsData}[{@link
     * #predOfsDataLow} + j*{@link #numStates} + i]</code>, and the last {@code j}-predecessor of {@code i} is
     * <code>{@link #predOfsData}[{@link #predOfsDataLow} + j*{@link #numStates} + i + 1] - 1</code>. Note that this
     * requires the index <code>{@link #predOfsDataLow} + {@link #numInputs} * {@link #numStates}</code> to be valid,
     * and the contents of the {@link #predOfsData} array at this index must be the highest offset of any predecessor
     * plus one.
     */
    public int[] predOfsData;
    /**
     * The lowest index storing predecessor offset data in the {@link #predOfsData} array.
     */
    public int predOfsDataLow;
    /**
     * The array storing the predecessor data, i.e., for each state and input symbol, a list of the respective
     * predecessors.
     * <p>
     * The layout of this array is assumed to be the following: for state {@code i} and input symbol {@code j}, where
     * <code>0 &lt;= i &lt; {@link #numStates}</code> and <code>0 &lt;= j &lt; {@link #numInputs}</code>, the {@code
     * j}-predecessors of {@code i} are the elements of {@link #predData} from index <code>{@link #predOfsData}[{@link
     * #predOfsDataLow} + j*{@link #numStates} + i]</code>, inclusive, to index <code>{@link #predOfsData}[{@link
     * #predOfsDataLow} + j*{@link #numStates} + i + 1]</code>, exclusive.
     */
    public int[] predData;
    /**
     * The array mapping states (in the range between {@code 0} and {@link #numStates}) to their containing block.
     */
    public Block[] blockForState;
    // the head of the block linked list
    private @Nullable Block blocklistHead;
    // the block count
    private int numBlocks;
    // the head of the worklist linked list
    private @Nullable Block worklistHead;
    // the tail of the worklist linked list
    private @Nullable Block worklistTail;
    // the head of the 'touched' list
    private @Nullable Block touchedHead;

    public void setSize(int numStates, int numInputs) {
        this.numStates = numStates;
        this.numInputs = numInputs;
    }

    public void setBlockForState(Block[] blockForState) {
        this.blockForState = blockForState;
    }

    public void setBlockData(int[] blockData) {
        this.blockData = blockData;
    }

    public void setPosData(int[] posData, int posDataLow) {
        this.posData = posData;
        this.posDataLow = posDataLow;
    }

    public void setPredOfsData(int[] predOfsData, int predOfsDataLow) {
        this.predOfsData = predOfsData;
        this.predOfsDataLow = predOfsDataLow;
    }

    public void setPredData(int[] predData) {
        this.predData = predData;
    }

    /**
     * Removes all blocks which are empty from the block list. The {@link Block#id IDs} of the blocks are adjusted to
     * remain contiguous.
     * <p>
     * Note that this method does not modify the worklist, i.e., it should only be called when the worklist is empty.
     */
    public void removeEmptyBlocks() {
        Block curr = blocklistHead;
        Block prev = null;
        int effId = 0;
        while (curr != null) {
            if (!curr.isEmpty()) {
                curr.id = effId++;
                if (prev != null) {
                    prev.nextBlock = curr;
                } else {
                    blocklistHead = curr;
                }
                prev = curr;
            }
            curr = curr.nextBlock;
        }
        if (prev != null) {
            prev.nextBlock = null;
        } else {
            blocklistHead = null;
        }
        numBlocks = effId;
    }

    /**
     * Automatically creates a {@link #blockForState} mapping, and sets it as the current one.
     *
     * @see #createBlockForStateMap()
     * @see #setBlockForState(Block[])
     */
    public void initBlockForStateMap() {
        this.blockForState = createBlockForStateMap();
    }

    /**
     * Creates the {@link #blockForState} mapping from the blocks in the block list, and the contents of the {@link
     * #blockData} array.
     *
     * @return a {@link #blockForState} mapping consistent with the {@link #blockData}
     */
    public Block[] createBlockForStateMap() {
        Block[] map = new Block[numStates];
        for (Block b = blocklistHead; b != null; b = b.nextBlock) {
            int low = b.low, high = b.high;
            for (int i = low; i < high; i++) {
                int state = blockData[i];
                map[state] = b;
            }
        }
        return map;
    }

    /**
     * Initializes the worklist from the block list. This assumes that the worklist is empty.
     * <p>
     * If {@code addAll} is {@code true}, all blocks from the block list will be added to the worklist. Otherwise, all
     * but the largest block will be added.
     *
     * @param addAll
     *         controls if all blocks are added to the worklist, or all but the largest.
     */
    public void initWorklist(boolean addAll) {
        if (addAll) {
            Block last = null;
            for (Block b = blocklistHead; b != null; b = b.nextBlock) {
                b.nextInWorklist = b.nextBlock;
                last = b;
            }
            worklistHead = blocklistHead;
            worklistTail = last;
        } else {
            Block largest = blocklistHead;
            if (largest == null) {
                return;
            }
            int largestSize = largest.size();
            for (Block b = largest.nextBlock; b != null; b = b.nextBlock) {
                int size = b.size();
                if (size > largestSize) {
                    addToWorklist(largest);
                    largest = b;
                    largestSize = size;
                } else {
                    addToWorklist(b);
                }
            }
        }
    }

    private void addToWorklist(Block b) {
        if (worklistHead == null) {
            worklistHead = b;
        } else {
            assert worklistTail != null;
            worklistTail.nextInWorklist = b;
        }
        worklistTail = b;
    }

    /**
     * Refines the partition until it stabilizes.
     */
    public void computeCoarsestStablePartition() {
        Block curr;
        while ((curr = poll()) != null) {
            int blockRange = curr.high - curr.low;
            // copy blockData, because #moveLeft() may change its data while we iterate over it
            // TODO maybe find an implementation that does not need to workaround this concurrent modification
            int[] blockCopy = new int[blockRange];
            System.arraycopy(blockData, curr.low, blockCopy, 0, blockRange);
            int predOfsBase = predOfsDataLow;
            for (int i = 0; i < numInputs; i++) {
                for (int j = 0; j < blockRange; j++) {
                    int state = blockCopy[j];
                    int predOfsIdx = predOfsBase + state;
                    int predLow = predOfsData[predOfsIdx], predHigh = predOfsData[predOfsIdx + 1];
                    for (int k = predLow; k < predHigh; k++) {
                        int pred = predData[k];
                        moveLeft(pred);
                    }
                }
                predOfsBase += numStates;
                processTouched();
            }
        }
    }

    private @Nullable Block poll() {
        if (worklistHead == null) {
            return null;
        }
        Block b = worklistHead;
        worklistHead = b.nextInWorklist;
        b.nextInWorklist = null;
        if (worklistHead == null) {
            worklistTail = null;
        }

        return b;
    }

    private void moveLeft(int state) {
        Block b = blockForState[state];
        //int[] blockData = b.data;
        int posIdx = posDataLow + state;
        int inBlockIdx = posData[posIdx];
        int ptr = b.ptr;

        if (ptr == -1) {
            b.nextTouched = touchedHead;
            touchedHead = b;
            ptr = b.low;
            b.ptr = ptr;
        }

        if (ptr <= inBlockIdx) {
            if (ptr < inBlockIdx) {
                int other = blockData[ptr];
                blockData[ptr] = blockData[inBlockIdx];
                blockData[inBlockIdx] = other;

                posData[posIdx] = ptr;
                posData[posDataLow + other] = inBlockIdx;
            }
            b.ptr = ++ptr;
        }
    }

    private void processTouched() {
        Block b = touchedHead;
        while (b != null) {
            Block next = b.nextTouched;
            b.nextTouched = null;
            Block splt = split(b);
            if (splt != null) {
                addToWorklist(splt);
            }
            b.ptr = -1;
            b = next;
        }

        touchedHead = null;
    }

    private @Nullable Block split(Block b) {
        Block splt = b.split(numBlocks);
        if (splt == null) {
            return null;
        }
        numBlocks++;
        int spltLow = splt.low, spltHigh = splt.high;
        for (int i = spltLow; i < spltHigh; i++) {
            int state = blockData[i];
            blockForState[state] = splt;
        }
        return splt;
    }

    /**
     * Creates a new block. The {@link Block#low} and {@link Block#high} fields will be initialized to {@code -1}.
     *
     * @return a newly created block.
     */
    public Block createBlock() {
        Block b = new Block(-1, -1, numBlocks++, blocklistHead);
        blocklistHead = b;
        return b;
    }

    /**
     * Retrieves the corresponding block for a given state (ID).
     *
     * @param id
     *         the state ID
     *
     * @return the block containing the specified state
     */
    public Block getBlockForState(int id) {
        return blockForState[id];
    }

    /**
     * Retrieves a representative state from the given block. This method behaves deterministically.
     *
     * @param b
     *         the block
     *
     * @return a representative state in the specified block
     */
    public int getRepresentative(Block b) {
        return blockData[b.low];
    }

    /**
     * Retrieves an iterator for the contents of the given block.
     *
     * @param b
     *         the block
     *
     * @return an iterator for the contents of the specified block
     */
    public PrimitiveIterator.OfInt statesInBlockIterator(Block b) {
        return Spliterators.iterator(statesInBlockSpliterator(b));
    }

    /**
     * Retrieves a spliterator for the contents of the given block.
     *
     * @param b
     *         the block
     *
     * @return a spliterator for the contents of the specified block
     */
    public Spliterator.OfInt statesInBlockSpliterator(Block b) {
        return Arrays.spliterator(blockData, b.low, b.high);
    }

    /**
     * Retrieves an iterator for iterating over all blocks in the block list.
     *
     * @return an iterator for iterating over all blocks
     */
    public Iterator<Block> blockListIterator() {
        return Block.blockListIterator(blocklistHead);
    }

    /**
     * Retrieves an {@link Iterable} that provides the iterator returned by {@link #blockListIterator()}.
     *
     * @return an {@link Iterable} for iterating over all blocks
     */
    public Iterable<Block> blockList() {
        return this::blockListIterator;
    }

    /**
     * Retrieves the total number of blocks in the block list.
     *
     * @return the total number of blocks
     */
    public int getNumBlocks() {
        return numBlocks;
    }

    /**
     * Determines how the worklist is managed, i.e., where newly created blocks are inserted.
     *
     * @author Malte Isberner
     */
    public enum WorklistPolicy {
        /**
         * Newly created blocks are inserted at the beginning of the worklist.
         */
        FIFO,
        /**
         * Newly created blocks are inserted at the end of the worklist.
         */
        LIFO
    }

}
