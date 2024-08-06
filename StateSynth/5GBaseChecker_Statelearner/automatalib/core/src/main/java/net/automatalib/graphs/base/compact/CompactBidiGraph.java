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
package net.automatalib.graphs.base.compact;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactBidiGraph<@Nullable NP, @Nullable EP> extends AbstractCompactBidiGraph<NP, EP> {

    private final ResizingArrayStorage<NP> nodeProperties;

    public CompactBidiGraph() {
        this.nodeProperties = new ResizingArrayStorage<>(Object.class);
    }

    public CompactBidiGraph(int initialCapacity) {
        super(initialCapacity);
        this.nodeProperties = new ResizingArrayStorage<>(Object.class, initialCapacity);
    }

    @Override
    public void setNodeProperty(int node, @Nullable NP property) {
        nodeProperties.ensureCapacity(node + 1);
        nodeProperties.array[node] = property;
    }

    @Override
    public NP getNodeProperty(int node) {
        return node < nodeProperties.array.length ? nodeProperties.array[node] : null;
    }

}
