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

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;

/**
 * A mutable version of {@link ModalTransitionSystem} that allows to add states and transitions.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <TP>
 *         (specific) transition property type
 *
 * @author msc
 * @author frohme
 */
public interface MutableModalTransitionSystem<S, I, T, TP extends MutableModalEdgeProperty>
        extends ModalTransitionSystem<S, I, T, TP>, MutableAutomaton<S, I, T, Void, TP> {

    T addModalTransition(S src, I input, S tgt, ModalType modalType);

    /**
     * Create a new transition with a default (non-null) {@link MutableModalEdgeProperty}.
     *
     * @param successor
     *         the successor of the transition
     *
     * @return a new transition with a default (non-null) {@link MutableModalEdgeProperty}
     */
    T createTransition(S successor);
}
