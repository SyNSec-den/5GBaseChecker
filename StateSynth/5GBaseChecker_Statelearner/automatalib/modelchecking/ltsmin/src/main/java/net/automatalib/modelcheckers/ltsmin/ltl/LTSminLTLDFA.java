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
package net.automatalib.modelcheckers.ltsmin.ltl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelcheckers.ltsmin.LTSminDFA;
import net.automatalib.modelcheckers.ltsmin.LTSminLTLParser;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.ModelCheckerLasso.DFAModelCheckerLasso;
import net.automatalib.modelchecking.lasso.DFALassoImpl;
import net.automatalib.serialization.fsm.parser.FSM2DFAParser;
import net.automatalib.serialization.fsm.parser.FSMFormatException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An LTL model checker using LTSmin for DFAs.
 *
 * @param <I>
 *         the input type
 *
 * @author Jeroen Meijer
 */
public class LTSminLTLDFA<I> extends AbstractLTSminLTL<I, DFA<?, I>, DFALasso<I>>
        implements DFAModelCheckerLasso<I, String>, LTSminDFA<I, DFALasso<I>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LTSminLTLDFA.class);

    /**
     * The index in the FSM state vector for accept/reject.
     */
    public static final String LABEL_NAME = "label";

    /**
     * The value in the state vector for acceptance.
     */
    public static final String LABEL_VALUE = "accept";

    @GenerateBuilder(defaults = AbstractLTSminLTL.BuilderDefaults.class)
    public LTSminLTLDFA(boolean keepFiles, Function<String, I> string2Input, int minimumUnfolds, double multiplier) {
        super(keepFiles, string2Input, minimumUnfolds, multiplier);
    }

    @Override
    protected void verifyFormula(String formula) {
        LTSminLTLParser.requireValidLetterFormula(formula);
    }

    /**
     * Converts the FSM file to a {@link DFALasso}.
     *
     * @param automaton
     *         the DFA used to compute the number of loop unrolls.
     *
     * @see AbstractLTSminLTL#findCounterExample(Object, Collection, Object)
     */
    @Override
    public @Nullable DFALasso<I> findCounterExample(DFA<?, I> automaton, Collection<? extends I> inputs, String property) {
        final File fsm = findCounterExampleFSM(automaton, inputs, property);

        if (fsm == null) {
            return null;
        }

        try {
            final CompactDFA<I> dfa =
                    FSM2DFAParser.getParser(inputs, getString2Input(), LABEL_NAME, LABEL_VALUE).readModel(fsm);

            return new DFALassoImpl<>(dfa, inputs, computeUnfolds(automaton.size()));
        } catch (IOException | FSMFormatException e) {
            throw new ModelCheckingException(e);
        } finally {
            // check if we must keep the FSM
            if (!isKeepFiles() && !fsm.delete()) {
                LOGGER.warn("Could not delete file: " + fsm.getAbsolutePath());
            }
        }
    }
}
