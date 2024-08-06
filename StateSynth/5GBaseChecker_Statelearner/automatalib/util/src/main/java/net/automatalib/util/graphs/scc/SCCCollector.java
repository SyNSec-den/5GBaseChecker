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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SCCCollector<N> implements SCCListener<N> {

    private final List<List<N>> sccList = new ArrayList<>();

    @Override
    public void foundSCC(Collection<? extends N> scc) {
        sccList.add(new ArrayList<>(scc));
    }

    public List<List<N>> getSCCList() {
        return sccList;
    }

}
