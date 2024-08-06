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

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.FiniteStateAcceptor;
import net.automatalib.automata.fsa.MutableFSA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.MutableMooreMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.automata.visualization.MCVisualizationHelper;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.graphs.base.compact.CompactGraph;
import net.automatalib.serialization.ModelDeserializer;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.ModalContract;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalContract;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalContractEdgeProperty.EdgeColor;
import net.automatalib.ts.modal.transition.ModalContractEdgePropertyImpl;
import net.automatalib.ts.modal.transition.ModalContractMembershipEdgePropertyImpl;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.transition.MutableModalContractEdgeProperty;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.MCEdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.MMCEdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.MTSEdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.NodeAttrs;
import net.automatalib.visualization.VisualizationHelper.NodeShapes;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An aggregation of factory methods for obtaining DOT parsers for several types of automata / graphs.
 *
 * @author frohme
 */
public final class DOTParsers {

    /**
     * Node property parser that parses a node's "{@link NodeAttrs#LABEL label}" attribute and returns its {@link
     * Object#toString() string} representation. Returns {@code null} if the attribute is not specified.
     */
    public static final Function<Map<String, String>, @Nullable String> DEFAULT_NODE_PARSER =
            attr -> attr.get(NodeAttrs.LABEL);

    /**
     * Node property parser that returns {@code true} if a node's "{@link NodeAttrs#SHAPE shape}" attribute is specified
     * and equals "{@link NodeShapes#DOUBLECIRCLE doublecircle}". Returns {@code false} otherwise.
     */
    public static final Function<Map<String, String>, Boolean> DEFAULT_FSA_NODE_PARSER =
            attr -> NodeShapes.DOUBLECIRCLE.equals(attr.get(NodeAttrs.SHAPE));

    /**
     * Node property parser that expects a node's "{@link NodeAttrs#LABEL label}" attribute to be of the form {@code
     * <id>/<property>}. Returns the string representation of {@code <property>} as-is. Returns {@code null} if the
     * attribute does not exist or does not match the expected format.
     */
    public static final Function<Map<String, String>, @Nullable String> DEFAULT_MOORE_NODE_PARSER = attr -> {
        final String label = attr.get(NodeAttrs.LABEL);
        if (label == null) {
            return null;
        }

        final String[] tokens = label.split("/");

        if (tokens.length != 2) {
            return null;
        }

        return tokens[1].trim();
    };

    /**
     * Edge input parser that parses an edges's "{@link EdgeAttrs#LABEL label}" attribute and returns its {@link
     * Object#toString() string} representation. Returns {@code null} if the attribute is not specified.
     */
    public static final Function<Map<String, String>, @Nullable String> DEFAULT_EDGE_PARSER =
            attr -> attr.get(EdgeAttrs.LABEL);

    /**
     * Edge input parser that expects an edge's "{@link EdgeAttrs#LABEL label}" attribute to be of the form {@code
     * <input>/<property>}. Returns a {@link Pair} object containing the string representation of both components
     * as-is. Returns {@code null} if the attribute does not exist or does not match the expected format.
     */
    public static final Function<Map<String, String>, Pair<@Nullable String, @Nullable String>>
            DEFAULT_MEALY_EDGE_PARSER = attr -> {
        final String label = attr.get(EdgeAttrs.LABEL);
        if (label == null) {
            return Pair.of(null, null);
        }

        final String[] tokens = label.split("/");

        if (tokens.length != 2) {
            return Pair.of(null, null);
        }

        return Pair.of(tokens[0].trim(), tokens[1].trim());
    };

    /**
     * Edge input parser that parses an edges's {@link MTSEdgeAttrs#MODALITY modality} attribute and constructs a
     * corresponding {@link MutableModalEdgeProperty}. Throws an {@link IllegalArgumentException} if the attribute is
     * missing or doesn't match its expected {@link ModalType format}.
     */
    public static final Function<Map<String, String>, MutableModalEdgeProperty> DEFAULT_MTS_EDGE_PARSER = attr -> {
        final String type = getAndRequireNotNull(attr, MTSEdgeAttrs.MODALITY);

        return new ModalEdgePropertyImpl(ModalType.valueOf(type.toUpperCase(Locale.ROOT)));
    };

