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

public class CompactPMPGEdge<L, EP> extends CompactEdge<EP> {

    private L label;

    public CompactPMPGEdge(int target, EP property, L label) {
        super(target, property);
        this.label = label;
    }

    public L getLabel() {
        return label;
    }

    void setLabel(L label) {
        this.label = label;
    }
}
