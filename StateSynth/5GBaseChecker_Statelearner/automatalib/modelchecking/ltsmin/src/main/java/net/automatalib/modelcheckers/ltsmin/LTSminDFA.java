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
package net.automatalib.modelcheckers.ltsmin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.serialization.etf.writer.DFA2ETFWriter;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * A model checker using LTSmin for DFAs.
 *
 * An important feature of this {@link DFAModelChecker}, is that it will check if a given DFA hypothesis is
 * prefix-closed.
 * <p>
 * Another important feature is that rejecting states are NOT part of the LTS. This avoids the need for an unconditional
 * fairness constraint in LTL formulae.
 *
 * @param <I> the input type.
 * @param <R> the type of a counterexample
 *
 * @see DFAs#isPrefixClosed(DFA, Alphabet)
 *
 * @author Jeroen Meijer
 */
public interface LTSminDFA<I, R> extends LTSmin<I, DFA<?, I>, R>, ModelChecker.DFAModelChecker<I, String, R> {

    /**
     * The index in the FSM state vector for accept/reject.
     */
    String LABEL_NAME = "label";

    /**
     * The value in the state vector for acceptance.
     */
    String LABEL_VALUE = "accept";

    @Override
    default void automaton2ETF(DFA<?, I> automaton, Collection<? extends I> inputs, File etf) throws IOException {
        dfa2ETF(automaton, inputs, etf);
    }

    /**
     * Writes the given {@code dfa} to {@code etf}, while skipping rejecting states.
     *
     * @param dfa
     *         the DFA to write.
     * @param inputs
     *         the alphabet.
     * @param etf
     *         the file to write to.
     * @param <S>
     *         the state type
     *
     * @throws IOException if the dfa couldn't be written to the provided file.
     * @throws ModelCheckingException if the dfa cannot be transformed into a valid LTS.
     */
    default <S> void dfa2ETF(DFA<S, I> dfa, Collection<? extends I> inputs, File etf) throws IOException {
        // check that the DFA rejects the empty language
        if (DFAs.acceptsEmptyLanguage(dfa)) {
            throw new ModelCheckingException("DFA accepts the empty language, the LTS for such a DFA is not defined.");
        }

        final Alphabet<I> alphabet = Alphabets.fromCollection(inputs);

        // check the DFA is prefix-closed
        if (!DFAs.isPrefixClosed(dfa, alphabet)) {
            throw new ModelCheckingException("DFA is not prefix closed.");
        }

        // remove all rejecting states
        final MutableDFA<?, I> copy = new CompactDFA<>(alphabet, dfa.size());
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE,
                                   dfa,
                                   inputs,
                                   copy,
                                   dfa::isAccepting,
                                   (s, i, t) -> true);
        DFA2ETFWriter.<I>getInstance().writeModel(etf, copy, alphabet);
    }
}
