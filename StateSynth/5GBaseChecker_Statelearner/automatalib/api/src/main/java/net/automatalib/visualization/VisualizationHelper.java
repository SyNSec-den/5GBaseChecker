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
package net.automatalib.visualization;

import java.util.Map;

/**
 * Helper interface for providing additional styling properties for plotting graphs.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 *
 * @author Malte Isberner
 */
public interface VisualizationHelper<N, E> {

    default void getGlobalNodeProperties(Map<String, String> properties) {}

    default void getGlobalEdgeProperties(Map<String, String> properties) {}

    /**
     * Retrieves the properties for rendering a single node. Additionally, the return value allows to control whether or
     * not to omit this node from rendering. If {@code false} is returned, the node will not be rendered. Consequently,
     * any modifications to the properties map will have no effect.
     * <p>
     * The properties are stored in the {@link Map} argument. Note that if an implementation of a base class is
     * overridden, it is probably a good idea to call {@code super.getNodeProperties(node, properties);} at the
     * beginning of the method.
     *
     * @param node
     *         the node to be rendered
     * @param properties
     *         the property map
     *
     * @return whether this node should be rendered
     */
    boolean getNodeProperties(N node, Map<String, String> properties);

    /**
     * Retrieves the properties for rendering a single edge. Additionally, the return value allows to control whether or
     * not to omit this edge from rendering. If {@code false} is returned, the edge will not be rendered. Consequently,
     * any modifications to the properties map will have no effect.
     * <p>
     * The properties are stored in the {@link Map} argument. Note that if an implementation of a base class is
     * overridden, it is probably a good idea to call {@code super.getEdgeProperties(node, properties);} at the
     * beginning of the method.
     *
     * @param edge
     *         the edge to be rendered
     * @param properties
     *         the property map
     *
     * @return whether this edge should be rendered
     */
    boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties);

    class CommonAttrs {

        public static final String LABEL = "label";
        public static final String COLOR = "color";
        public static final String TEXLBL = "texlbl";
        public static final String STYLE = "style";

        private CommonAttrs() {
            // prevent instantiation
        }
    }

    final class NodeAttrs extends CommonAttrs {

        public static final String SHAPE = "shape";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        public static final String FIXEDSIZE = "fixedsize";

        public static final String INITIAL = "initial";
        public static final String ACCEPTING = "accepting";

        private NodeAttrs() {
            // prevent instantiation
        }
    }

    class EdgeAttrs extends CommonAttrs {

        public static final String PENWIDTH = "penwidth";
        public static final String ARROWHEAD = "arrowhead";

        private EdgeAttrs() {
            // prevent instantiation
        }
    }

    final class NodeShapes {

        public static final String NONE = "none";

        public static final String OVAL = "oval";
        public static final String DOUBLEOVAL = "doubleoval";

        public static final String CIRCLE = "circle";
        public static final String DOUBLECIRCLE = "doublecircle";

        public static final String OCTAGON = "octagon";
        public static final String DOUBLEOCTAGON = "doubleoctagon";

        public static final String BOX = "box";

        private NodeShapes() {
            // prevent instantiation
        }
    }

    class CommonStyles {

        public static final String DASHED = "dashed";
        public static final String DOTTED = "dotted";
        public static final String SOLID = "solid";
        public static final String BOLD = "bold";

        private CommonStyles() {
            // prevent instantiation
        }
    }

    final class NodeStyles extends CommonStyles {

        public static final String FILLED = "filled";
        public static final String INVISIBLE = "invisible";
        public static final String DIAGONALS = "diagonals";
        public static final String ROUNDED = "rounded";

        private NodeStyles() {
            // prevent instantiation
        }
    }

    final class EdgeStyles extends CommonStyles {

        private EdgeStyles() {
            // prevent instantiation
        }
    }

    class MTSEdgeAttrs extends EdgeAttrs {

        public static final String MODALITY = "modality";

        private MTSEdgeAttrs() {
            // prevent instantiation
        }
    }

    class MCEdgeAttrs extends MTSEdgeAttrs {

        public static final String CONTRACT = "contract";

        private MCEdgeAttrs() {
            // prevent instantiation
        }
    }

    final class MMCEdgeAttrs extends MCEdgeAttrs {

        public static final String MEMBERSHIP = "group";

        private MMCEdgeAttrs() {
            // prevent instantiation
        }
    }
}