    /**
     * Edge input parser that parses an edges's {@link MTSEdgeAttrs#MODALITY modality} and {@link EdgeAttrs#COLOR color}
     * attribute and constructs a corresponding {@link MutableModalContractEdgeProperty}. Throws an {@link
     * IllegalArgumentException} if any of the two attributes are missing or doesn't match its expected format.
     */
    public static final Function<Map<String, String>, MutableModalContractEdgeProperty> DEFAULT_MC_EDGE_PARSER =
            attr -> {
                final String type = getAndRequireNotNull(attr, MTSEdgeAttrs.MODALITY);
                final String contract = getAndRequireNotNull(attr, MCEdgeAttrs.CONTRACT);

                return new ModalContractEdgePropertyImpl(ModalType.valueOf(type.toUpperCase(Locale.ROOT)),
                                                         MCVisualizationHelper.TAU.equals(attr.get(EdgeAttrs.LABEL)),
                                                         EdgeColor.valueOf(contract.toUpperCase(Locale.ROOT)));
            };

    /**
     * Edge input parser that parses an edges's {@link MTSEdgeAttrs#MODALITY modality} and {@link EdgeAttrs#COLOR color}
     * attribute and constructs a corresponding {@link MutableModalContractEdgeProperty}. Throws an {@link
     * IllegalArgumentException} if any of the two attributes are missing or doesn't match its expected format.
     */
    public static final Function<Map<String, String>, ModalContractMembershipEdgePropertyImpl> DEFAULT_MMC_EDGE_PARSER =
            attr -> {
                final String type = getAndRequireNotNull(attr, MTSEdgeAttrs.MODALITY);
                final String contract = getAndRequireNotNull(attr, MCEdgeAttrs.CONTRACT);
                final String membership = getAndRequireNotNull(attr, MMCEdgeAttrs.MEMBERSHIP);

                return new ModalContractMembershipEdgePropertyImpl(ModalType.valueOf(type.toUpperCase(Locale.ROOT)),
                                                                   MCVisualizationHelper.TAU.equals(attr.get(EdgeAttrs.LABEL)),
                                                                   EdgeColor.valueOf(contract.toUpperCase(Locale.ROOT)),
                                                                   Integer.parseInt(membership));
            };

    private DOTParsers() {}

    /**
     * Default parser for {@link DFA}s serialized by AutomataLib.
     * <p>
     * Invokes {@link #dfa(Function, Function)} with {@link #DEFAULT_FSA_NODE_PARSER} as {@code nodeParser} and {@link
     * #DEFAULT_EDGE_PARSER} as {@code edgeParser}.
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactDFA}s.
     */
    public static DOTInputModelDeserializer<Integer, @Nullable String, CompactDFA<@Nullable String>> dfa() {
        return dfa(DEFAULT_FSA_NODE_PARSER, DEFAULT_EDGE_PARSER);
    }

    /**
     * Parser for {@link DFA}s with custom node and edge attributes.
     * <p>
     * Invokes {@link #fsa(AutomatonCreator, Function, Function)} with {@link CompactDFA.Creator} as {@code creator}.
     *
     * @param nodeParser
     *         a node parser that decides for a property map of a node whether it is accepting or not
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param <I>
     *         the input symbol type
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactDFA}s.
     */
    public static <I> DOTInputModelDeserializer<Integer, I, CompactDFA<I>> dfa(Function<Map<String, String>, Boolean> nodeParser,
                                                                               Function<Map<String, String>, I> edgeParser) {
        return fsa(new CompactDFA.Creator<>(), nodeParser, edgeParser);
    }

    /**
     * Default parser for {@link NFA}s serialized by AutomataLib.
     * <p>
     * Invokes {@link #nfa(Function, Function)} with {@link #DEFAULT_FSA_NODE_PARSER} as {@code nodeParser} and {@link
     * #DEFAULT_EDGE_PARSER} as {@code edgeParser}.
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactNFA}s.
     */
    public static DOTInputModelDeserializer<Integer, @Nullable String, CompactNFA<@Nullable String>> nfa() {
        return nfa(DEFAULT_FSA_NODE_PARSER, DEFAULT_EDGE_PARSER);
    }

