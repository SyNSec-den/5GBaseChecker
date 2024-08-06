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
package net.automatalib.words.abstractimpl;

import java.util.Objects;

import net.automatalib.commons.util.nid.AbstractMutableNumericID;
import net.automatalib.words.impl.Symbol;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractSymbol<S extends AbstractSymbol<S>> extends AbstractMutableNumericID
        implements Comparable<S> {

    @Override
    public int compareTo(S that) {
        return this.id - that.id;
    }

    @Override
    public final boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Symbol)) {
            return false;
        }

        final AbstractSymbol<?> that = (AbstractSymbol<?>) o;
        return this.id == that.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(super.id);
    }

}
