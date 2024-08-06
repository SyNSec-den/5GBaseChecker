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
package net.automatalib.util.graphs.scc;

final class TarjanSCCRecord {

    /**
     * The index of this record in the order of discovery, i.e. the record belongs to the number-th node discovered
     * during DFS traversal.
     */
    public final int number;

    /**
     * The id of the SCC of the node. Initially it is assumed that each nodes has its own SCC, thus this value is
     * initialized with {@link #number}.
     */
    public int sccId;

    TarjanSCCRecord(int number) {
        this.number = number;
        this.sccId = number;
    }

}
