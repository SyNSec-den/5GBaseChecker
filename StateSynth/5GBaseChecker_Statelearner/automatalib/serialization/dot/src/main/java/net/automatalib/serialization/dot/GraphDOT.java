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

import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.commons.util.strings.StringUtil;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.UndirectedGraph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.VisualizationHelper.CommonAttrs;
import net.automatalib.visualization.VisualizationHelper.NodeAttrs;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Methods for rendering a {@link Graph} or {@link Automaton} in the GraphVIZ DOT format.
 *
 * @author Malte Isberner
 */
public final class GraphDOT {

    private static final String INITIAL_LABEL = "__start";
    private static final String HTML_START_TAG = "<HTML>";
    private static final String HTML_END_TAG = "</HTML>";

    private GraphDOT() {}

    public static void write(GraphViewable gv, Appendable a) throws IOException {
        Graph<?, ?> graph = gv.graphView();
        write(graph, a);
    }

    /**
     * Renders an {@link Automaton} in the GraphVIZ DOT format.
     *
     * @param automaton
     *         the automaton to render.
     * @param inputAlphabet
     *         the input alphabet to consider
     * @param a
     *         the appendable to write to
     *
     * @throws IOException
     *         if writing to {@code a} fails
     */
    public static <S, I, T> void write(Automaton<S, I, T> automaton,
                                       Collection<? extends I> inputAlphabet,
                                       Appendable a) throws IOException {
        write(automaton.transitionGraphView(inputAlphabet), a);
    }

    /**
     * Renders an {@link Automaton} in the GraphVIZ DOT format.
     *
     * @param automaton
     *         the automaton to render.
     * @param inputAlphabet
     *         the input alphabet to consider
     * @param a
     *         the appendable to write to
     * @param additionalHelpers
     *         additional helpers for providing visualization properties.
     *
     * @throws IOException
     *         if writing to {@code a} fails
     */
    @SafeVarargs
    public static <S, I, T> void write(Automaton<S, I, T> automaton,
                                       Collection<? extends I> inputAlphabet,
                                       Appendable a,
                                       VisualizationHelper<S, ? super TransitionEdge<I, T>>... additionalHelpers)
            throws IOException {
        write(automaton, inputAlphabet, a, Arrays.asList(additionalHelpers));
    }

    /**
     * Renders an {@link Automaton} in the GraphVIZ DOT format.
     *
     * @param automaton
     *         the automaton to render.
     * @param inputAlphabet
     *         the input alphabet to consider
     * @param a
     *         the appendable to write to
     * @param additionalHelpers
     *         additional helpers for providing visualization properties.
     *
     * @throws IOException
     *         if writing to {@code a} fails
     */
    public static <S, I, T> void write(Automaton<S, I, T> automaton,
                                       Collection<? extends I> inputAlphabet,
                                       Appendable a,
                                       List<VisualizationHelper<S, ? super TransitionEdge<I, T>>> additionalHelpers)
            throws IOException {
        write(automaton.transitionGraphView(inputAlphabet), a, additionalHelpers);
    }

    /**
     * Renders a {@link Graph} in the GraphVIZ DOT format.
     *
     * @param graph
     *         the graph to render
     * @param a
     *         the appendable to write to.
     *
     * @throws IOException
     *         if writing to {@code a} fails.
     */
    public static <N, E> void write(Graph<N, E> graph, Appendable a) throws IOException {
        writeRaw(graph, a, toDOTVisualizationHelper(graph.getVisualizationHelper()));
    }

    /**
     * Renders a {@link Graph} in the GraphVIZ DOT format.
     *
     * @param graph
     *         the graph to render
     * @param a
     *         the appendable to write to.
     * @param additionalHelpers
     *         additional helpers for providing visualization properties.
     *
     * @throws IOException
     *         if writing to {@code a} fails.
     */
    @SafeVarargs
    public static <N, E> void write(Graph<N, E> graph,
                                    Appendable a,
                                    VisualizationHelper<N, ? super E>... additionalHelpers) throws IOException {
        write(graph, a, Arrays.asList(additionalHelpers));
    }

