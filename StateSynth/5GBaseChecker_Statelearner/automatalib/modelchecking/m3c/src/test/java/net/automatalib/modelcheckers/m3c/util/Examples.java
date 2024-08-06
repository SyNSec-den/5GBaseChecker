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
package net.automatalib.modelcheckers.m3c.util;

import java.util.Collections;
import java.util.Set;

import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.MutableProceduralModalProcessGraph;
import net.automatalib.graphs.base.DefaultCFMPS;
import net.automatalib.graphs.base.compact.CompactPMPG;

public final class Examples {

    private Examples() {}

    public static ContextFreeModalProcessSystem<String, String> getCfmpsAnBn(Set<String> finalNodesAP) {
        final CompactPMPG<String, String> pmpg = buildPMPG(new CompactPMPG<>(""), finalNodesAP);
        return new DefaultCFMPS<>("P", Collections.singletonMap("P", pmpg));
    }

    private static <N, E, AP, M extends MutableProceduralModalProcessGraph<N, String, E, AP, ?>> M buildPMPG(M pmpg,
                                                                                                             Set<AP> finalNodeAPs) {

        final N start = pmpg.addNode();
        final N end = pmpg.addNode();
        final N s1 = pmpg.addNode();
        final N s2 = pmpg.addNode();

        pmpg.setInitialNode(start);
        pmpg.setFinalNode(end);
        pmpg.setAtomicPropositions(s2, finalNodeAPs);
        pmpg.setAtomicPropositions(end, finalNodeAPs);

        final E e1 = pmpg.connect(start, s1);
        final E e2 = pmpg.connect(start, end);
        final E e3 = pmpg.connect(s1, s2);
        final E e4 = pmpg.connect(s2, end);

        pmpg.getEdgeProperty(e1).setMust();
        pmpg.setEdgeLabel(e1, "a");

        pmpg.getEdgeProperty(e2).setMust();
        pmpg.setEdgeLabel(e2, "e");

        pmpg.getEdgeProperty(e3).setMust();
        pmpg.getEdgeProperty(e3).setProcess();
        pmpg.setEdgeLabel(e3, "P");

        pmpg.getEdgeProperty(e4).setMust();
        pmpg.setEdgeLabel(e4, "b");

        return pmpg;
    }

}
