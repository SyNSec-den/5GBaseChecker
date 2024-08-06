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
import java.util.Collection;
import java.util.List;

import net.automatalib.commons.util.mappings.MutableMapping;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Class for associating arbitrary values with the blocks of a minimization result.
 * <p>
 * The storage and lookup are performed in constant time.
 *
 * @param <V>
 *         value class.
 *
 * @author Malte Isberner
 */
public class BlockMap<V> implements MutableMapping<Block<?, ?>, V> {

    private final @Nullable Object[] storage;

    /**
     * Constructor.
     *
     * @param minResult
     *         the result structure.
     */
    public BlockMap(MinimizationResult<?, ?> minResult) {
        this.storage = new Object[minResult.getNumBlocks()];
    }

    /**
     * Retrieves a value.
     *
     * @param block
     *         the block.
     *
     * @return the associated value.
     */
    @SuppressWarnings("unchecked")
    @Override
    public V get(Block<?, ?> block) {
        return (V) storage[block.getId()];
    }

    /**
     * Stores a value.
     *
     * @param block
     *         the associated block.
     * @param value
     *         the value.
     */
    @Override
    public V put(Block<?, ?> block, V value) {
        @SuppressWarnings("unchecked")
        V old = (V) storage[block.getId()];
        storage[block.getId()] = value;
        return old;
    }

    /**
     * Retrieves all values that are stored in this map.
     *
     * @return the values that are stored in this map.
     */
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        return (List<V>) Arrays.asList(storage);
    }
}
