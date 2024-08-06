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
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import net.automatalib.automata.base.compact.CompactTransition;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transducers.MealyMachine.MealyGraphView;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.automata.transducers.impl.compact.CompactSST;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.io.UnclosableOutputStream;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import net.automatalib.graphs.base.DefaultCFMPS;
import net.automatalib.graphs.base.compact.CompactEdge;
import net.automatalib.graphs.base.compact.CompactGraph;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class DOTSerializationTest {

    @Test
    public void testRegularDFASerialization() throws IOException {

        final CompactDFA<String> dfa = DOTSerializationUtil.DFA;

        ThrowingWriter writer = w -> GraphDOT.write(dfa, dfa.getInputAlphabet(), w);
        checkDOTOutput(writer, DOTSerializationUtil.DFA_RESOURCE);
    }

    @Test
    public void testRegularNFASerialization() throws IOException {

        final CompactNFA<String> nfa = DOTSerializationUtil.NFA;

        ThrowingWriter writer = w -> GraphDOT.write(nfa, nfa.getInputAlphabet(), w);
        checkDOTOutput(writer, DOTSerializationUtil.NFA_RESOURCE);
    }

    @Test
    public void testRegularMealySerialization() throws IOException {

        final CompactMealy<String, String> mealy = DOTSerializationUtil.MEALY;

        ThrowingWriter writer = w -> GraphDOT.write(mealy, mealy.getInputAlphabet(), w);
        checkDOTOutput(writer, DOTSerializationUtil.MEALY_RESOURCE);
    }

    @Test
    public void testRegularMooreExport() throws IOException {

        final CompactMoore<String, String> moore = DOTSerializationUtil.MOORE;

        ThrowingWriter writer = w -> GraphDOT.write(moore, moore.getInputAlphabet(), w);
        checkDOTOutput(writer, DOTSerializationUtil.MOORE_RESOURCE);
    }

    @Test
    public void testRegularSSTExport() throws IOException {

        final CompactSST<Character, Character> sst = DOTSerializationUtil.SST;

        ThrowingWriter writer = w -> GraphDOT.write(sst, sst.getInputAlphabet(), w);
        checkDOTOutput(writer, DOTSerializationUtil.SST_RESOURCE);
    }

    @Test
    public void testRegularMTSExport() throws IOException {

        final CompactMTS<String> mts = DOTSerializationUtil.MTS;

        ThrowingWriter writer = w -> GraphDOT.write(mts.graphView(), w);
        checkDOTOutput(writer, DOTSerializationUtil.MTS_RESOURCE);
    }

    @Test
    public void testRegularMCExport() throws IOException {

        final CompactMC<String> mc = DOTSerializationUtil.MC;

        ThrowingWriter writer = w -> GraphDOT.write(mc.graphView(), w);
        checkDOTOutput(writer, DOTSerializationUtil.MC_RESOURCE);
    }

    @Test
    public void testRegularClusterExport() throws IOException {

        final Graph<?, ?> dfa = DOTSerializationUtil.DFA.graphView();
        final Graph<?, ?> mealy =
                new MealyGraphView<Integer, String, CompactTransition<String>, String, CompactMealy<String, String>>(
                        DOTSerializationUtil.MEALY,
                        DOTSerializationUtil.MEALY.getInputAlphabet()) {

                    @Override
                    public VisualizationHelper<Integer, TransitionEdge<String, CompactTransition<String>>> getVisualizationHelper() {
                        return new DefaultDOTVisualizationHelper<Integer, TransitionEdge<String, CompactTransition<String>>>(
                                super.getVisualizationHelper()) {

                            @Override
                            public void writePreamble(Appendable a) throws IOException {
                                a.append("color=blue;")
                                 .append(System.lineSeparator())
                                 .append("label=\"Mealy\";")
                                 .append(System.lineSeparator());
                            }
                        };
                    }
                };
        final Graph<?, ?> mc = DOTSerializationUtil.MC.graphView();

        final List<Graph<?, ?>> clusters = Arrays.asList(dfa, mealy, mc);

        ThrowingWriter writer = w -> GraphDOT.write(clusters, w);
        checkDOTOutput(writer, DOTSerializationUtil.CLUSTER_RESOURCE);
    }

    @Test
    public void testPMPGExport() throws IOException {

        final DefaultCFMPS<Character, Character> cfmps = DOTSerializationUtil.CFMPS;
        final ProceduralModalProcessGraph<?, Character, ?, Character, ?> pmpg = cfmps.getPMPGs().get('s');

        ThrowingWriter writer = w -> GraphDOT.write(pmpg, w);
        checkDOTOutput(writer, DOTSerializationUtil.PMPG_RESOURCE);
    }

    @Test
    public void testCFMPSExport() throws IOException {

        final DefaultCFMPS<Character, Character> cfmps = DOTSerializationUtil.CFMPS;

        ThrowingWriter writer = w -> GraphDOT.write(cfmps, w);
        checkDOTOutput(writer, DOTSerializationUtil.CFMPS_RESOURCE);
    }

    @Test
    public void testVisualizationHelper() throws IOException {

        final CompactGraph<String, String> graph = DOTSerializationUtil.GRAPH;

        ThrowingWriter writer = w -> GraphDOT.write(graph,
                                                    w,
                                                    GraphDOT.toDOTVisualizationHelper(new RedTransitionHelper<>()),
                                                    new PreambleHelper<>(),
                                                    new PropertyHelper<>(graph::getNodeProperty,
                                                                         graph::getEdgeProperty));
        checkDOTOutput(writer, DOTSerializationUtil.GRAPH_RESOURCE);
    }

    @Test
    public void testHTMLVisualizationHelper() throws IOException {

        final CompactGraph<String, String> graph = DOTSerializationUtil.GRAPH;

        ThrowingWriter writer = w -> GraphDOT.write(graph, w, new HTMLHelper<>());
        checkDOTOutput(writer, DOTSerializationUtil.GRAPH_HTML_RESOURCE);
    }

    @Test
    public void testGlobalVisualizationHelper() throws IOException {

        final CompactGraph<String, String> graph = DOTSerializationUtil.GRAPH;

        ThrowingWriter writer = w -> GraphDOT.write(graph, w, new GlobalHelper<>());
        checkDOTOutput(writer, DOTSerializationUtil.GRAPH_GLOBAL_RESOURCE);
    }

    @Test
    public void testNullVisualizationHelper() throws IOException {

        final CompactGraph<String, String> graph = DOTSerializationUtil.GRAPH;

        ThrowingWriter writer = w -> GraphDOT.write(graph, w, new NullHelper<>());
        checkDOTOutput(writer, DOTSerializationUtil.EMPTY_RESOURCE);
    }

    @Test
    public void doNotCloseOutputStreamTest() throws IOException {
        DOTSerializationProvider.<Integer, CompactEdge<String>>getInstance()
                                .writeModel(new UnclosableOutputStream(ByteStreams.nullOutputStream()),
                                            DOTSerializationUtil.GRAPH);
    }

    private void checkDOTOutput(ThrowingWriter writer, String resource) throws IOException {

        final StringWriter dotWriter = new StringWriter();
        final StringWriter expectedWriter = new StringWriter();

        try (Reader mealyReader = IOUtil.asBufferedUTF8Reader(DOTSerializationUtil.class.getResourceAsStream(resource))) {

            CharStreams.copy(mealyReader, expectedWriter);
            writer.write(dotWriter);

            Assert.assertEquals(dotWriter.toString(), expectedWriter.toString());
        }
    }

    private interface ThrowingWriter {

        void write(Writer w) throws IOException;
    }

    private static class RedTransitionHelper<E> implements VisualizationHelper<Integer, E> {

        @Override
        public boolean getNodeProperties(Integer node, Map<String, String> properties) {
            if (node % 2 == 0) {
                properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
            }
            return true;
        }

        @Override
        public boolean getEdgeProperties(Integer src, E edge, Integer tgt, Map<String, String> properties) {
            properties.put(NodeAttrs.COLOR, "red");
            return true;
        }
    }

    private static class PreambleHelper<N, E> extends DefaultDOTVisualizationHelper<N, E> {

        @Override
        public void writePreamble(Appendable a) throws IOException {
            a.append("// this is a preamble").append(System.lineSeparator());
        }

        @Override
        public void writePostamble(Appendable a) throws IOException {
            a.append("// this is a postamble").append(System.lineSeparator());
        }
    }

    private static class PropertyHelper<N, E, NP, EP> implements VisualizationHelper<N, E> {

        private final Function<N, NP> npExtractor;
        private final Function<E, EP> epExtractor;

        PropertyHelper(Function<N, NP> npExtractor, Function<E, EP> epExtractor) {
            this.npExtractor = npExtractor;
            this.epExtractor = epExtractor;
        }

        @Override
        public boolean getNodeProperties(N node, Map<String, String> properties) {
            properties.put(NodeAttrs.LABEL, String.valueOf(npExtractor.apply(node)));
            return true;
        }

        @Override
        public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
            properties.put(EdgeAttrs.LABEL, String.valueOf(epExtractor.apply(edge)));
            return true;
        }
    }

    private static class HTMLHelper<N, E> extends DefaultVisualizationHelper<N, E> {

        @Override
        public boolean getNodeProperties(N node, Map<String, String> properties) {
            // Both types of HTML markup should be supported
            if (Objects.hashCode(node) % 2 == 0) {
                properties.put(NodeAttrs.LABEL, "<HTML><U>" + node + "</U>");
            } else {
                properties.put(NodeAttrs.LABEL, "<HTML><U>" + node + "</U></HTML>");
            }
            return true;
        }
    }

    private static class GlobalHelper<N, E> implements VisualizationHelper<N, E> {

        @Override
        public void getGlobalNodeProperties(Map<String, String> properties) {
            properties.put(NodeAttrs.SHAPE, NodeShapes.OCTAGON);
        }

        @Override
        public void getGlobalEdgeProperties(Map<String, String> properties) {
            properties.put(EdgeAttrs.COLOR, "green");
        }

        @Override
        public boolean getNodeProperties(N node, Map<String, String> properties) {
            properties.clear();
            return true;
        }

        @Override
        public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
            properties.clear();
            return true;
        }
    }

    private static class NullHelper<N, E> implements VisualizationHelper<N, E> {

        @Override
        public boolean getNodeProperties(N node, Map<String, String> properties) {
            return false;
        }

        @Override
        public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
            return false;
        }
    }
}
