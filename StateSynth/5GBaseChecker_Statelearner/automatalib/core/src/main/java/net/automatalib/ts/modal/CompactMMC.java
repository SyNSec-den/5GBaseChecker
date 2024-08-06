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
import java.util.LinkedHashSet;
import java.util.Set;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.ts.modal.transition.ModalContractEdgeProperty.EdgeColor;
import net.automatalib.ts.modal.transition.ModalContractMembershipEdgePropertyImpl;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactMMC<I> extends AbstractCompactMTS<I, ModalContractMembershipEdgePropertyImpl>
        implements MutableMembershipModalContract<Integer, I, MTSTransition<ModalContractMembershipEdgePropertyImpl>, ModalContractMembershipEdgePropertyImpl> {

    protected Set<I> communicationAlphabet;

    public CompactMMC(Alphabet<I> alphabet, Collection<I> gamma) {
        super(alphabet);

        if (!alphabet.containsAll(gamma)) {
            throw new IllegalArgumentException("Communication alphabet needs to be a subset of alphabet");
        }

        this.communicationAlphabet = new LinkedHashSet<>(gamma);
    }

    public CompactMMC(Alphabet<I> alphabet) {
        super(alphabet);
        this.communicationAlphabet = new LinkedHashSet<>();
    }

    @Override
    public boolean addCommunicationSymbol(I symbol) {
        if (!getInputAlphabet().containsSymbol(symbol)) {
            throw new IllegalArgumentException("Communication alphabet needs to be a subset of alphabet");
        }

        return communicationAlphabet.add(symbol);
    }

    @Override
    public void removeCommunicationSymbol(I symbol) {
        communicationAlphabet.remove(symbol);
    }

    @Override
    public void setCommunicationAlphabet(Collection<I> alphabet) {
        if (!getInputAlphabet().containsAll(alphabet)) {
            throw new IllegalArgumentException("Communication alphabet needs to be a subset of alphabet");
        }

        this.communicationAlphabet.clear();
        this.communicationAlphabet.addAll(alphabet);
    }

    @Override
    public void clearCommunicationAlphabet() {
        communicationAlphabet.clear();
    }

    @Override
    protected ModalContractMembershipEdgePropertyImpl getDefaultTransitionProperty() {
        return buildModalProperty(ModalType.MUST);
    }

    @Override
    protected ModalContractMembershipEdgePropertyImpl buildModalProperty(ModalType type) {
        return buildContractProperty(type, false, EdgeColor.NONE, -1);
    }

    public ModalContractMembershipEdgePropertyImpl buildContractProperty(ModalType type,
                                                                         boolean tau,
                                                                         EdgeColor color,
                                                                         int id) {
        return new ModalContractMembershipEdgePropertyImpl(type, tau, color, id);
    }

    @Override
    public Alphabet<I> getCommunicationAlphabet() {
        return Alphabets.fromCollection(communicationAlphabet);
    }

    @Override
    public MTSTransition<ModalContractMembershipEdgePropertyImpl> addContractTransition(Integer src,
                                                                                        I input,
                                                                                        Integer tgt,
                                                                                        ModalType modalType,
                                                                                        boolean tau,
                                                                                        EdgeColor color) {
        return addContractTransition(src, input, tgt, modalType, tau, color, -1);
    }

    public MTSTransition<ModalContractMembershipEdgePropertyImpl> addContractTransition(Integer src,
                                                                                        I input,
                                                                                        Integer tgt,
                                                                                        ModalType modalType,
                                                                                        boolean tau,
                                                                                        EdgeColor color,
                                                                                        int memberId) {
        return super.addTransition(src, input, tgt, buildContractProperty(modalType, tau, color, memberId));
    }

    public static final class Creator<I> implements AutomatonCreator<CompactMMC<I>, I> {

        private final @Nullable Alphabet<I> defaultInputAlphabet;
        private final @Nullable Alphabet<I> defaultCommunicationAlphabet;

        public Creator() {
            this(null, null);
        }

        public Creator(@Nullable Alphabet<I> defaultCommunicationAlphabet) {
            this(null, defaultCommunicationAlphabet);
        }

        public Creator(@Nullable Alphabet<I> defaultInputAlphabet, @Nullable Alphabet<I> defaultCommunicationAlphabet) {
            this.defaultInputAlphabet = defaultInputAlphabet;
            this.defaultCommunicationAlphabet = defaultCommunicationAlphabet;
        }

        @Override
        public CompactMMC<I> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return createAutomaton(alphabet);
        }

        @Override
        public CompactMMC<I> createAutomaton(Alphabet<I> alphabet) {
            return new CompactMMC<>(defaultInputAlphabet != null ? defaultInputAlphabet : alphabet,
                                    defaultCommunicationAlphabet != null ?
                                            defaultCommunicationAlphabet :
                                            Collections.emptyList());
        }
    }

}
