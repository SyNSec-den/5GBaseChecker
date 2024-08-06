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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A block (i.e., partition class) that is maintained during the Paige/Tarjan partition refinement algorithm (see {@link
 * PaigeTarjan}).
 * <p>
 * Like {@link PaigeTarjan}, this is a very low-level class that exposes a lot (almost all) of its fields directly. Care
 * should be taken that instances of this class are not returned (in any form) to the API user, but are hidden behind a
 * facade.
 *
 * @author Malte Isberner
 */
public class Block {

    /**
     * The index of the first element in this block in the {@link PaigeTarjan#blockData} array.
     */
    public int low;
    /**
     * The current pointer, i.e., the delimiter between elements of this block which were found to belong to a potential
     * sub-class of this block, and those that do not or have not been checked.
     * <p>
     * This variable will be maintained such that either <code>ptr == -1</code>, or <code>{@link #low} &lt;= ptr &lt;=
     * {@link #high}</code>.
     */
    public int ptr = -1;
    /**
     * The index of the last element in this block in the {@link PaigeTarjan#blockData} array, plus one.
     */
    public int high;
    public @Nullable Block nextBlock;
    public int id;
    protected @Nullable Block nextInWorklist;
    protected @Nullable Block nextTouched;

    /**
     * Constructor. Creates a new block with the specified parameters.
     *
     * @param low
     *         the low index of this block's data in the {@link PaigeTarjan#blockData} array
     * @param high
     *         the high index of this block's data in the {@link PaigeTarjan#blockData} array
     * @param id
     *         the ID of this block
     * @param next
     *         the next block in the block list
     */
    public Block(int low, int high, int id, @Nullable Block next) {
        this.low = low;
        this.high = high;
        this.id = id;
        this.nextBlock = next;
    }

    static Iterator<Block> blockListIterator(@Nullable Block start) {
        return new BlockListIterator(start);
    }

    /**
     * Retrieves the size of this block.
     *
     * @return the size of this block
     */
    public int size() {
        return high - low;
    }

    /**
     * Checks whether this block is empty.
     *
     * @return {@code true} if this block is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return low >= high;
    }

    /**
     * Splits this block, if applicable. If this block cannot be split, {@code null} is returned.
     * <p>
     * A new block (the split result) is created if both <code>{@link #ptr} &gt; {@link #low}</code> and <code>{@link
     * #ptr} &lt; {@link #high}</code>. This new block will contain either the elements between {@link #low} (inclusive)
     * and {@link #ptr} (exclusive), or between {@link #ptr} (inclusive) and {@link #high} (exclusive), depending on
     * whichever range is smaller. This block will be updated to contain the remaining elements.
     * <p>
     * When this method returns (regardless of whether a new block is created), the {@link #ptr} field will have been
     * reset to {@code -1}.
     *
     * @param newId
     *         the ID of the newly created block, if applicable
     *
     * @return a block
     */
    public @Nullable Block split(int newId) {
        int ptr = this.ptr;
        this.ptr = -1;
        int high = this.high;
        int ptrHighDiff = high - ptr;
        if (ptrHighDiff == 0) {
            return null;
        }
        int low = this.low;
        Block splt;
        if (ptrHighDiff > ptr - low) {
            splt = new Block(low, ptr, newId, nextBlock);
            this.low = ptr;
        } else {
            splt = new Block(ptr, high, newId, nextBlock);
            this.high = ptr;
        }
        this.nextBlock = splt;
        return splt;
    }

    private static final class BlockListIterator implements Iterator<Block> {

        private @Nullable Block curr;

        BlockListIterator(@Nullable Block start) {
            this.curr = start;
        }

        @Override
        public boolean hasNext() {
            return curr != null;
        }

        @Override
        public Block next() {
            Block result = curr;
            if (result == null) {
                throw new NoSuchElementException();
            }
            curr = result.nextBlock;
            return result;
        }
    }
}
