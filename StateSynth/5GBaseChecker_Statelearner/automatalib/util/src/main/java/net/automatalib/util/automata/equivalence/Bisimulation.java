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
package net.automatalib.util.automata.equivalence;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;
import net.automatalib.automata.Automaton;
import net.automatalib.commons.util.Pair;

/**
 * @author msc
 */
public final class Bisimulation {

    private Bisimulation() {
        // prevent instantiation
    }

    public static <AS, I, AT, A extends Automaton<AS, I, AT>, BS, BT, B extends Automaton<BS, I, BT>> Set<Pair<AS, BS>> bisimulationEquivalenceRelation(
            A a,
            B b,
            Collection<I> inputs) {

        Set<Pair<AS, BS>> bisim = Sets.newHashSetWithExpectedSize(a.size() * b.size());
        Set<Pair<AS, BS>> change = Sets.newHashSetWithExpectedSize(a.size() * b.size());

        boolean empty;

        for (AS p : a.getStates()) {
            for (BS q : b.getStates()) {

                empty = true;
                for (I sym : inputs) {
                    empty &= a.getTransitions(p, sym).isEmpty() && b.getTransitions(q, sym).isEmpty();
                    if (!a.getTransitions(p, sym).isEmpty() && !b.getTransitions(q, sym).isEmpty()) {
                        change.add(Pair.of(p, q));
                        break;
                    }
                }

                if (empty) {
                    change.add(Pair.of(p, q));
                }
            }
        }

        Set<Pair<AS, BS>> swap;
        boolean forall, exists;
        while (!bisim.equals(change)) {
            swap = change;
            change = bisim;
            bisim = swap;
            change.clear();

            for (Pair<AS, BS> p : bisim) {

                forall = true;
                for (I sym : inputs) {
                    for (AT t : a.getTransitions(p.getFirst(), sym)) {

                        exists = false;
                        for (BT f : b.getTransitions(p.getSecond(), sym)) {
                            for (Pair<AS, BS> s : bisim) {
                                if (Objects.equals(a.getSuccessor(t), s.getFirst()) &&
                                    Objects.equals(b.getSuccessor(f), s.getSecond())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (exists) {
                                break;
                            }
                        }
                        if (!exists) {
                            forall = false;
                            break;
                        }
                    }
                }

                if (!forall) {
                    continue;
                }

                forall = true;
                for (I sym : inputs) {
                    for (BT f : b.getTransitions(p.getSecond(), sym)) {

                        exists = false;
                        for (AT t : a.getTransitions(p.getFirst(), sym)) {
                            for (Pair<AS, BS> s : bisim) {
                                if (Objects.equals(b.getSuccessor(f), s.getSecond()) &&
                                    Objects.equals(a.getSuccessor(t), s.getFirst())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (exists) {
                                break;
                            }
                        }
                        if (!exists) {
                            forall = false;
                            break;
                        }
                    }
                }

                if (forall) {
                    change.add(p);
                }
            }
        }

        return change;
    }
}
