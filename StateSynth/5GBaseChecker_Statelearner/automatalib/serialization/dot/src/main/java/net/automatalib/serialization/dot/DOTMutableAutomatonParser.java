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
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.visualization.VisualizationHelper.NodeAttrs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * General-purpose DOT parser for {@link MutableAutomaton}s.
 *
 * @param <S>
 *         automaton state type
 * @param <I>
 *         input symbol type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 * @param <A>
 *         automaton type
 *
 * @author frohme
 */
public class DOTMutableAutomatonParser<S, I, SP, TP, A extends MutableAutomaton<S, I, ?, SP, TP>>
        implements DOTInputModelDeserializer<S, I, A> {

    private final AutomatonCreator<A, I> creator;
    private final Function<Map<String, String>, SP> nodeParser;
    private final Function<Map<String, String>, Pair<I, TP>> edgeParser;
    private final Collection<String> initialNodeIds;
    private final boolean fakeInitialNodeIds;

    /**
     * Parser for arbitrary {@link MutableAutomaton}s with a custom automaton instance, custom node and edge attributes
     * and custom labels for the initial nodes.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param nodeParser
     *         a node parser that extracts from a property map of a node the state property
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol and transition property
     * @param initialNodeIds
     *         the ids of the initial nodes
     * @param fakeInitialNodeIds
     *         a flag indicating whether the {@code initialNodeIds} are artificial or not. If {@code true}, the nodes
     *         matching the {@code initialNodeIds} will not be added to the automaton. Instead, their direct successors
     *         will be initial states instead. This may be useful for instances where there are artificial nodes used to
     *         display in incoming arrow for the actual initial states. If {@code false}, the nodes matching the
     *         {@code initialNodeIds} will be used as initial nodes.
     */
    public DOTMutableAutomatonParser(AutomatonCreator<A, I> creator,
                                     Function<Map<String, String>, SP> nodeParser,
                                     Function<Map<String, String>, Pair<I, TP>> edgeParser,
                                     Collection<String> initialNodeIds,
                                     boolean fakeInitialNodeIds) {
        this.creator = creator;
        this.nodeParser = nodeParser;
        this.edgeParser = edgeParser;
        this.initialNodeIds = initialNodeIds;
        this.fakeInitialNodeIds = fakeInitialNodeIds;
    }

    @Override
    public DOTInputModelData<S, I, A> readModel(InputStream is) throws IOException {

        try (Reader r = IOUtil.asUncompressedBufferedNonClosingUTF8Reader(is)) {
            InternalDOTParser parser = new InternalDOTParser(r);
            parser.parse();

            assert parser.isDirected();

            final Set<I> inputs = new HashSet<>();

            for (Edge edge : parser.getEdges()) {
                if (!fakeInitialNodeIds || !initialNodeIds.contains(edge.src)) {
                    inputs.add(edgeParser.apply(edge.attributes).getFirst());
                }
            }

            final Alphabet<I> alphabet = Alphabets.fromCollection(inputs);
            final A automaton = creator.createAutomaton(alphabet, parser.getNodes().size());

            final Mapping<S, String> labels = parseNodesAndEdges(parser, automaton);

            return new DOTInputModelData<>(automaton, alphabet, labels);
        }
    }

    private Mapping<S, String> parseNodesAndEdges(InternalDOTParser parser,
                                                  MutableAutomaton<S, I, ?, SP, TP> automaton) {
        final List<Node> nodes = parser.getNodes();
        final Map<String, S> stateMap = Maps.newHashMapWithExpectedSize(nodes.size());
        final MutableMapping<S, String> mapping = automaton.createDynamicStateMapping();

        for (Node node : nodes) {
            final S state;

            if (fakeInitialNodeIds && initialNodeIds.contains(node.id)) {
                continue;
            } else if (!fakeInitialNodeIds && initialNodeIds.contains(node.id)) {
                state = automaton.addInitialState(nodeParser.apply(node.attributes));
            } else {
                state = automaton.addState(nodeParser.apply(node.attributes));
            }

            stateMap.put(node.id, state);

            final String label = node.attributes.getOrDefault(NodeAttrs.LABEL, node.id);
            mapping.put(state, label);
        }

        for (Edge edge : parser.getEdges()) {
            if (fakeInitialNodeIds && initialNodeIds.contains(edge.src)) {
                automaton.setInitial(stateMap.get(edge.tgt), true);
            } else {
                final Pair<I, TP> property = edgeParser.apply(edge.attributes);
                automaton.addTransition(stateMap.get(edge.src),
                                        property.getFirst(),
                                        stateMap.get(edge.tgt),
                                        property.getSecond());
            }
        }

        return mapping;
    }
}