    /**
     * Parser for {@link NFA}s with custom node and edge attributes.
     * <p>
     * Invokes {@link #fsa(AutomatonCreator, Function, Function)} with {@link CompactNFA.Creator} as {@code creator}.
     *
     * @param nodeParser
     *         a node parser that decides for a property map of a node whether it is accepting or not
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param <I>
     *         the input symbol type
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactNFA}s.
     */
    public static <I> DOTInputModelDeserializer<Integer, I, CompactNFA<I>> nfa(Function<Map<String, String>, Boolean> nodeParser,
                                                                               Function<Map<String, String>, I> edgeParser) {
        return fsa(new CompactNFA.Creator<>(), nodeParser, edgeParser);
    }

    /**
     * Parser for {@link FiniteStateAcceptor}s with a custom automaton instance and custom node and edge attributes.
     * <p>
     * Invokes {@link #fsa(AutomatonCreator, Function, Function, Collection)} with AutomataLib's default initial state
     * label "{@code __start0}" as {@code initialNodeLabels}.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param nodeParser
     *         a node parser that decides for a property map of a node whether it is accepting or not
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param <S>
     *         the state type of the returned automaton
     * @param <I>
     *         the input symbol type
     * @param <A>
     *         the type of the returned automaton
     *
     * @return a {@link DOTInputModelDeserializer} for {@code A}s.
     */
    public static <S, I, A extends MutableFSA<S, I>> DOTInputModelDeserializer<S, I, A> fsa(AutomatonCreator<A, I> creator,
                                                                                            Function<Map<String, String>, Boolean> nodeParser,
                                                                                            Function<Map<String, String>, I> edgeParser) {
        return fsa(creator, nodeParser, edgeParser, Collections.singleton(GraphDOT.initialLabel(0)));
    }

    /**
     * Parser for {@link FiniteStateAcceptor}s with a custom automaton instance, custom node and edge attributes and
     * custom labels for the initial nodes.
     * <p>
     * Invokes {@link #fsa(AutomatonCreator, Function, Function, Collection, boolean)} with {@code true} as {@code
     * fakeInitialNodeLabels}.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param nodeParser
     *         a node parser that decides for a property map of a node whether it is accepting or not
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param initialNodeIds
     *         the ids of the initial nodes
     * @param <S>
     *         the state type of the returned automaton
     * @param <I>
     *         the input symbol type
     * @param <A>
     *         the type of the returned automaton
     *
     * @return a {@link DOTInputModelDeserializer} for {@code A}s.
     */
    public static <S, I, A extends MutableFSA<S, I>> DOTInputModelDeserializer<S, I, A> fsa(AutomatonCreator<A, I> creator,
                                                                                            Function<Map<String, String>, Boolean> nodeParser,
                                                                                            Function<Map<String, String>, I> edgeParser,
                                                                                            Collection<String> initialNodeIds) {
        return fsa(creator, nodeParser, edgeParser, initialNodeIds, true);
    }

    /**
     * Parser for {@link FiniteStateAcceptor}s with a custom automaton instance, custom node and edge attributes, custom
     * labels for initial nodes and a flag whether the initial nodes are artificial.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param nodeParser
     *         a node parser that decides for a property map of a node whether it is accepting or not
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param initialNodeIds
     *         the ids of the initial nodes
     * @param fakeInitialNodeIds
     *         a flag indicating whether the {@code initialNodeIds} are artificial or not. If {@code true}, the nodes
     *         matching the {@code initialNodeIds} will not be added to the automaton. Instead, their direct successors
     *         will be initial states instead. This may be useful for instances where there are artificial nodes used to
     *         display in incoming arrow for the actual initial states. If {@code false}, the nodes matching the
     *         {@code initialNodeIds} will be used as initial nodes.
     * @param <S>
     *         the state type of the returned automaton
     * @param <I>
     *         the input symbol type
     * @param <A>
     *         the type of the returned automaton
     *
     * @return a {@link DOTInputModelDeserializer} for {@code A}s.
     */
    public static <S, I, A extends MutableFSA<S, I>> DOTInputModelDeserializer<S, I, A> fsa(AutomatonCreator<A, I> creator,
                                                                                            Function<Map<String, String>, Boolean> nodeParser,
                                                                                            Function<Map<String, String>, I> edgeParser,
                                                                                            Collection<String> initialNodeIds,
                                                                                            boolean fakeInitialNodeIds) {
        return new DOTMutableAutomatonParser<>(creator,
                                               nodeParser,
                                               edge -> Pair.of(edgeParser.apply(edge), null),
                                               initialNodeIds,
                                               fakeInitialNodeIds);
    }

