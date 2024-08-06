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
import java.util.function.Predicate;

import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.commons.smartcollections.ArrayStorage;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class revolving around 1-SEVPAs.
 *
 * @author Malte Isberner
 */
public final class OneSEVPAUtil {

    private OneSEVPAUtil() {}

    public static <L, I> List<L> findReachableLocations(final OneSEVPA<L, I> sevpa, final VPDAlphabet<I> alphabet) {
        return computeAccessSequences(sevpa, alphabet, false, l -> false).reachableLocs;
    }

    public static <L, I> ReachResult<L, I> computeAccessSequences(final OneSEVPA<L, I> sevpa,
                                                                  final VPDAlphabet<I> alphabet,
                                                                  final boolean computeAs,
                                                                  final Predicate<? super L> terminatePred) {
        final ArrayStorage<Word<I>> result = new ArrayStorage<>(sevpa.size());

        final L initLoc = sevpa.getInitialLocation();
        final List<L> reachable = new ArrayList<>();
        reachable.add(initLoc);
        result.set(sevpa.getLocationId(initLoc), Word.epsilon());

        if (terminatePred.test(initLoc)) {
            return new ReachResult<>(initLoc, reachable, result);
        }

        int queuePtr = 0;
        while (queuePtr < reachable.size()) {
            final L curr = reachable.get(queuePtr++);
            final Word<I> currAs = result.get(sevpa.getLocationId(curr));

            for (I intSym : alphabet.getInternalAlphabet()) {
                final L succ = sevpa.getInternalSuccessor(curr, intSym);
                if (succ == null) {
                    continue;
                }
                final int succIdx = sevpa.getLocationId(succ);
                if (result.get(succIdx) != null) {
                    continue;
                }
                final Word<I> succAs = computeAs ? currAs.append(intSym) : Word.epsilon();
                result.set(succIdx, succAs);
                if (terminatePred.test(succ)) {
                    return new ReachResult<>(succ, reachable, result);
                }
                reachable.add(succ);
            }

            for (I callSym : alphabet.getCallAlphabet()) {
                for (I returnSym : alphabet.getReturnAlphabet()) {
                    for (int i = 0; i < queuePtr; i++) {
                        final L src = reachable.get(i);
                        int stackSym = sevpa.encodeStackSym(src, callSym);
                        L succ = sevpa.getReturnSuccessor(curr, returnSym, stackSym);
                        if (succ == null) {
                            continue;
                        }
                        int succIdx = sevpa.getLocationId(succ);
                        if (result.get(succIdx) == null) {
                            Word<I> succAs = computeAs ?
                                    result.get(sevpa.getLocationId(src))
                                          .append(callSym)
                                          .concat(currAs.append(returnSym)) :
                                    Word.epsilon();
                            result.set(succIdx, succAs);
                            if (terminatePred.test(succ)) {
                                return new ReachResult<>(succ, reachable, result);
                            }
                            reachable.add(succ);
                        }

                        if (src != curr) {
                            stackSym = sevpa.encodeStackSym(curr, callSym);
                            succ = sevpa.getReturnSuccessor(src, returnSym, stackSym);
                            if (succ == null) {
                                continue;
                            }
                            succIdx = sevpa.getLocationId(succ);
                            if (result.get(succIdx) == null) {
                                final Word<I> succAs = computeAs ?
                                        currAs.append(callSym)
                                              .concat(result.get(sevpa.getLocationId(src)).append(returnSym)) :
                                        Word.epsilon();
                                result.set(succIdx, succAs);
                                if (terminatePred.test(succ)) {
                                    return new ReachResult<>(succ, reachable, result);
                                }
                                reachable.add(succ);
                            }
                        }
                    }
                }
            }
        }

        return new ReachResult<>(null, reachable, result);
    }

    public static <L, I> ArrayStorage<Word<I>> computeAccessSequences(final OneSEVPA<L, I> sevpa,
                                                                      final VPDAlphabet<I> alphabet) {
        return computeAccessSequences(sevpa, alphabet, true, l -> false).accessSequences;
    }

    public static <L, I> @Nullable Word<I> findRejectedWord(final OneSEVPA<L, I> sevpa, final VPDAlphabet<I> alphabet) {
        return computeAccessSequence(sevpa, alphabet, l -> !sevpa.isAcceptingLocation(l));
    }

    public static <L, I> @Nullable Word<I> computeAccessSequence(final OneSEVPA<L, I> sevpa,
                                                                 final VPDAlphabet<I> alphabet,
                                                                 final Predicate<? super L> predicate) {
        final ReachResult<L, I> result = computeAccessSequences(sevpa, alphabet, true, predicate);
        L resultLoc = result.terminateLoc;
        if (resultLoc != null) {
            return result.accessSequences.get(sevpa.getLocationId(resultLoc));
        }
        return null;
    }

    public static <I> boolean testEquivalence(final OneSEVPA<?, I> sevpa1,
                                              final OneSEVPA<?, I> sevpa2,
                                              final VPDAlphabet<I> alphabet) {
        return findSeparatingWord(sevpa1, sevpa2, alphabet) == null;
    }

    public static <I> @Nullable Word<I> findSeparatingWord(final OneSEVPA<?, I> sevpa1,
                                                           final OneSEVPA<?, I> sevpa2,
                                                           final VPDAlphabet<I> alphabet) {
        final OneSEVPA<?, I> prod = OneSEVPAs.xor(sevpa1, sevpa2, alphabet);
        return findAcceptedWord(prod, alphabet);
    }

    public static <L, I> @Nullable Word<I> findAcceptedWord(final OneSEVPA<L, I> sevpa, final VPDAlphabet<I> alphabet) {
        return computeAccessSequence(sevpa, alphabet, sevpa::isAcceptingLocation);
    }

    public static class ReachResult<L, I> {

        public final @Nullable L terminateLoc;
        public final List<L> reachableLocs;
        public final ArrayStorage<Word<I>> accessSequences;

        public ReachResult(final @Nullable L terminateLoc,
                           final List<L> reachableLocs,
                           final ArrayStorage<Word<I>> accessSequences) {
            this.terminateLoc = terminateLoc;
            this.reachableLocs = reachableLocs;
            this.accessSequences = accessSequences;
        }
    }

}