    /**
     * Renders a {@link Graph} in the GraphVIZ DOT format.
     *
     * @param graph
     *         the graph to render
     * @param a
     *         the appendable to write to.
     * @param additionalHelpers
     *         additional helpers for providing visualization properties.
     *
     * @throws IOException
     *         if writing to {@code a} fails.
     */
    public static <N, E> void write(Graph<N, E> graph,
                                    Appendable a,
                                    List<VisualizationHelper<N, ? super E>> additionalHelpers) throws IOException {

        final List<VisualizationHelper<N, ? super E>> helpers = new ArrayList<>(additionalHelpers.size() + 1);

        helpers.add(graph.getVisualizationHelper());
        helpers.addAll(additionalHelpers);

        writeRaw(graph, a, toDOTVisualizationHelper(helpers));
    }

    /**
     * Renders a list of {@link Graph}s as clusters (subgraphs) in the GraphVIZ DOT format. Note that any markup
     * information for each cluster must be provided by the respective graph's {@link Graph#getVisualizationHelper()
     * visualization helper}.
     *
     * @param graphs
     *         the graphs to render
     * @param a
     *         the appendable to write to.
     *
     * @throws IOException
     *         if writing to {@code a} fails.
     */
    public static void write(List<Graph<?, ?>> graphs, Appendable a) throws IOException {

        boolean directed = false;

        for (Graph<?, ?> g : graphs) {
            // one directed graph is enough to require arrow tips
            if (!(g instanceof UndirectedGraph)) {
                directed = true;
                break;
            }
        }

        writeRawHeader(a, directed);

        int clusterId = 0;
        for (Graph<?, ?> g : graphs) {
            final String idPrefix = "c" + clusterId + '_';

            a.append(System.lineSeparator())
             .append("subgraph cluster")
             .append(Integer.toString(clusterId))
             .append(" {")
             .append(System.lineSeparator());

            @SuppressWarnings("unchecked")
            final Graph<Object, Object> graph = (Graph<Object, Object>) g;

            writeRawBody(graph,
                         a,
                         toDOTVisualizationHelper(graph.getVisualizationHelper()),
                         !(graph instanceof UndirectedGraph),
                         idPrefix);
            a.append('}').append(System.lineSeparator());

            clusterId++;
        }

        writeRawFooter(a);
    }

    /**
     * Renders a {@link Graph} in the GraphVIZ DOT format.
     *
     * @param graph
     *         the graph to render
     * @param a
     *         the appendable to write to
     * @param dotHelper
     *         the helper to use for rendering.
     *
     * @throws IOException
     *         if writing to {@code a} fails
     */
    private static <N, E> void writeRaw(Graph<N, E> graph, Appendable a, DOTVisualizationHelper<N, ? super E> dotHelper)
            throws IOException {

        final boolean directed = !(graph instanceof UndirectedGraph);

        writeRawHeader(a, directed);
        writeRawBody(graph, a, dotHelper, directed, "");
        writeRawFooter(a);

        if (a instanceof Flushable) {
            ((Flushable) a).flush();
        }
    }

    private static void writeRawHeader(Appendable a, boolean directed) throws IOException {
        if (directed) {
            a.append("di");
        }

        a.append("graph g {").append(System.lineSeparator());
    }

