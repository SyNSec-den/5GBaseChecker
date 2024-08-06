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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.automatalib.AutomataLibProperty;
import net.automatalib.AutomataLibSettings;
import net.automatalib.automata.Automaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Visualization {

    private static final Logger LOGGER = LoggerFactory.getLogger(Visualization.class);

    private static final Visualization INSTANCE = new Visualization();
    private final VisualizationProvider provider;

    private Visualization() {
        AutomataLibSettings settings = AutomataLibSettings.getInstance();

        String providerId = settings.getProperty(AutomataLibProperty.VISUALIZATION_PROVIDER);
        VisualizationProvider vp = null;

        VPManager manager = new VPManager();
        manager.load();

        if (providerId != null) {
            vp = manager.getProviderById(providerId);
            if (vp == null) {
                LOGGER.error("No provider found with id '{}'. Defaulting to dummy provider...", providerId);
            }
        }

        if (vp == null) {
            vp = manager.getBestProvider();
        }

        provider = vp;
    }

    public static <N, E> void visualize(Graph<N, E> graph) {
        visualize(graph, true);
    }

    @SafeVarargs
    public static <N, E> void visualize(Graph<N, E> graph, VisualizationHelper<N, ? super E>... additionalHelpers) {
        visualize(graph, true, additionalHelpers);
    }

    public static <N, E> void visualize(Graph<N, E> graph, boolean modal) {
        visualize(graph, modal, Collections.emptyMap());
    }

    @SafeVarargs
    public static <N, E> void visualize(Graph<N, E> graph,
                                        boolean modal,
                                        VisualizationHelper<N, ? super E>... additionalHelpers) {
        visualize(graph, modal, Collections.emptyMap(), additionalHelpers);
    }

    public static <N, E> void visualize(Graph<N, E> graph, boolean modal, Map<String, String> options) {
        INSTANCE.visualizeInternal(graph, modal, options);
    }

    @SafeVarargs
    public static <N, E> void visualize(Graph<N, E> graph,
                                        boolean modal,
                                        Map<String, String> options,
                                        VisualizationHelper<N, ? super E>... additionalHelpers) {
        INSTANCE.visualizeInternal(graph, modal, options, additionalHelpers);
    }

    public static void visualize(GraphViewable gv) {
        visualize(gv, true);
    }

    public static void visualize(GraphViewable gv, boolean modal) {
        visualize(gv, modal, Collections.emptyMap());
    }

    public static void visualize(GraphViewable gv, boolean modal, Map<String, String> options) {
        visualize(gv.graphView(), modal, options);
    }

    public static <S, I, T> void visualize(Automaton<S, I, T> graph, Collection<? extends I> inputs) {
        visualize(graph, inputs, true);
    }

    @SafeVarargs
    public static <S, I, T> void visualize(Automaton<S, I, T> graph,
                                           Collection<? extends I> inputs,
                                           VisualizationHelper<S, ? super TransitionEdge<I, T>>... additionalHelpers) {
        visualize(graph, inputs, true, additionalHelpers);
    }

    public static <S, I, T> void visualize(Automaton<S, I, T> graph, Collection<? extends I> inputs, boolean modal) {
        visualize(graph, inputs, modal, Collections.emptyMap());
    }

    @SafeVarargs
    public static <S, I, T> void visualize(Automaton<S, I, T> graph,
                                           Collection<? extends I> inputs,
                                           boolean modal,
                                           VisualizationHelper<S, ? super TransitionEdge<I, T>>... additionalHelpers) {
        visualize(graph, inputs, modal, Collections.emptyMap(), additionalHelpers);
    }

    public static <S, I, T> void visualize(Automaton<S, I, T> graph,
                                           Collection<? extends I> inputs,
                                           boolean modal,
                                           Map<String, String> options) {
        INSTANCE.visualizeInternal(graph.transitionGraphView(inputs), modal, options);
    }

    @SafeVarargs
    public static <S, I, T> void visualize(Automaton<S, I, T> graph,
                                           Collection<? extends I> inputs,
                                           boolean modal,
                                           Map<String, String> options,
                                           VisualizationHelper<S, ? super TransitionEdge<I, T>>... additionalHelpers) {
        INSTANCE.visualizeInternal(graph.transitionGraphView(inputs), modal, options, additionalHelpers);
    }

    private <N, E> void visualizeInternal(Graph<N, E> graph, boolean modal, Map<String, String> options) {
        provider.visualize(graph, Collections.emptyList(), modal, options);
    }

    @SafeVarargs
    private final <N, E> void visualizeInternal(Graph<N, E> graph,
                                                boolean modal,
                                                Map<String, String> options,
                                                VisualizationHelper<N, ? super E>... additionalHelpers) {
        provider.visualize(graph, Arrays.asList(additionalHelpers), modal, options);
    }

}
