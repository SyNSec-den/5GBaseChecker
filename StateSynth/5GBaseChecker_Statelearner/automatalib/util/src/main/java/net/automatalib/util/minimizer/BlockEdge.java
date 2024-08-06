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
package net.automatalib.util.minimizer;

/**
 * An edge in a {@link BlockAutomaton}.
 *
 * @param <S>
 *         state class.
 * @param <L>
 *         transition label class.
 *
 * @author Malte Isberner
 */
public class BlockEdge<S, L> {

    private final Block<S, L> source;
    private final Block<S, L> target;
    private final L label;

    /**
     * Constructor.
     *
     * @param source
     *         source block.
     * @param target
     *         target block.
     * @param label
     *         the transition label.
     */
    BlockEdge(Block<S, L> source, Block<S, L> target, L label) {
        this.source = source;
        this.target = target;
        this.label = label;
    }

    /**
     * Retrieves the source block.
     *
     * @return the source block.
     */
    public Block<S, L> getSource() {
        return source;
    }

    /**
     * Retrieves the target block.
     *
     * @return the target block.
     */
    public Block<S, L> getTarget() {
        return target;
    }

    /**
     * Retrieves the transition label.
     *
     * @return the transition label.
     */
    public L getLabel() {
        return label;
    }
}
