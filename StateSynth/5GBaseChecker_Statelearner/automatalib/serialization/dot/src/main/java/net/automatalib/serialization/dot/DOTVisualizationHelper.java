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
package net.automatalib.serialization.dot;

import java.io.IOException;

import net.automatalib.visualization.VisualizationHelper;

/**
 * Extension to the {@link VisualizationHelper} interface for DOT specific methods.
 *
 * @param <N>
 *         node type
 * @param <E>
 *         edge type
 */
public interface DOTVisualizationHelper<N, E> extends VisualizationHelper<N, E> {

    /**
     * Called before the node and edge data are written, but <i>after</i> the opening "digraph {" statement.
     *
     * @param a
     *         the {@link Appendable} to write to
     *
     * @throws IOException
     *         if writing to {@code a} throws.
     */
    void writePreamble(Appendable a) throws IOException;

    /**
     * Called after the node and edge data are written, but <i>before</i> the closing brace.
     *
     * @param a
     *         the {@link Appendable} to write to
     *
     * @throws IOException
     *         if writing to {@code a} throws.
     */
    void writePostamble(Appendable a) throws IOException;

}
