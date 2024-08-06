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

import java.util.Collection;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.Mappings;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.UniversalIndefiniteGraph;
import net.automatalib.util.traversal.TraversalOrder;

public final class GraphCopy {

    private GraphCopy() {}

    public static <N1, E1, N2, E2, NP2, EP2> Mapping<N1, N2> copyPlain(UniversalGraph<N1, E1, ? extends NP2, ? extends EP2> in,
                                                                       MutableGraph<N2, E2, NP2, EP2> out) {
        Mapping<N1, ? extends NP2> npMapping = in::getNodeProperty;
        Mapping<E1, ? extends EP2> epMapping = in::getEdgeProperty;
        return copyPlain(in, out, npMapping, epMapping);
    }

    public static <N1, E1, N2, E2, NP2, EP2> Mapping<N1, N2> copyPlain(Graph<N1, E1> in,
                                                                       MutableGraph<N2, E2, NP2, EP2> out,
                                                                       Mapping<? super N1, ? extends NP2> npMapping,
                                                                       Mapping<? super E1, ? extends EP2> epMapping) {
        PlainGraphCopy<N1, E1, N2, E2, NP2, EP2> copy = new PlainGraphCopy<>(in, out, npMapping, epMapping);
        copy.doCopy();
        return copy.getNodeMapping();
    }

    public static <N1, E1, NP1, EP1, N2, E2, NP2, EP2> Mapping<N1, N2> copyUniversalPlain(UniversalGraph<N1, E1, NP1, EP1> in,
                                                                                          MutableGraph<N2, E2, NP2, EP2> out,
                                                                                          Mapping<? super NP1, ? extends NP2> npConversion,
                                                                                          Mapping<? super EP1, ? extends EP2> epConversion) {
        Mapping<? super N1, ? extends NP2> npMapping = Mappings.compose(in::getNodeProperty, npConversion);
        Mapping<? super E1, ? extends EP2> epMapping = Mappings.compose(in::getEdgeProperty, epConversion);
        return copyPlain(in, out, npMapping, epMapping);
    }

    public static <N1, E1, N2, E2, NP2, EP2> Mapping<N1, N2> copyTraversal(UniversalIndefiniteGraph<N1, E1, ? extends NP2, ? extends EP2> in,
                                                                           MutableGraph<N2, E2, NP2, EP2> out,
                                                                           TraversalOrder order,
                                                                           int limit,
                                                                           Collection<? extends N1> initialNodes) {
        Mapping<N1, ? extends NP2> npMapping = in::getNodeProperty;
        Mapping<E1, ? extends EP2> epMapping = in::getEdgeProperty;
        return copyTraversal(in, out, order, limit, initialNodes, npMapping, epMapping);
    }

    public static <N1, E1, N2, E2, NP2, EP2> Mapping<N1, N2> copyTraversal(IndefiniteGraph<N1, E1> in,
                                                                           MutableGraph<N2, E2, NP2, EP2> out,
                                                                           TraversalOrder order,
                                                                           int limit,
                                                                           Collection<? extends N1> initialNodes,
                                                                           Mapping<? super N1, ? extends NP2> npMapping,
                                                                           Mapping<? super E1, ? extends EP2> epMapping) {
        TraversalGraphCopy<N1, E1, N2, E2, NP2, EP2> copy =
                new TraversalGraphCopy<>(order, limit, in, initialNodes, out, npMapping, epMapping);
        copy.doCopy();
        return copy.getNodeMapping();
    }

    public static <N1, E1, NP1, EP1, N2, E2, NP2, EP2> Mapping<N1, N2> copyUniversalTraversal(UniversalIndefiniteGraph<N1, E1, NP1, EP1> in,
                                                                                              MutableGraph<N2, E2, NP2, EP2> out,
                                                                                              TraversalOrder order,
                                                                                              int limit,
                                                                                              Collection<? extends N1> initialNodes,
                                                                                              Mapping<? super NP1, ? extends NP2> npConversion,
                                                                                              Mapping<? super EP1, ? extends EP2> epConversion) {
        Mapping<? super N1, ? extends NP2> npMapping = Mappings.compose(in::getNodeProperty, npConversion);
        Mapping<? super E1, ? extends EP2> epMapping = Mappings.compose(in::getEdgeProperty, epConversion);
        return copyTraversal(in, out, order, limit, initialNodes, npMapping, epMapping);
    }

}