    /**
     * Default parser for {@link MealyMachine}s serialized by AutomataLib.
     * <p>
     * Invokes {@link #mealy(Function)} with {@link #DEFAULT_MEALY_EDGE_PARSER} as {@code edgeParser}.
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactMealy}s.
     */
    public static DOTInputModelDeserializer<Integer, @Nullable String, CompactMealy<@Nullable String, @Nullable String>> mealy() {
        return mealy(DEFAULT_MEALY_EDGE_PARSER);
    }

    /**
     * Parser for {@link MealyMachine}s with custom edge attributes.
     * <p>
     * Invokes {@link #mealy(AutomatonCreator, Function)} with {@link CompactMealy.Creator} as {@code creator}.
     *
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol and transition property
     * @param <I>
     *         the input symbol type
     * @param <O>
     *         the output symbol type
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactMealy}s.
     */
    public static <I, O> DOTInputModelDeserializer<Integer, I, CompactMealy<I, O>> mealy(Function<Map<String, String>, Pair<I, O>> edgeParser) {
        return mealy(new CompactMealy.Creator<>(), edgeParser);
    }

    /**
     * Parser for {@link MealyMachine}s with a custom automaton instance and custom edge attributes.
     * <p>
     * Invokes {@link #mealy(AutomatonCreator, Function, String)} with AutomataLib's default initial state label "{@code
     * __start0}" as {@code initialNodeLabel}.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol and transition property
     * @param <S>
     *         the state type of the returned automaton
     * @param <I>
     *         the input symbol type
     * @param <O>
     *         the output symbol type
     * @param <A>
     *         the type of the returned automaton
     *
     * @return a {@link DOTInputModelDeserializer} for {@code A}s.
     */
    public static <S, I, O, A extends MutableMealyMachine<S, I, ?, O>> DOTInputModelDeserializer<S, I, A> mealy(
            AutomatonCreator<A, I> creator,
            Function<Map<String, String>, Pair<I, O>> edgeParser) {
        return mealy(creator, edgeParser, GraphDOT.initialLabel(0));
    }

    /**
     * Parser for {@link MealyMachine}s with a custom automaton instance, custom edge attributes and a custom label for
     * the initial node.
     * <p>
     * Invokes {@link #fsa(AutomatonCreator, Function, Function, Collection, boolean)} with {@code true} as {@code
     * fakeInitialNodeLabels}.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol and transition property
     * @param initialNodeId
     *         the id of the initial node
     * @param <S>
     *         the state type of the returned automaton
     * @param <I>
     *         the input symbol type
     * @param <O>
     *         the output symbol type
     * @param <A>
     *         the type of the returned automaton
     *
     * @return a {@link DOTInputModelDeserializer} for {@code A}s.
     */
    public static <S, I, O, A extends MutableMealyMachine<S, I, ?, O>> DOTInputModelDeserializer<S, I, A> mealy(
            AutomatonCreator<A, I> creator,
            Function<Map<String, String>, Pair<I, O>> edgeParser,
            String initialNodeId) {
        return mealy(creator, edgeParser, initialNodeId, true);
    }

