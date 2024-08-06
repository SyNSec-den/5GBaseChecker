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
import java.util.List;

import net.automatalib.visualization.helper.AggregateVisualizationHelper;

public class AggregateDOTVisualizationHelper<N, E> extends AggregateVisualizationHelper<N, E>
        implements DOTVisualizationHelper<N, E> {

    private final List<? extends DOTVisualizationHelper<N, ? super E>> helpers;

    public AggregateDOTVisualizationHelper(List<? extends DOTVisualizationHelper<N, ? super E>> visualizationHelpers) {
        super(visualizationHelpers);
        this.helpers = visualizationHelpers;
    }

    @Override
    public void writePreamble(Appendable a) throws IOException {
        for (DOTVisualizationHelper<N, ? super E> h : helpers) {
            h.writePreamble(a);
        }
    }

    @Override
    public void writePostamble(Appendable a) throws IOException {
        for (DOTVisualizationHelper<N, ? super E> h : helpers) {
            h.writePostamble(a);
        }
    }
}
