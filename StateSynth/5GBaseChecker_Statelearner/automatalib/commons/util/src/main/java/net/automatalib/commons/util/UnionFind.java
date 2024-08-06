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
package net.automatalib.commons.util;

/**
 * The well-known disjoint-set forest data structure for dealing with partitions on a fixed-range integer domain.
 *
 * @author fhowar
 * @author Malte Isberner
 */
public final class UnionFind implements IntDisjointSets {

    private final int[] p;
    private final int[] rank;

    /**
     * Initializes the disjoint-set data structure.
     *
     * @param n
     *         the overall size of the domain
     */
    public UnionFind(int n) {
        p = new int[n];
        rank = new int[n];

        for (int i = 0; i < n; i++) {
            // primitive arrays are always zero initialized
            // rank[i] = 0;
            p[i] = i;
        }
    }

    @Override
    public int size() {
        return p.length;
    }

    /**
     * Finds the set of a given element, and compresses the path to the root node.
     *
     * @param x
     *         the element
     *
     * @return the identifier of the set which contains the given element
     */
    @Override
    public int find(int x) {
        int curr = x;
        int currp = p[curr];
        while (curr != currp) {
            curr = currp;
            currp = p[curr];
        }
        int ancestor = curr;
        curr = x;
        while (curr != ancestor) {
            int next = p[curr];
            p[curr] = ancestor;
            curr = next;
        }

        return ancestor;
    }

    /**
     * Unites two given sets. Note that the behavior of this method is not specified if the given parameters are normal
     * elements and no set identifiers.
     *
     * @param x
     *         the first set
     * @param y
     *         the second set
     *
     * @return the identifier of the resulting set (either {@code x} or {@code y})
     */
    @Override
    public int link(int x, int y) {
        int rx = rank[x], ry = rank[y];
        if (rx > ry) {
            p[y] = x;
            return x;
        }
        p[x] = y;
        if (rx == ry) {
            rank[y] = ry + 1;
        }
        return y;
    }

}
