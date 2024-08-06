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
package net.automatalib.graphs.helpers;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import net.automatalib.graphs.Graph;
import net.automatalib.graphs.SimpleGraph;

public class SimpleNormalGraphView<N, G extends SimpleGraph<N>> extends IndefiniteNormalGraphView<N, G>
        implements Graph<N, N> {

    public SimpleNormalGraphView(G simpleGraph) {
        super(simpleGraph);
    }

    @Override
    public int size() {
        return simpleGraph.size();
    }

    @Override
    public Collection<N> getNodes() {
        return simpleGraph.getNodes();
    }

    @Override
    public Iterator<N> iterator() {
        return simpleGraph.iterator();
    }

    @Override
    public Stream<N> nodesStream() {
        return simpleGraph.nodesStream();
    }

}