    /**
     * Parser for {@link MealyMachine}s with a custom automaton instance, custom edge attributes and a custom label for
     * the initial node and a flag whether the initial nodes are artificial.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol and transition property
     * @param initialNodeId
     *         the id of the initial node
     * @param fakeInitialNodeId
     *         a flag indicating whether the {@code initialNodeId} are artificial or not. If {@code true}, the nodes
     *         matching the {@code initialNodeId} will not be added to the automaton. Instead, their direct successors
     *         will be initial states instead. This may be useful for instances where there are artificial nodes used to
     *         display in incoming arrow for the actual initial states. If {@code false}, the nodes matching the
     *         {@code initialNodeId} will be used as initial nodes.
     * @param <S>
     *         the state type of the returned automaton
     * @param <I>
     *         the input symbol type
     * @param <O>
     *         the output symbol type
     * @param <A>
     *         the type of the returned automaton
     *
     * @return a {@link DOTInputModelDeserializer} for {@code A}s.
     */
    public static <S, I, O, A extends MutableMealyMachine<S, I, ?, O>> DOTInputModelDeserializer<S, I, A> mealy(
            AutomatonCreator<A, I> creator,
            Function<Map<String, String>, Pair<I, O>> edgeParser,
            String initialNodeId,
            boolean fakeInitialNodeId) {
        return new DOTMutableAutomatonParser<>(creator,
                                               node -> null,
                                               edgeParser,
                                               Collections.singleton(initialNodeId),
                                               fakeInitialNodeId);
    }

    /**
     * Default parser for {@link MooreMachine}s serialized by AutomataLib.
     * <p>
     * Invokes {@link #moore(Function, Function)} with {@link #DEFAULT_MOORE_NODE_PARSER} as {@code nodeParser} and
     * {@link #DEFAULT_EDGE_PARSER} as {@code edgeParser}.
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactMoore}s.
     */
    public static DOTInputModelDeserializer<Integer, @Nullable String, CompactMoore<@Nullable String, @Nullable String>> moore() {
        return moore(DEFAULT_MOORE_NODE_PARSER, DEFAULT_EDGE_PARSER);
    }

    /**
     * Parser for {@link MooreMachine}s with custom node and edge attributes.
     * <p>
     * Invokes {@link #moore(AutomatonCreator, Function, Function)} with {@link CompactMoore.Creator} as {@code
     * creator}.
     *
     * @param nodeParser
     *         a node parser that extracts from a property map of a node the state property
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param <I>
     *         the input symbol type
     * @param <O>
     *         the output symbol type
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactMoore}s.
     */
    public static <I, O> DOTInputModelDeserializer<Integer, I, CompactMoore<I, O>> moore(Function<Map<String, String>, O> nodeParser,
                                                                                         Function<Map<String, String>, I> edgeParser) {
        return moore(new CompactMoore.Creator<>(), nodeParser, edgeParser);

    }

    /**
     * Parser for {@link MooreMachine}s with a custom automaton instance and custom node and edge attributes.
     * <p>
     * Invokes {@link #moore(AutomatonCreator, Function, Function, String)} with AutomataLib's default initial state
     * label "{@code __start0}" as {@code initialNodeLabel}.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param nodeParser
     *         a node parser that extracts from a property map of a node the state property
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param <S>
     *         the state type of the returned automaton
     * @param <I>
     *         the input symbol type
     * @param <O>
     *         the output symbol type
     * @param <A>
     *         the type of the returned automaton
     *
     * @return a {@link DOTInputModelDeserializer} for {@code A}s.
     */
    public static <S, I, O, A extends MutableMooreMachine<S, I, ?, O>> DOTInputModelDeserializer<S, I, A> moore(
            AutomatonCreator<A, I> creator,
            Function<Map<String, String>, O> nodeParser,
            Function<Map<String, String>, I> edgeParser) {
        return moore(creator, nodeParser, edgeParser, GraphDOT.initialLabel(0));
    }

