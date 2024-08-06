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
package net.automatalib.ts.powerset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

public class FastPowersetState<S> extends AbstractSet<S> {

    private final BitSet bs = new BitSet();
    private final List<S> contents = new ArrayList<>();

    public void add(S state, int id) {
        if (bs.get(id)) {
            return;
        }
        bs.set(id);
        contents.add(state);
    }

    @Override
    public Iterator<S> iterator() {
        return contents.iterator();
    }

    @Override
    public int size() {
        return contents.size();
    }

    @Override
    public boolean remove(@Nullable Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FastPowersetState)) {
            return false;
        }

        final FastPowersetState<?> that = (FastPowersetState<?>) o;
        return Objects.equals(bs, that.bs);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(bs);
    }
}
