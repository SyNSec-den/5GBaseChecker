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
package net.automatalib.visualization.jung;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.github.misberner.graphvizawtshapes.ShapeLibrary;
import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.VisualizationHelper.CommonAttrs;
import net.automatalib.visualization.VisualizationHelper.CommonStyles;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.NodeAttrs;
import net.automatalib.visualization.VisualizationProvider;
import net.automatalib.visualization.helper.AggregateVisualizationHelper;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(VisualizationProvider.class)
public class JungGraphVisualizationProvider implements VisualizationProvider {

    /**
     * the {@link #getId() id} of this {@link VisualizationProvider}.
     */
    public static final String ID = "jung";

    private static final Logger LOGGER = LoggerFactory.getLogger(JungGraphVisualizationProvider.class);

    private final PluggableGraphMouse mouse;

    public JungGraphVisualizationProvider() {
        final float zoomScaling = 1.1f;

        mouse = new PluggableGraphMouse();
        mouse.add(new PickingGraphMousePlugin<>());
        mouse.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON3_MASK));
        mouse.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1 / zoomScaling, zoomScaling));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean checkUsable() {
        return true;
    }

    @Override
    public <N, E> void visualize(Graph<N, E> graph,
                                 List<VisualizationHelper<N, ? super E>> additionalHelpers,
                                 boolean modal,
                                 Map<String, String> options) {

        DirectedGraph<NodeVisualization, EdgeVisualization> visGraph =
                createVisualizationGraph(graph, additionalHelpers);

        Layout<NodeVisualization, EdgeVisualization> layout = new KKLayout<>(visGraph);

        VisualizationViewer<NodeVisualization, EdgeVisualization> vv = new VisualizationViewer<>(layout);
        setupRenderContext(vv.getRenderContext());
        setupRenderer(vv.getRenderer());
        vv.setGraphMouse(mouse);

        final JDialog frame = new JDialog((Dialog) null, "Visualization", modal);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }

    public static <N, E> DirectedGraph<NodeVisualization, EdgeVisualization> createVisualizationGraph(Graph<N, E> graph,
                                                                                                      List<VisualizationHelper<N, ? super E>> additionalHelpers) {

        final VisualizationHelper<N, E> helper =
                new AggregateVisualizationHelper<>(graph.getVisualizationHelper(), additionalHelpers);

        Map<String, String> defaultProps = new HashMap<>();
        helper.getGlobalNodeProperties(defaultProps);

        DirectedGraph<NodeVisualization, EdgeVisualization> jungGraph = new DirectedSparseMultigraph<>();

        MutableMapping<N, @Nullable NodeVisualization> mapping = graph.createStaticNodeMapping();

        NodeIDs<N> nodeIds = graph.nodeIDs();

        for (N node : graph) {
            int id = nodeIds.getNodeId(node);
            Map<String, String> props = new HashMap<>(defaultProps);
            helper.getNodeProperties(node, props);
            NodeVisualization vis = createNodeVisualization(props, id);
            mapping.put(node, vis);
        }

        defaultProps = new HashMap<>();
        helper.getGlobalEdgeProperties(defaultProps);

        for (N node : graph) {
            NodeVisualization srcVis = mapping.get(node);
            for (E edge : graph.getOutgoingEdges(node)) {
                N target = graph.getTarget(edge);
                NodeVisualization tgtVis = mapping.get(target);
                Map<String, String> props = new HashMap<>(defaultProps);
                helper.getEdgeProperties(node, edge, target, props);
                EdgeVisualization edgeVis = createEdgeVisualization(props);
                jungGraph.addEdge(edgeVis, srcVis, tgtVis);
            }
        }

        return jungGraph;
    }

    public static void setupRenderContext(RenderContext<NodeVisualization, EdgeVisualization> ctx) {
        ctx.setVertexLabelTransformer(NodeVisualization.LABEL);
        ctx.setVertexDrawPaintTransformer(NodeVisualization.DRAW_COLOR);
        ctx.setVertexFillPaintTransformer(NodeVisualization.FILL_COLOR);
        ctx.setVertexShapeTransformer(NodeVisualization.SHAPE);
        ctx.setVertexStrokeTransformer(NodeVisualization.STROKE);

        ctx.setEdgeLabelTransformer(EdgeVisualization.LABEL);
        ctx.setEdgeDrawPaintTransformer(EdgeVisualization.DRAW_COLOR);
        ctx.setEdgeStrokeTransformer(EdgeVisualization.STROKE);
    }

    public static void setupRenderer(Renderer<NodeVisualization, EdgeVisualization> renderer) {
        renderer.getVertexLabelRenderer().setPosition(Position.AUTO);
    }

    protected static NodeVisualization createNodeVisualization(Map<String, String> props, int id) {
        String label = props.get(NodeAttrs.LABEL);
        if (label == null) {
            label = "v" + id;
        }
        Color drawColor = getColor(props, CommonAttrs.COLOR, Color.BLACK);
        Color fillColor = getColor(props, "fillcolor", Color.WHITE);

        String shapeName = props.get(NodeAttrs.SHAPE);
        if (shapeName == null) {
            shapeName = "circle";
        }
        ShapeLibrary shapeLib = ShapeLibrary.getInstance();

        Shape shape = shapeLib.createShape(shapeName);
        if (shape == null) {
            LOGGER.error("Could not create shape {}", shapeName);
            shape = shapeLib.createShape("circle");
        }

        final Stroke stroke = getStroke(props);

        return new NodeVisualization(label, drawColor, fillColor, shape, stroke);
    }

    protected static EdgeVisualization createEdgeVisualization(Map<String, String> props) {
        String label = props.get(EdgeAttrs.LABEL);
        if (label == null) {
            label = "";
        }
        final Color drawColor = getColor(props, CommonAttrs.COLOR, Color.BLACK);
        final Stroke stroke = getStroke(props);

        return new EdgeVisualization(label, drawColor, stroke);
    }

    protected static Color getColor(Map<String, String> props, String propName, Color defColor) {
        String colName = props.get(propName);
        if (colName == null) {
            return defColor;
        }
        Color col = Color.getColor(colName);
        if (col == null) {
            return defColor;
        }
        return col;
    }

    private static Stroke getStroke(Map<String, String> properties) {

        final List<String> styleList;
        String styleAttr = properties.get(NodeAttrs.STYLE);
        if (styleAttr != null) {
            styleList = Arrays.asList(styleAttr.toLowerCase(Locale.ROOT).split(","));
        } else {
            styleList = Collections.emptyList();
        }

        float penWidth = 1.0f;
        if (styleList.contains(CommonStyles.BOLD)) {
            penWidth = 3.0f;
        }

        final float miterLimit = 10.0f;
        if (styleList.contains(CommonStyles.DASHED)) {
            float[] dash = {miterLimit};
            return new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, miterLimit, dash, 0.0f);
        } else if (styleList.contains(CommonStyles.DOTTED)) {
            float[] dotted = {penWidth, miterLimit};
            return new BasicStroke(penWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, miterLimit, dotted, 0.0f);
        } else {
            return new BasicStroke(penWidth);
        }
    }

    @SuppressWarnings("nullness") // we don't pass null to the functions
    public static final class NodeVisualization {

        public static final Function<NodeVisualization, String> LABEL = input -> input.label;
        public static final Function<NodeVisualization, Paint> DRAW_COLOR = input -> input.color;
        public static final Function<NodeVisualization, Paint> FILL_COLOR = input -> input.fillColor;
        public static final Function<NodeVisualization, Shape> SHAPE = input -> input.shape;
        public static final Function<NodeVisualization, Stroke> STROKE = input -> input.stroke;

        public final String label;
        public final Color color;
        public final Color fillColor;
        public final Shape shape;
        public final Stroke stroke;

        public NodeVisualization(String label, Color drawColor, Color fillColor, Shape shape, Stroke stroke) {
            this.label = label;
            this.color = drawColor;
            this.fillColor = fillColor;
            this.shape = shape;
            this.stroke = stroke;
        }
    }

    @SuppressWarnings("nullness") // we don't pass null to the functions
    public static final class EdgeVisualization {

        public static final Function<EdgeVisualization, String> LABEL = input -> input.label;
        public static final Function<EdgeVisualization, Paint> DRAW_COLOR = input -> input.drawColor;
        public static final Function<EdgeVisualization, Stroke> STROKE = input -> input.stroke;

        public final String label;
        public final Color drawColor;
        public final Stroke stroke;

        public EdgeVisualization(String label, Color drawColor, Stroke stroke) {
            this.label = label;
            this.drawColor = drawColor;
            this.stroke = stroke;
        }
    }

}
