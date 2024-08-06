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
package net.automatalib.modelchecking.lasso;

import java.util.Collection;

import net.automatalib.automata.concepts.DetOutputAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.modelchecking.Lasso.DFALasso;

/**
 * A DFALasso is a lasso for {@link DFA}s.
 *
 * @param <I>
 *         the input type
 *
 * @author Jeroen Meijer
 */
public class DFALassoImpl<I> extends AbstractLasso<I, Boolean> implements DFALasso<I> {

    public DFALassoImpl(DetOutputAutomaton<?, I, ?, Boolean> automaton,
                        Collection<? extends I> inputs,
                        int unfoldTimes) {
        super(automaton, inputs, unfoldTimes);
    }

    /**
     * Returns whether the given state is accepting.
     * <p>
     * The current state is only accepting iff it is precisely the state after the last symbol index in the finite
     * representation of the lasso.
     *
     * @param state
     *         to compute whether it is accepting.
     *
     * @return whether the given {@code state} is accepting.
     */
    @Override
    public boolean isAccepting(Integer state) {
        return state == getWord().length();
    }
}
