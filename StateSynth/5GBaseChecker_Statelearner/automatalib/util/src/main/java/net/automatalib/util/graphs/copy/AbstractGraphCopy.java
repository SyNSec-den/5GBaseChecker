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
package net.automatalib.util.graphs.copy;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.graphs.MutableGraph;
import org.checkerframework.checker.nullness.qual.EnsuresKeyFor;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractGraphCopy<N1, E1, N2, E2, NP2, EP2, G1 extends IndefiniteGraph<N1, E1>> {

    protected final MutableMapping<N1, @Nullable N2> nodeMapping;
    protected final G1 inGraph;
    protected final MutableGraph<N2, E2, NP2, EP2> outGraph;

    protected final Mapping<? super N1, ? extends NP2> npMapping;
    protected final Mapping<? super E1, ? extends EP2> epMapping;

    AbstractGraphCopy(G1 inGraph,
                      MutableGraph<N2, E2, NP2, EP2> outGraph,
                      Mapping<? super N1, ? extends NP2> npMapping,
                      Mapping<? super E1, ? extends EP2> epMapping) {
        this.inGraph = inGraph;
        this.outGraph = outGraph;
        this.nodeMapping = inGraph.createStaticNodeMapping();
        this.npMapping = npMapping;
        this.epMapping = epMapping;
    }

    protected E2 copyEdge(@KeyFor("nodeMapping") N2 src2, E1 edge) {
        return copyEdge(src2, edge, inGraph.getTarget(edge));
    }

    protected E2 copyEdge(@KeyFor("nodeMapping") N2 src2, E1 edge, N1 tgt1) {
        EP2 prop = epMapping.get(edge);

        N2 tgt2 = nodeMapping.get(tgt1);

        return outGraph.connect(src2, tgt2, prop);
    }

    protected @Nullable N2 copyEdgeChecked(N2 source, E1 edge, N1 tgt1) {
        EP2 prop = epMapping.get(edge);

        N2 tgt2 = nodeMapping.get(tgt1);

        N2 freshTgt = null;

        if (tgt2 == null) {
            tgt2 = copyNode(tgt1);
            freshTgt = tgt2;
        }

        outGraph.connect(source, tgt2, prop);
        return freshTgt;
    }

    @SuppressWarnings("contracts.postcondition.not.satisfied")
    @EnsuresKeyFor(value = "#1", map = "nodeMapping")
    protected N2 copyNode(N1 node) {
        NP2 prop = npMapping.get(node);
        N2 n2 = outGraph.addNode(prop);

        nodeMapping.put(node, n2);
        return n2;
    }

    public Mapping<N1, N2> getNodeMapping() {
        return nodeMapping;
    }

    public abstract void doCopy();
}
