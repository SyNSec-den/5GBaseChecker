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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractTwoLevelIterator<L1, L2, O> implements Iterator<O> {

    private final Iterator<? extends L1> l1Iterator;
    private L1 l1Object;
    private @Nullable Iterator<L2> l2Iterator;

    public AbstractTwoLevelIterator(Iterator<? extends L1> l1Iterator) {
        this.l1Iterator = l1Iterator;
        this.l2Iterator = null;
    }

    @Override
    public boolean hasNext() {
        if (l2Iterator != null && l2Iterator.hasNext()) {
            return true;
        }
        return advance();
    }

    @EnsuresNonNullIf(expression = "l2Iterator", result = true)
    private boolean advance() {
        while (l2Iterator == null || !l2Iterator.hasNext()) {
            if (!l1Iterator.hasNext()) {
                return false;
            }
            this.l1Object = l1Iterator.next();
            this.l2Iterator = l2Iterator(this.l1Object);
        }
        return true;
    }

    protected abstract Iterator<L2> l2Iterator(L1 l1Object);

    @Override
    public O next() {
        if ((l2Iterator == null || !l2Iterator.hasNext()) && !advance()) {
            throw new NoSuchElementException();
        }

        return combine(l1Object, l2Iterator.next());
    }

    protected abstract O combine(L1 l1Object, L2 l2Object);

    @Override
    public void remove() {
        if (l2Iterator == null) {
            throw new IllegalStateException();
        }
        l2Iterator.remove();
    }

}
