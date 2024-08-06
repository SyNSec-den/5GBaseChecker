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
import java.util.Map;

import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DefaultDOTVisualizationHelper<N, E> extends DefaultVisualizationHelper<N, E>
        implements DOTVisualizationHelper<N, E> {

    private final @Nullable VisualizationHelper<N, ? super E> delegate;

    public DefaultDOTVisualizationHelper() {
        this(null);
    }

    public DefaultDOTVisualizationHelper(@Nullable VisualizationHelper<N, ? super E> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void getGlobalNodeProperties(Map<String, String> properties) {
        if (delegate != null) {
            delegate.getGlobalNodeProperties(properties);
        } else {
            super.getGlobalNodeProperties(properties);
        }
    }

    @Override
    public void getGlobalEdgeProperties(Map<String, String> properties) {
        if (delegate != null) {
            delegate.getGlobalEdgeProperties(properties);
        } else {
            super.getGlobalEdgeProperties(properties);
        }
    }

    @Override
    public boolean getNodeProperties(N node, Map<String, String> properties) {
        if (delegate != null) {
            return delegate.getNodeProperties(node, properties);
        } else {
            return super.getNodeProperties(node, properties);
        }
    }

    @Override
    public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
        if (delegate != null) {
            return delegate.getEdgeProperties(src, edge, tgt, properties);
        } else {
            return super.getEdgeProperties(src, edge, tgt, properties);
        }
    }

    @Override
    public void writePreamble(Appendable a) throws IOException {}

    @Override
    public void writePostamble(Appendable a) throws IOException {}

}
