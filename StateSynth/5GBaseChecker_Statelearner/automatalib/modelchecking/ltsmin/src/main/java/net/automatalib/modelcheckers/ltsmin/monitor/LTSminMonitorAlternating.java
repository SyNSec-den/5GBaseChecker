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
package net.automatalib.modelcheckers.ltsmin.monitor;

import java.util.Collection;
import java.util.function.Function;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.modelcheckers.ltsmin.LTSminAlternating;
import net.automatalib.modelcheckers.ltsmin.LTSminLTLParser;
import net.automatalib.serialization.fsm.parser.FSM2MealyParserAlternating;

/**
 * A monitor model checker using LTSmin for Mealy machines using alternating edge semantics.
 *
 * @param <I>
 *         the input type
 * @param <O>
 *         the output type
 *
 * @author Jeroen Meijer
 */
public class LTSminMonitorAlternating<I, O> extends AbstractLTSminMonitorMealy<I, O>
        implements LTSminAlternating<I, O, MealyMachine<?, I, ?, O>> {

    @GenerateBuilder(defaults = BuilderDefaults.class)
    public LTSminMonitorAlternating(boolean keepFiles,
                                    Function<String, I> string2Input,
                                    Function<String, O> string2Output,
                                    Collection<? super O> skipOutputs) {
        super(keepFiles, string2Input, string2Output, skipOutputs);
    }

    /**
     * @see FSM2MealyParserAlternating
     *
     * @return {@code true}, because there could be undefined outputs in FSMs.
     */
    @Override
    public boolean requiresOriginalAutomaton() {
        return true;
    }

    @Override
    protected void verifyFormula(String formula) {
        LTSminLTLParser.requireValidLetterFormula(formula);
    }
}
