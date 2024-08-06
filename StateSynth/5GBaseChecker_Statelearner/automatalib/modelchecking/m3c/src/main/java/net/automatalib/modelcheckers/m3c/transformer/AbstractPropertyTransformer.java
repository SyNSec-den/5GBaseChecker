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
package net.automatalib.modelcheckers.m3c.transformer;

import java.util.BitSet;
import java.util.List;
import java.util.Set;

import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;

/**
 * Base class used to represent a property transformer, i.e., a function which maps a subset of formulas to a subset of
 * formulas. Can also be seen as a function which maps bit vectors of length n to bit vectors of length n.
 *
 * @param <T>
 *         property transformer type
 * @param <L>
 *         edge label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public abstract class AbstractPropertyTransformer<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final boolean isMust;

    AbstractPropertyTransformer() {
        this(true);
    }

    AbstractPropertyTransformer(boolean isMust) {
        this.isMust = isMust;
    }

    /**
     * Returns the set of variable numbers of subformulas y with f(input)=y, where f is the property transformer
     * represented by {@code this}.
     *
     * @param input
     *         a boolean array representing a set of subformulas
     *
     * @return the set of variable numbers of subformulas
     */
    public abstract BitSet evaluate(boolean[] input);

    /**
     * Returns the composition {@code h} of {@code this} and {@code other} such that {@code h(x) = this(other(x))}. The
     * {@code isMust} attribute of the composition is set to the {@code isMust} attribute of {@code this}.
     *
     * @param other
     *         function which is first applied to an input
     *
     * @return the composition of {@code this} and {@code other}
     */
    public abstract T compose(T other);

    /**
     * Returns the updated property transformer of a node.
     *
     * @param atomicPropositions
     *         of the node
     * @param compositions
     *         of the property transformers belonging to the outgoing edges and their target nodes
     * @param currentBlock
     *         the block which is considered during this update
     *
     * @return the updated property transformer of a node
     */
    public abstract T createUpdate(Set<AP> atomicPropositions,
                                   List<T> compositions,
                                   EquationalBlock<L, AP> currentBlock);

    /**
     * Returns whether the property transformer belongs to a node or to a must edge.
     *
     * @return {@code true} if the property transformer belongs to a node or to a must edge, {@code false} otherwise
     */
    public boolean isMust() {
        return isMust;
    }

}