    /**
     * Parser for {@link MooreMachine}s with a custom automaton instance, custom node and edge attributes and a custom
     * label for the initial node.
     * <p>
     * Invokes {@link #moore(AutomatonCreator, Function, Function, String, boolean)} with {@code true} as {@code
     * fakeInitialNodeLabel}.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param nodeParser
     *         a node parser that extracts from a property map of a node the state property
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param initialNodeId
     *         the id of the initial node
     * @param <S>
     *         the state type of the returned automaton
     * @param <I>
     *         the input symbol type
     * @param <O>
     *         the output symbol type
     * @param <A>
     *         the type of the returned automaton
     *
     * @return a {@link DOTInputModelDeserializer} for {@code A}s.
     */
    public static <S, I, O, A extends MutableMooreMachine<S, I, ?, O>> DOTInputModelDeserializer<S, I, A> moore(
            AutomatonCreator<A, I> creator,
            Function<Map<String, String>, O> nodeParser,
            Function<Map<String, String>, I> edgeParser,
            String initialNodeId) {
        return moore(creator, nodeParser, edgeParser, initialNodeId, true);
    }

    /**
     * Parser for {@link MooreMachine}s with a custom automaton instance, custom node and edge attributes, a custom
     * label for the initial node and a flag whether the initial node is artificial.
     *
     * @param creator
     *         a creator that is used to instantiate the returned automaton
     * @param nodeParser
     *         a node parser that extracts from a property map of a node the state property
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param initialNodeId
     *         the id of the initial node
     * @param fakeInitialNodeId
     *         a flag indicating whether the {@code initialNodeId} are artificial or not. If {@code true}, the nodes
     *         matching the {@code initialNodeId} will not be added to the automaton. Instead, their direct successors
     *         will be initial states instead. This may be useful for instances where there are artificial nodes used to
     *         display in incoming arrow for the actual initial states. If {@code false}, the nodes matching the
     *         {@code initialNodeId} will be used as initial nodes.
     * @param <S>
     *         the state type of the returned automaton
     * @param <I>
     *         the input symbol type
     * @param <O>
     *         the output symbol type
     * @param <A>
     *         the type of the returned automaton
     *
     * @return a {@link DOTInputModelDeserializer} for {@code A}s.
     */
    public static <S, I, O, A extends MutableMooreMachine<S, I, ?, O>> DOTInputModelDeserializer<S, I, A> moore(
            AutomatonCreator<A, I> creator,
            Function<Map<String, String>, O> nodeParser,
            Function<Map<String, String>, I> edgeParser,
            String initialNodeId,
            boolean fakeInitialNodeId) {
        return new DOTMutableAutomatonParser<>(creator,
                                               nodeParser,
                                               edge -> Pair.of(edgeParser.apply(edge), null),
                                               Collections.singleton(initialNodeId),
                                               fakeInitialNodeId);
    }

    /**
     * Default parser for (directed) {@link Graph}s serialized by AutomataLib.
     * <p>
     * Invokes {@link #graph(Function, Function)} with {@link #DEFAULT_NODE_PARSER} as {@code nodeParser} and {@link
     * #DEFAULT_EDGE_PARSER} as {@code edgeParser}.
     *
     * @return a DOT {@link ModelDeserializer} for {@link CompactGraph}s.
     */
    public static ModelDeserializer<CompactGraph<@Nullable String, @Nullable String>> graph() {
        return graph(DEFAULT_NODE_PARSER, DEFAULT_EDGE_PARSER);
    }

    /**
     * Parser for (directed) {@link Graph}s with custom node and edge attributes.
     * <p>
     * Invokes {@link #graph(Supplier, Function, Function)} with {@link CompactGraph#CompactGraph()} as {@code
     * creator}.
     *
     * @param nodeParser
     *         a node parser that extracts from a property map of a node the node property
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the edge property
     * @param <NP>
     *         the node property type
     * @param <EP>
     *         the edge property type
     *
     * @return a DOT {@link ModelDeserializer} for {@link CompactGraph}s.
     */
    public static <NP, EP> ModelDeserializer<CompactGraph<NP, EP>> graph(Function<Map<String, String>, NP> nodeParser,
                                                                         Function<Map<String, String>, EP> edgeParser) {
        return graph(CompactGraph::new, nodeParser, edgeParser);
    }

