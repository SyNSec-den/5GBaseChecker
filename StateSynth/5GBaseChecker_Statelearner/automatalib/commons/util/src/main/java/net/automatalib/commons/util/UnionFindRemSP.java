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
 * Implementation of a disjoint set (union-find) data structure for integers, based on Rem's algorithm, as described in
 * the paper <a href="http://www.ii.uib.no/~fredrikm/fredrik/papers/SEA2010.pdf"><i>Experiments on Union-Find Algorithms
 * for the Disjoint-Set Data Structure</i> (M. Patwary, J. Blair, F. Manne; Proc. SEA 2010)</a>.
 *
 * @author Malte Isberner
 */
public class UnionFindRemSP implements IntDisjointSets {

    private final int[] p;

    /**
     * Initializes the disjoint-set data structure.
     *
     * @param n
     *         the overall size of the domain
     */
    public UnionFindRemSP(int n) {
        p = new int[n];

        for (int i = 0; i < n; i++) {
            p[i] = i;
        }
    }

    @Override
    public int size() {
        return p.length;
    }

    @Override
    public boolean equivalent(int x, int y) {
        int rx = x;
        int ry = y;
        int px = p[rx];
        int py = p[ry];

        while (px != py) {
            if (px < py) {
                if (rx == px) {
                    return false;
                }
                rx = px;
                px = p[rx];
            } else {
                if (ry == py) {
                    return false;
                }
                ry = py;
                py = p[ry];
            }
        }
        return true;
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
        return curr;
        // TODO: use path compression/halving/...?
    }

    /**
     * Unites the sets containing the two given elements.
     *
     * @param x
     *         the first element
     * @param y
     *         the second element
     */
    @Override
    public boolean union(int x, int y) {
        int rx = x;
        int ry = y;
        int px = p[rx];
        int py = p[ry];
        while (px != py) {
            if (px < py) {
                if (rx == px) {
                    p[rx] = py;
                    return true;
                }
                p[rx] = py;
                rx = px;
                px = p[rx];
            } else {
                if (ry == py) {
                    p[ry] = px;
                    return true;
                }
                p[ry] = px;
                ry = py;
                py = p[ry];
            }
        }
        return false;
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
        if (x < y) {
            p[x] = y;
            return y;
        }
        p[y] = x;
        return x;
    }
}
