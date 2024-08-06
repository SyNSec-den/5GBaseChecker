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
package net.automatalib.automata.base.compact;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class CompactTransition<TP> {

    private int memoryIdx;
    private final int succId;
    private TP property;

    public CompactTransition(int succId, TP property) {
        this(-1, succId, property);
    }

    public CompactTransition(int memoryIdx, int succId, TP property) {
        this.memoryIdx = memoryIdx;
        this.succId = succId;
        this.property = property;
    }

    public int getSuccId() {
        return succId;
    }

    public TP getProperty() {
        return property;
    }

    public void setProperty(TP property) {
        this.property = property;
    }

    public int getMemoryIdx() {
        return memoryIdx;
    }

    public void setMemoryIdx(int memoryIdx) {
        this.memoryIdx = memoryIdx;
    }

    public boolean isAutomatonTransition() {
        return memoryIdx >= 0;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompactTransition)) {
            return false;
        }

        final CompactTransition<?> that = (CompactTransition<?>) o;
        return memoryIdx == that.memoryIdx && succId == that.succId && Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Integer.hashCode(memoryIdx);
        result = 31 * result + Integer.hashCode(succId);
        result = 31 * result + Objects.hashCode(property);
        return result;
    }
}