    /**
     * Parser for (directed) {@link Graph}s with a custom graph instance and custom node and edge attributes.
     *
     * @param creator
     *         a creator that is used to instantiate the returned graph
     * @param nodeParser
     *         a node parser that extracts from a property map of a node the node property
     * @param edgeParser
     *         an edge parser that extracts from a property map of an edge the edge property
     * @param <NP>
     *         the node property type
     * @param <EP>
     *         the edge property type
     * @param <G>
     *         the graph type
     *
     * @return a DOT {@link ModelDeserializer} for {@code G}s.
     */
    public static <NP, EP, G extends MutableGraph<?, ?, NP, EP>> ModelDeserializer<G> graph(Supplier<G> creator,
                                                                                            Function<Map<String, String>, NP> nodeParser,
                                                                                            Function<Map<String, String>, EP> edgeParser) {
        return new DOTGraphParser<>(creator, nodeParser, edgeParser);
    }

    /**
     * Default parser for {@link ModalTransitionSystem}s serialized by AutomataLib.
     * <p>
     * Invokes {@link #mts(AutomatonCreator, Function, Function)} with {@link CompactMTS.Creator} as {@code creator},
     * {@link #DEFAULT_EDGE_PARSER} as {@code inputParser} and {@link #DEFAULT_EDGE_PARSER} as {@code propertyParser}.
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactMTS}s.
     */
    public static DOTInputModelDeserializer<Integer, @Nullable String, CompactMTS<@Nullable String>> mts() {
        return mts(new CompactMTS.Creator<@Nullable String>(), DEFAULT_EDGE_PARSER, DEFAULT_MTS_EDGE_PARSER);
    }

    /**
     * Parser for {@link ModalTransitionSystem}s with a custom MTS instance, custom input type and edge attributes
     * parsers.
     * <p>
     * Invokes {@link #mts(AutomatonCreator, Function, Function, Collection)} with AutomataLib's default initial state
     * label "{@code __start0}" as {@code initialNodeLabel}.
     *
     * @param creator
     *         a creator that is used to instantiate the returned graph
     * @param inputParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param propertyParser
     *         an edge parser that extracts from a property map of an edge the modal transition property
     * @param <S>
     *         the state type of the returned MTS
     * @param <I>
     *         the input symbol type
     * @param <TP>
     *         the modal transition property
     * @param <M>
     *         the type of the returned MTS
     *
     * @return a DOT {@link ModelDeserializer} for {@code M}s.
     */
    public static <S, I, TP extends MutableModalEdgeProperty, M extends MutableModalTransitionSystem<S, I, ?, TP>> DOTInputModelDeserializer<S, I, M> mts(
            AutomatonCreator<M, I> creator,
            Function<Map<String, String>, I> inputParser,
            Function<Map<String, String>, TP> propertyParser) {
        return mts(creator, inputParser, propertyParser, Collections.singletonList(GraphDOT.initialLabel(0)));
    }

    /**
     * Parser for {@link ModalTransitionSystem}s with a custom MTS instance, custom input type and edge attributes
     * parsers and custom initial state labels.
     *
     * @param creator
     *         a creator that is used to instantiate the returned graph
     * @param inputParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param propertyParser
     *         an edge parser that extracts from a property map of an edge the modal transition property
     * @param initialNodeIds
     *         the ids of the initial nodes
     * @param <S>
     *         the state type of the returned MTS
     * @param <I>
     *         the input symbol type
     * @param <TP>
     *         the modal transition property
     * @param <M>
     *         the type of the returned MTS
     *
     * @return a DOT {@link ModelDeserializer} for {@code M}s.
     */
    public static <S, I, TP extends MutableModalEdgeProperty, M extends MutableModalTransitionSystem<S, I, ?, TP>> DOTInputModelDeserializer<S, I, M> mts(
            AutomatonCreator<M, I> creator,
            Function<Map<String, String>, I> inputParser,
            Function<Map<String, String>, TP> propertyParser,
            Collection<String> initialNodeIds) {
        return new DOTMutableAutomatonParser<>(creator,
                                               node -> null,
                                               edge -> Pair.of(inputParser.apply(edge), propertyParser.apply(edge)),
                                               initialNodeIds,
                                               true);
    }

