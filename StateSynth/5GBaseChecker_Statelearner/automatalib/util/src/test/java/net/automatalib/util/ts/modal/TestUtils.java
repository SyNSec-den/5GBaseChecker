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
package net.automatalib.util.ts.modal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.MTSTransition;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalContractMembershipEdgePropertyImpl;
import net.automatalib.ts.modal.transition.MutableModalContractEdgeProperty;
import net.automatalib.words.Alphabet;
import org.assertj.core.api.Assertions;

public final class TestUtils {

    private static final InputModelDeserializer<String, CompactMTS<String>> MTS_PARSER = DOTParsers.mts();
    private static final InputModelDeserializer<String, CompactMC<String>> MC_PARSER = DOTParsers.mc();
    private static final InputModelDeserializer<String, CompactMMC<String>> MMC_PARSER = DOTParsers.mmc();

    private TestUtils() {}

    public static CompactMTS<String> loadMTSFromPath(String path) throws IOException {
        try (InputStream is = TestUtils.class.getResourceAsStream(path)) {
            return MTS_PARSER.readModel(is).model;
        }
    }

    public static CompactMC<String> loadMCFromPath(String path) throws IOException {
        try (InputStream is = TestUtils.class.getResourceAsStream(path)) {
            final CompactMC<String> parsed = MC_PARSER.readModel(is).model;

            for (Integer s : parsed.getStates()) {
                for (String label : parsed.getInputAlphabet()) {
                    for (MTSTransition<MutableModalContractEdgeProperty> transition : parsed.getTransitions(s, label)) {

                        if (transition.getProperty().isRed() || transition.getProperty().isGreen()) {
                            parsed.getCommunicationAlphabet().add(label);
                        }

                    }
                }
            }

            return parsed;
        }
    }

    public static CompactMMC<String> loadMMCFromPath(String path) throws IOException {
        try (InputStream is = TestUtils.class.getResourceAsStream(path)) {
            final CompactMMC<String> parsed = MMC_PARSER.readModel(is).model;

            for (Integer s : parsed.getStates()) {
                for (String label : parsed.getInputAlphabet()) {
                    for (MTSTransition<ModalContractMembershipEdgePropertyImpl> transition : parsed.getTransitions(s,
                                                                                                                   label)) {

                        if (transition.getProperty().isRed() || transition.getProperty().isGreen()) {
                            parsed.addCommunicationSymbol(label);
                        }

                    }
                }
            }

            return parsed;
        }
    }

    public static CompactMMC<String> loadMMCFromPath(String path,
                                                     Alphabet<String> inputAlphabet,
                                                     Alphabet<String> communicationAlphabet) throws IOException {
        try (InputStream is = TestUtils.class.getResourceAsStream(path)) {
            final CompactMMC.Creator<String> creator = new CompactMMC.Creator<>(inputAlphabet, communicationAlphabet);

            return DOTParsers.mc(creator, DOTParsers.DEFAULT_EDGE_PARSER, DOTParsers.DEFAULT_MMC_EDGE_PARSER)
                             .readModel(is).model;
        }
    }

    public static <S1, S2, I> void assertIsRefinementEquivalentTo(ModalTransitionSystem<S1, I, ?, ?> actual,
                                                                  ModalTransitionSystem<S2, I, ?, ?> expected,
                                                                  Collection<I> alphabetSubset) {
        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(expected).isNotNull();
        Assertions.assertThat(alphabetSubset).isNotNull();

        Assertions.assertThat(alphabetSubset)
                  .describedAs("Refinement alphabet needs to be a subset of the input´s alphabets")
                  .isSubsetOf(actual.getInputAlphabet())
                  .isSubsetOf(expected.getInputAlphabet());

        Set<Pair<S1, S2>> refinement = getRefinementRelation(actual, expected, alphabetSubset);

        final Set<S1> initialStatesActual = new HashSet<>(actual.getInitialStates());
        final Set<S2> initialStatesExpected = new HashSet<>(expected.getInitialStates());

        for (Pair<S1, S2> p : refinement) {
            initialStatesActual.remove(p.getFirst());
            initialStatesExpected.remove(p.getSecond());
        }

        if (!initialStatesActual.isEmpty() || !initialStatesExpected.isEmpty()) {
            Assertions.fail("Expected mts to be refinement of: Expected pairs %s to match %s.",
                            initialStatesActual,
                            initialStatesExpected);
        }
    }

    private static <S1, S2, I> Set<Pair<S1, S2>> getRefinementRelation(ModalTransitionSystem<S1, I, ?, ?> actual,
                                                                       ModalTransitionSystem<S2, I, ?, ?> expected,
                                                                       Collection<I> inputs) {

        final Set<Pair<S1, S2>> refinement1 = ModalRefinement.refinementRelation(actual, expected, inputs);
        final Set<Pair<S2, S1>> refinement2 = ModalRefinement.refinementRelation(expected, actual, inputs);

        final Set<Pair<S1, S2>> refinement2Rev =
                refinement2.stream().map(p -> Pair.of(p.getSecond(), p.getFirst())).collect(Collectors.toSet());

        return Sets.intersection(refinement1, refinement2Rev);
    }
}
