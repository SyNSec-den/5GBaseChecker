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
package net.automatalib.commons.util.collections;

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link Set} for positive {@link Integer int}s that internally stores its elements in a {@link BitSet}. This class
 * may also be used as a(n) (im)mutable {@link Set}-view of an existing {@link BitSet}.
 *
 * @author frohme
 */
public class PositiveIntSet extends AbstractSet<Integer> {

    private final BitSet delegate;
    private final boolean immutable;

    /**
     * Default constructor for a mutable {@link PositiveIntSet}.
     */
    public PositiveIntSet() {
        this(new BitSet(), false);
    }

    /**
     * Default constructor for an immutable view of a given {@link BitSet}.
     *
     * @param delegate
     *         the backing {@link BitSet}
     */
    public PositiveIntSet(BitSet delegate) {
        this(delegate, true);
    }

    /**
     * Constructor for a(n) (immutable) view of a given {@link PositiveIntSet}.
     *
     * @param delegate
     *         the backing {@link BitSet}
     * @param immutable
     *         a flag indicating whether mutating operations should write through to the backing {@link BitSet}
     */
    public PositiveIntSet(BitSet delegate, boolean immutable) {
        this.delegate = delegate;
        this.immutable = immutable;
    }

    @Override
    public int size() {
        return delegate.cardinality();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        if (!(o instanceof Integer)) {
            return false;
        }

        return containsInt((Integer) o);
    }

    public boolean containsInt(int integer) {
        return delegate.get(integer);
    }

    @Override
    public BitSetIterator iterator() {
        return new BitSetIterator(delegate, immutable);
    }

    @Override
    public boolean add(Integer integer) {
        return addInt(integer);
    }

    public boolean addInt(int integer) {
        if (immutable) {
            throw new UnsupportedOperationException("This is a read-only set-view");
        }

        boolean containedBefore = delegate.get(integer);
        delegate.set(integer);

        return !containedBefore;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        if (!(o instanceof Integer)) {
            return false;
        }

        return removeInt((Integer) o);
    }

    public boolean removeInt(int integer) {
        if (immutable) {
            throw new UnsupportedOperationException("This is a read-only set-view");
        }

        boolean containedBefore = delegate.get(integer);
        delegate.clear(integer);
        return containedBefore;
    }

    @Override
    public void clear() {
        if (immutable) {
            throw new UnsupportedOperationException("This is a read-only set-view");
        }

        delegate.clear();
    }
}
