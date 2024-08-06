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
package net.automatalib.automata.visualization;

import java.util.Map;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.ts.modal.ModalContract;
import net.automatalib.ts.modal.transition.GroupMemberEdge;
import net.automatalib.ts.modal.transition.ModalContractEdgeProperty;

public class MMCVisualizationHelper<S, I, T, TP extends ModalContractEdgeProperty & GroupMemberEdge, M extends ModalContract<S, I, T, TP>>
        extends MCVisualizationHelper<S, I, T, TP, M> {

    public MMCVisualizationHelper(M mc) {
        super(mc);
    }

    @Override
    public boolean getEdgeProperties(S src, TransitionEdge<I, T> edge, S tgt, Map<String, String> properties) {
        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
            return false;
        }

        final TP transitionProperty = super.automaton.getTransitionProperty(edge.getTransition());
        final int memberId = transitionProperty.getMemberId();

        properties.put(MMCEdgeAttrs.MEMBERSHIP, String.valueOf(memberId));

        return true;
    }
}