    /**
     * Default parser for {@link ModalContract}s serialized by AutomataLib.
     * <p>
     * Invokes {@link #mc(AutomatonCreator, Function, Function)} with {@link CompactMC.Creator} as {@code creator},
     * {@link #DEFAULT_EDGE_PARSER} as {@code inputParser} and {@link #DEFAULT_MC_EDGE_PARSER} as {@code
     * propertyParser}.
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactMC}s.
     */
    public static DOTInputModelDeserializer<Integer, @Nullable String, CompactMC<@Nullable String>> mc() {
        return mc(new CompactMC.Creator<@Nullable String>(), DEFAULT_EDGE_PARSER, DEFAULT_MC_EDGE_PARSER);
    }

    /**
     * Parser for {@link ModalContract}s with a custom MC instance, custom input type and edge attributes parsers.
     * <p>
     * Invokes {@link #mc(AutomatonCreator, Function, Function, Collection)} with AutomataLib's default initial state
     * label "{@code __start0}" as {@code initialNodeLabel}.
     *
     * @param creator
     *         a creator that is used to instantiate the returned graph
     * @param inputParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param propertyParser
     *         an edge parser that extracts from a property map of an edge the modal transition property
     * @param <S>
     *         the state type of the returned MTS
     * @param <I>
     *         the input symbol type
     * @param <TP>
     *         the modal transition property
     * @param <M>
     *         the type of the returned MC
     *
     * @return a DOT {@link ModelDeserializer} for {@code M}s.
     */
    public static <S, I, TP extends MutableModalContractEdgeProperty, M extends MutableModalContract<S, I, ?, TP>> DOTInputModelDeserializer<S, I, M> mc(
            AutomatonCreator<M, I> creator,
            Function<Map<String, String>, I> inputParser,
            Function<Map<String, String>, TP> propertyParser) {
        return mc(creator, inputParser, propertyParser, Collections.singletonList(GraphDOT.initialLabel(0)));
    }

    /**
     * Parser for {@link ModalContract}s with a custom MC instance, custom input type and edge attributes parsers and
     * custom initial state labels.
     *
     * @param creator
     *         a creator that is used to instantiate the returned graph
     * @param inputParser
     *         an edge parser that extracts from a property map of an edge the input symbol
     * @param propertyParser
     *         an edge parser that extracts from a property map of an edge the modal transition property
     * @param initialNodeIds
     *         the ids of the initial nodes
     * @param <S>
     *         the state type of the returned MTS
     * @param <I>
     *         the input symbol type
     * @param <TP>
     *         the modal transition property
     * @param <M>
     *         the type of the returned MC
     *
     * @return a DOT {@link ModelDeserializer} for {@code M}s.
     */
    public static <S, I, TP extends MutableModalContractEdgeProperty, M extends MutableModalContract<S, I, ?, TP>> DOTInputModelDeserializer<S, I, M> mc(
            AutomatonCreator<M, I> creator,
            Function<Map<String, String>, I> inputParser,
            Function<Map<String, String>, TP> propertyParser,
            Collection<String> initialNodeIds) {
        return new DOTMutableAutomatonParser<>(creator,
                                               node -> null,
                                               edge -> Pair.of(inputParser.apply(edge), propertyParser.apply(edge)),
                                               initialNodeIds,
                                               true);
    }

    /**
     * Default parser for {@link CompactMMC}s serialized by AutomataLib.
     * <p>
     * Invokes {@link #mc(AutomatonCreator, Function, Function)} with {@link CompactMMC.Creator} as {@code creator},
     * {@link #DEFAULT_EDGE_PARSER} as {@code inputParser} and {@link #DEFAULT_MMC_EDGE_PARSER} as {@code
     * propertyParser}.
     *
     * @return a {@link DOTInputModelDeserializer} for {@link CompactMMC}s.
     */
    public static DOTInputModelDeserializer<Integer, @Nullable String, CompactMMC<@Nullable String>> mmc() {
        return mc(new CompactMMC.Creator<@Nullable String>(), DEFAULT_EDGE_PARSER, DEFAULT_MMC_EDGE_PARSER);
    }

    private static String getAndRequireNotNull(Map<String, String> map, String attribute) {
        final String value = map.get(attribute);
        if (value == null) {
            throw new IllegalArgumentException("attribute '" + attribute + "' is undefined");
        }
        return value;
    }

}
