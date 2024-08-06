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
package net.automatalib.ts.modal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.ts.modal.transition.ModalContractEdgeProperty.EdgeColor;
import net.automatalib.ts.modal.transition.ModalContractEdgePropertyImpl;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableModalContractEdgeProperty;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;

public class CompactMC<I> extends AbstractCompactMTS<I, MutableModalContractEdgeProperty>
        implements MutableModalContract<Integer, I, MTSTransition<MutableModalContractEdgeProperty>, MutableModalContractEdgeProperty> {

    protected final GrowingAlphabet<I> communicationAlphabet;
    protected final Set<MTSTransition<MutableModalContractEdgeProperty>> redTransitions;

    public CompactMC(Alphabet<I> alphabet, Collection<I> gamma) {
        super(alphabet);
        this.communicationAlphabet = new GrowingMapAlphabet<>(gamma);
        this.redTransitions = new HashSet<>();

        assert new HashSet<>(alphabet).containsAll(gamma) : "Communication alphabet needs to be a subset of alphabet";
    }

    @Override
    protected MutableModalContractEdgeProperty getDefaultTransitionProperty() {
        return buildModalProperty(ModalType.MUST);
    }

    @Override
    protected MutableModalContractEdgeProperty buildModalProperty(ModalType type) {
        return buildContractProperty(type, false, EdgeColor.NONE);
    }

    public MutableModalContractEdgeProperty buildContractProperty(ModalType type, boolean tau, EdgeColor color) {
        return new ModalContractEdgePropertyImpl(type, tau, color);
    }

    @Override
    public GrowingAlphabet<I> getCommunicationAlphabet() {
        return communicationAlphabet;
    }

    @Override
    public MTSTransition<MutableModalContractEdgeProperty> addContractTransition(Integer src,
                                                                                 I input,
                                                                                 Integer tgt,
                                                                                 ModalType modalType,
                                                                                 boolean tau,
                                                                                 EdgeColor color) {
        return super.addTransition(src, input, tgt, buildContractProperty(modalType, tau, color));
    }

    public static final class Creator<I> implements AutomatonCreator<CompactMC<I>, I> {

        @Override
        public CompactMC<I> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return createAutomaton(alphabet);
        }

        @Override
        public CompactMC<I> createAutomaton(Alphabet<I> alphabet) {
            return new CompactMC<>(alphabet, Collections.emptyList());
        }
    }

}