    private static <N, E> void writeRawBody(Graph<N, E> graph,
                                            Appendable a,
                                            DOTVisualizationHelper<N, ? super E> dotHelper,
                                            boolean directed,
                                            String idPrefix) throws IOException {

        Map<String, String> props = new HashMap<>();

        dotHelper.getGlobalNodeProperties(props);
        if (!props.isEmpty()) {
            a.append('\t').append("node");
            appendParams(props, a);
            a.append(';').append(System.lineSeparator());
        }

        props.clear();
        dotHelper.getGlobalEdgeProperties(props);
        if (!props.isEmpty()) {
            a.append('\t').append("edge");
            appendParams(props, a);
            a.append(';').append(System.lineSeparator());
        }

        dotHelper.writePreamble(a);
        a.append(System.lineSeparator());

        MutableMapping<N, @Nullable String> nodeNames = graph.createStaticNodeMapping();
        Set<String> initialNodes = new HashSet<>();

        int i = 0;

        for (N node : graph) {
            props.clear();
            if (!dotHelper.getNodeProperties(node, props)) {
                continue;
            }
            String id = idPrefix + "s" + i++;

            // remove potential attributes that are no valid DOT attributes
            if (Boolean.parseBoolean(props.remove(NodeAttrs.INITIAL))) {
                initialNodes.add(id);
            }
            props.remove(NodeAttrs.ACCEPTING);

            a.append('\t').append(id);
            appendParams(props, a);
            a.append(';').append(System.lineSeparator());
            nodeNames.put(node, id);
        }

        for (N node : graph) {
            String srcId = nodeNames.get(node);
            if (srcId == null) {
                continue;
            }
            Collection<E> outEdges = graph.getOutgoingEdges(node);
            if (outEdges.isEmpty()) {
                continue;
            }
            for (E e : outEdges) {
                N tgt = graph.getTarget(e);
                String tgtId = nodeNames.get(tgt);
                if (tgtId == null) {
                    continue;
                }

                if (!directed && tgtId.compareTo(srcId) < 0) {
                    continue;
                }

                props.clear();
                if (!dotHelper.getEdgeProperties(node, e, tgt, props)) {
                    continue;
                }

                a.append('\t').append(srcId).append(' ');
                if (directed) {
                    a.append("-> ");
                } else {
                    a.append("-- ");
                }
                a.append(tgtId);
                appendParams(props, a);
                a.append(';').append(System.lineSeparator());
            }
        }

        if (!initialNodes.isEmpty()) {
            a.append(System.lineSeparator());
            renderInitialArrowTip(initialNodes, idPrefix, a);
        }

        a.append(System.lineSeparator());
        dotHelper.writePostamble(a);
    }

    private static void writeRawFooter(Appendable a) throws IOException {
        a.append('}').append(System.lineSeparator());
    }

    private static void appendParams(Map<String, String> params, Appendable a) throws IOException {
        if (params.isEmpty()) {
            return;
        }
        a.append(" [");
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                a.append(' ');
            }
            String key = e.getKey();
            String value = e.getValue();
            a.append(e.getKey()).append("=");
            if (key.equals(CommonAttrs.LABEL)) {
                // HTML labels have to be enclosed in <> instead of ""
                final String upperCase = value.toUpperCase(Locale.ROOT);
                if (upperCase.startsWith(HTML_START_TAG)) {
                    a.append('<');
                    if (upperCase.endsWith(HTML_END_TAG)) {
                        a.append(value.substring(HTML_START_TAG.length(), value.length() - HTML_END_TAG.length()));
                    } else {
                        a.append(value.substring(HTML_START_TAG.length()));
                    }
                    a.append('>');
                } else {
                    StringUtil.enquote(e.getValue(), a);
                }
            } else {
                StringUtil.enquote(e.getValue(), a);
            }
        }
        a.append(']');
    }

    private static void renderInitialArrowTip(Set<String> initialNodes, String idPrefix, Appendable a)
            throws IOException {

        int i = 0;
        for (String init : initialNodes) {
            a.append(initialLabel(idPrefix, i))
             .append(" [label=\"\" shape=\"none\" width=\"0\" height=\"0\"];")
             .append(System.lineSeparator())
             .append(initialLabel(idPrefix, i++))
             .append(" -> ")
             .append(init)
             .append(';')
             .append(System.lineSeparator());
        }
    }

    static String initialLabel(int n) {
        return initialLabel("", n);
    }

    static String initialLabel(String idPrefix, int n) {
        return idPrefix + INITIAL_LABEL + n;
    }

    public static <N, E> DOTVisualizationHelper<N, E> toDOTVisualizationHelper(VisualizationHelper<N, E> helper) {
        if (helper instanceof DOTVisualizationHelper) {
            return (DOTVisualizationHelper<N, E>) helper;
        }

        return new DefaultDOTVisualizationHelper<>(helper);
    }

    public static <N, E> DOTVisualizationHelper<N, E> toDOTVisualizationHelper(List<VisualizationHelper<N, ? super E>> helpers) {

        final List<DOTVisualizationHelper<N, ? super E>> convertedHelpers = new ArrayList<>(helpers.size());

        for (VisualizationHelper<N, ? super E> h : helpers) {
            convertedHelpers.add(toDOTVisualizationHelper(h));
        }

        return new AggregateDOTVisualizationHelper<>(convertedHelpers);
    }
}
