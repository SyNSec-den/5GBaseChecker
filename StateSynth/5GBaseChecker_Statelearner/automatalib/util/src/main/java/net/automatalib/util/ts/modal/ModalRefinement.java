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

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiPredicate;

import com.google.common.collect.Sets;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;

public final class ModalRefinement {

    private ModalRefinement() {
        // do not instantiate
    }

    private static <BS, I, BT, BTP extends ModalEdgeProperty> Set<BT> partnerTransitions(ModalTransitionSystem<BS, I, BT, BTP> b,
                                                                                         BS source,
                                                                                         I input,
                                                                                         Set<ModalType> acceptableValues) {

        Set<BT> coTransitions = Sets.newHashSetWithExpectedSize(b.getInputAlphabet().size());

        for (BT candidateTransition : b.getTransitions(source, input)) {
            BTP property = b.getTransitionProperty(candidateTransition);

            if (property != null && acceptableValues.contains(property.getModalType())) {
                coTransitions.add(candidateTransition);
            }
        }

        return coTransitions;
    }

    private static <AS, I, AT, BS, BT> boolean eligiblePartner(ModalTransitionSystem<AS, I, AT, ?> a,
                                                               ModalTransitionSystem<BS, I, BT, ?> b,
                                                               Collection<I> inputs,
                                                               BiPredicate<AS, BS> inRefinementRelation,
                                                               AS source,
                                                               BS coSource,
                                                               Set<ModalType> acceptableValues) {

        for (I label : inputs) {
            for (AT transition : a.getTransitions(source, label)) {
                if (!acceptableValues.contains(a.getTransitionProperty(transition).getModalType())) {
                    continue;
                }

                Set<BT> partnerTransitions = partnerTransitions(b, coSource, label, acceptableValues);

                AS target = a.getSuccessor(transition);
                final boolean eligiblePartner = partnerTransitions.stream()
                                                                  .map(b::getSuccessor)
                                                                  .anyMatch(s -> inRefinementRelation.test(target, s));

                if (!eligiblePartner) {
                    return false;
                }
            }
        }

        return true;
    }

    public static <AS, BS, I> Set<Pair<AS, BS>> refinementRelation(ModalTransitionSystem<AS, I, ?, ?> implementation,
                                                                   ModalTransitionSystem<BS, I, ?, ?> specification,
                                                                   Collection<I> inputs) {

        Set<Pair<AS, BS>> refinement = Sets.newHashSetWithExpectedSize(implementation.size() * specification.size());

        // lower approximation only correct if automaton is finite (image-finite)
        for (AS p : implementation.getStates()) {
            for (BS q : specification.getStates()) {
                refinement.add(Pair.of(p, q));
            }
        }

        Set<ModalType> may = EnumSet.of(ModalType.MAY, ModalType.MUST);
        Set<ModalType> must = Collections.singleton(ModalType.MUST);

        boolean update = true;
        while (update) {
            update = false;

            Iterator<Pair<AS, BS>> iterator = refinement.iterator();
            while (iterator.hasNext()) {
                Pair<AS, BS> pair = iterator.next();

                boolean eligiblePartner = eligiblePartner(implementation,
                                                          specification,
                                                          inputs,
                                                          (s, t) -> refinement.contains(Pair.of(s, t)),
                                                          pair.getFirst(),
                                                          pair.getSecond(),
                                                          may);

                eligiblePartner &= eligiblePartner(specification,
                                                   implementation,
                                                   inputs,
                                                   (s, t) -> refinement.contains(Pair.of(t, s)),
                                                   pair.getSecond(),
                                                   pair.getFirst(),
                                                   must);

                if (!eligiblePartner) {
                    update = true;
                    iterator.remove();
                }
            }
        }

        return refinement;
    }
}
