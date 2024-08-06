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
package net.automatalib.util.automata.vpda;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.vpda.AbstractSEVPA;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.commons.util.Pair;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.VPDAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Production automaton that allows to join two given 1-SEVPAs. Acceptance semantics of the product automaton depends on
 * the given {@link AcceptanceCombiner}.
 *
 * @param <L1>
 *         location type of first 1-SEVPA
 * @param <L2>
 *         location type of second 1-SEVPA
 * @param <I>
 *         input alphabet type
 *
 * @author Malte Isberner
 */
public class ProductOneSEVPA<L1, L2, I> extends AbstractSEVPA<Pair<L1, L2>, I> implements OneSEVPA<Pair<L1, L2>, I> {

    private final OneSEVPA<L1, I> sevpa1;
    private final OneSEVPA<L2, I> sevpa2;

    private final AcceptanceCombiner accCombiner;

    public ProductOneSEVPA(final VPDAlphabet<I> alphabet,
                           final OneSEVPA<L1, I> sevpa1,
                           final OneSEVPA<L2, I> sevpa2,
                           final AcceptanceCombiner combiner) {
        super(alphabet);
        this.sevpa1 = sevpa1;
        this.sevpa2 = sevpa2;
        this.accCombiner = combiner;
    }

    @Override
    public @Nullable Pair<L1, L2> getInternalSuccessor(final Pair<L1, L2> loc, final I intSym) {
        final L1 succ1 = sevpa1.getInternalSuccessor(loc.getFirst(), intSym);
        if (succ1 == null) {
            return null;
        }
        final L2 succ2 = sevpa2.getInternalSuccessor(loc.getSecond(), intSym);
        if (succ2 == null) {
            return null;
        }
        return Pair.of(succ1, succ2);
    }

    @Override
    public Pair<L1, L2> getLocation(final int id) {
        final int l1Id = id / sevpa2.size();
        final int l2Id = id % sevpa2.size();
        return Pair.of(sevpa1.getLocation(l1Id), sevpa2.getLocation(l2Id));
    }

    @Override
    public int getLocationId(final Pair<L1, L2> loc) {
        return sevpa1.getLocationId(loc.getFirst()) * sevpa2.size() + sevpa2.getLocationId(loc.getSecond());
    }

    @Override
    public List<Pair<L1, L2>> getLocations() {
        final List<Pair<L1, L2>> locations = new ArrayList<>(sevpa1.size() * sevpa2.size());

        for (L1 l1 : sevpa1.getLocations()) {
            for (L2 l2 : sevpa2.getLocations()) {
                locations.add(Pair.of(l1, l2));
            }
        }

        return locations;
    }

    @Override
    public @Nullable Pair<L1, L2> getReturnSuccessor(final Pair<L1, L2> loc, final I retSym, final int stackSym) {
        final int stackSym1 = stackSym / sevpa2.getNumStackSymbols();
        final L1 succ1 = sevpa1.getReturnSuccessor(loc.getFirst(), retSym, stackSym1);
        if (succ1 == null) {
            return null;
        }
        final int stackSym2 = stackSym % sevpa2.getNumStackSymbols();
        final L2 succ2 = sevpa2.getReturnSuccessor(loc.getSecond(), retSym, stackSym2);
        if (succ2 == null) {
            return null;
        }
        return Pair.of(succ1, succ2);
    }

    @Override
    public boolean isAcceptingLocation(final Pair<L1, L2> loc) {
        return accCombiner.combine(sevpa1.isAcceptingLocation(loc.getFirst()),
                                   sevpa2.isAcceptingLocation(loc.getSecond()));
    }

    @Override
    public Pair<L1, L2> getInitialLocation() {
        return Pair.of(sevpa1.getInitialLocation(), sevpa2.getInitialLocation());
    }

    @Override
    public int encodeStackSym(final Pair<L1, L2> srcLoc, final I callSym) {
        final int stackSym1 = sevpa1.encodeStackSym(srcLoc.getFirst(), callSym);
        final int stackSym2 = sevpa2.encodeStackSym(srcLoc.getSecond(), callSym);
        return stackSym1 * sevpa2.getNumStackSymbols() + stackSym2;
    }

    @Override
    public int getNumStackSymbols() {
        return sevpa1.getNumStackSymbols() * sevpa2.getNumStackSymbols();
    }

    @Override
    public int size() {
        return sevpa1.size() * sevpa2.size();
    }

}
