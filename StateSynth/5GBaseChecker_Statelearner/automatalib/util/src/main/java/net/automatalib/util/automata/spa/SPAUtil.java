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
package net.automatalib.util.automata.spa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.spa.SPA;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.util.graphs.Graphs;
import net.automatalib.util.graphs.apsp.APSPResult;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility methods for {@link SPA}s.
 *
 * @author frohme
 */
public final class SPAUtil {

    private SPAUtil() {
        // prevent instantiation
    }

    /**
     * Computes a set of access, terminating and return sequences for a given {@link SPA}.
     *
     * @param spa
     *         the {@link SPA} for which the sequences should be computed
     * @param <I>
     *         input symbol type
     *
     * @return a {@link ATRSequences} object which contains the respective sequences.
     */
    public static <I> ATRSequences<I> computeATRSequences(SPA<?, I> spa) {
        return computeATRSequences(spa, spa.getInputAlphabet());
    }

    /**
     * Computes a set of access, terminating and return sequences for a given {@link SPA} limited to the symbols of the
     * given {@link SPAAlphabet}.
     *
     * @param spa
     *         the {@link SPA} for which the sequences should be computed
     * @param alphabet
     *         the {@link SPAAlphabet} whose symbols should be used for computing the respective sequences
     * @param <I>
     *         input symbol type
     *
     * @return a {@link ATRSequences} object which contains the respective sequences.
     */
    public static <I> ATRSequences<I> computeATRSequences(SPA<?, I> spa, SPAAlphabet<I> alphabet) {

        final Map<I, Word<I>> terminatingSequences = computeTerminatingSequences(spa, alphabet);
        final Pair<Map<I, Word<I>>, Map<I, Word<I>>> accessAndReturnSequences =
                computeAccessAndReturnSequences(spa, alphabet, terminatingSequences);

        final Map<I, Word<I>> accessSequences = accessAndReturnSequences.getFirst();
        final Map<I, Word<I>> returnSequences = accessAndReturnSequences.getSecond();

        return new ATRSequences<>(accessSequences, terminatingSequences, returnSequences);
    }

    /**
     * Computes for a given {@link SPA} the set of terminating sequences using the given {@link SPAAlphabet alphabet}.
     * Terminating sequences transfer a procedure from its initial state to an accepting state. This methods furthermore
     * checks that the hierarchy of calls is well-defined, i.e. it only includes procedural invocations <i>p</i> for
     * determining a terminating sequence if <i>p</i> has a valid terminating sequence itself.
     *
     * @param spa
     *         the {@link SPA} to analyze
     * @param alphabet
     *         the set of allowed alphabet symbols for determining the terminating sequences
     * @param <I>
     *         input symbol type
     *
     * @return A map from procedures (restricted to the call symbols of the given alphabet) to the terminating
     * sequences. This map may be partial as some procedures may not have a well-defined terminating sequence for the
     * given alphabet.
     */
    public static <I> Map<I, Word<I>> computeTerminatingSequences(SPA<?, I> spa, SPAAlphabet<I> alphabet) {

        final Map<I, DFA<?, I>> procedures = spa.getProcedures();
        final Map<I, Word<I>> terminatingSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());

        // initial internal sequences
        for (I procedure : alphabet.getCallAlphabet()) {
            final DFA<?, I> dfa = procedures.get(procedure);

            if (dfa != null) {
                if (dfa.accepts(Word.epsilon())) {
                    terminatingSequences.put(procedure, Word.epsilon());
                } else {
                    final Iterator<Word<I>> iter = Covers.stateCoverIterator(dfa, alphabet.getInternalAlphabet());
                    while (iter.hasNext()) {
                        final Word<I> trace = iter.next();
                        if (dfa.accepts(trace)) {
                            terminatingSequences.put(procedure, trace);
                            break;
                        }
                    }
                }
            }
        }

        final Set<I> remainingProcedures = new HashSet<>(alphabet.getCallAlphabet());
        remainingProcedures.removeAll(terminatingSequences.keySet());

        final Set<I> eligibleInputs = new HashSet<>(alphabet.getInternalAlphabet());
        eligibleInputs.addAll(terminatingSequences.keySet());

        boolean stable = false;

        while (!stable) {
            stable = true;

            for (final I i : new ArrayList<>(remainingProcedures)) {

                final DFA<?, I> dfa = procedures.get(i);

                if (dfa == null) {
                    // for non-existing procedures we cannot compute any terminating sequences
                    remainingProcedures.remove(i);
                } else {
                    final Iterator<Word<I>> iter = Covers.stateCoverIterator(dfa, eligibleInputs);

                    while (iter.hasNext()) {
                        final Word<I> trace = iter.next();
                        if (dfa.accepts(trace)) {
                            terminatingSequences.put(i, alphabet.expand(trace, terminatingSequences::get));
                            remainingProcedures.remove(i);
                            eligibleInputs.add(i);
                            stable = false;
                            break;
                        }
                    }
                }
            }
        }

        return terminatingSequences;
    }

    /**
     * Computes for a given {@link SPA} the set of access and return sequences using the given {@link SPAAlphabet
     * alphabet}. An access sequence (for procedure <i>p</i>) transfers an {@link SPA} from its initial state to a state
     * that is able to successfully execute a run of <i>p</i>, whereas the corresponding return sequence transfers the
     * {@link SPA} to the global accepting state from an accepting state of <i>p</i>. This methods furthermore checks
     * that potentially nested calls are well-defined, i.e. it only includes procedural invocations <i>p</i> for
     * determining a access/return sequences if <i>p</i> has a valid terminating sequence and therefore can be expanded
     * correctly.
     *
     * @param spa
     *         the {@link SPA} to analyze
     * @param alphabet
     *         the set of allowed alphabet symbols for determining the access and return sequences
     * @param terminatingSequences
     *         a map of terminating sequences (for a given call symbol) used to expand nested invocations in access and
     *         return sequences
     * @param <I>
     *         input symbol type
     *
     * @return A pair of maps from procedures (restricted to the call symbols of the given alphabet) to the
     * access/return sequences. tha  These maps may be partial as some procedures may not have well-defined
     * access/terminating sequences for the given alphabet.
     */
    public static <I> Pair<Map<I, Word<I>>, Map<I, Word<I>>> computeAccessAndReturnSequences(SPA<?, I> spa,
                                                                                             SPAAlphabet<I> alphabet,
                                                                                             Map<I, Word<I>> terminatingSequences) {
        final I initialProcedure = spa.getInitialProcedure();

        if (initialProcedure == null || !alphabet.isCallSymbol(initialProcedure)) {
            return Pair.of(Collections.emptyMap(), Collections.emptyMap());
        }

        final Map<I, DFA<?, I>> submodels = spa.getProcedures();
        final Collection<I> proceduralInputs = spa.getProceduralInputs(alphabet);

        final Map<I, Word<I>> accessSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        final Map<I, Word<I>> returnSequences = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        final Set<I> finishedProcedures = Sets.newHashSetWithExpectedSize(alphabet.getNumCalls());

        // initial value
        accessSequences.put(initialProcedure, Word.fromLetter(initialProcedure));
        returnSequences.put(initialProcedure, Word.fromLetter(alphabet.getReturnSymbol()));
        finishedProcedures.add(initialProcedure);

        final Deque<I> pendingProcedures = new ArrayDeque<>();
        pendingProcedures.add(initialProcedure);

        while (!pendingProcedures.isEmpty()) {
            final I i = pendingProcedures.pop();
            final DFA<?, I> dfa = submodels.get(i);

            if (dfa != null) {
                final Collection<I> newProcedures = discoverAccessAndReturnSequences(alphabet,
                                                                                     proceduralInputs,
                                                                                     i,
                                                                                     dfa,
                                                                                     finishedProcedures,
                                                                                     accessSequences,
                                                                                     terminatingSequences,
                                                                                     returnSequences);
                pendingProcedures.addAll(newProcedures);
            }
        }

        return Pair.of(accessSequences, returnSequences);
    }

    private static <I> Collection<I> discoverAccessAndReturnSequences(SPAAlphabet<I> alphabet,
                                                                      Collection<I> proceduralInputs,
                                                                      I procedure,
                                                                      DFA<?, I> dfa,
                                                                      Set<I> finishedProcedures,
                                                                      Map<I, Word<I>> accessSequences,
                                                                      Map<I, Word<I>> terminatingSequences,
                                                                      Map<I, Word<I>> returnSequences) {

        final List<I> newASRS = new ArrayList<>();
        final List<Word<I>> acceptingPaths =
                exploreAccessSequences(dfa, CollectionsUtil.randomAccessList(proceduralInputs), alphabet::isCallSymbol);

        tc:
        for (Word<I> trace : acceptingPaths) {

            for (int i = 0; i < trace.length(); i++) {
                final I input = trace.getSymbol(i);

                if (alphabet.isCallSymbol(input)) {
                    if (!finishedProcedures.contains(input)) {

                        final Word<I> remainingTrace = trace.subWord(i + 1);
                        for (I r : remainingTrace) {
                            if (alphabet.isCallSymbol(r) && !terminatingSequences.containsKey(r)) {
                                // If we encounter a call symbol for which we do not have a terminating sequence,
                                // the remaining return sequences cannot be expanded properly.
                                // Therefore, skip the current trace.
                                continue tc;
                            }
                        }

                        // we only query existing terminating sequences, therefore nullity is fine
                        @SuppressWarnings("methodref.return.invalid")
                        final Mapping<I, Word<I>> tsMapping = terminatingSequences::get;

                        final WordBuilder<I> accessBuilder = new WordBuilder<>();
                        // we only invoke this method with finished procedures
                        @SuppressWarnings("assignment.type.incompatible")
                        final @NonNull Word<I> as = accessSequences.get(procedure);
                        accessBuilder.append(as);
                        accessBuilder.append(alphabet.expand(trace.subWord(0, i), tsMapping));
                        accessBuilder.append(input);

                        accessSequences.put(input, accessBuilder.toWord());

                        final WordBuilder<I> terminatingBuilder = new WordBuilder<>();
                        // we only invoke this method with finished procedures
                        @SuppressWarnings("assignment.type.incompatible")
                        final @NonNull Word<I> rs = returnSequences.get(procedure);
                        terminatingBuilder.append(alphabet.getReturnSymbol());
                        terminatingBuilder.append(alphabet.expand(remainingTrace, tsMapping));
                        terminatingBuilder.append(rs);

                        returnSequences.put(input, terminatingBuilder.toWord());

                        finishedProcedures.add(input);
                        newASRS.add(input);
                    } else if (!terminatingSequences.containsKey(input)) {
                        // If we encounter a call symbol for which we do not have a terminating sequence,
                        // all local access sequences of future call symbols cannot be expanded properly.
                        // Therefore, skip the current trace.
                        continue tc;
                    }
                }

                if (finishedProcedures.containsAll(alphabet.getCallAlphabet())) {
                    return newASRS;
                }
            }
        }
        return newASRS;
    }

    private static <S, I> List<Word<I>> exploreAccessSequences(DFA<S, I> dfa,
                                                               List<I> inputs,
                                                               Predicate<I> callPredicate) {
        final S init = dfa.getInitialState();

        if (init == null) {
            return Collections.emptyList();
        }

        final List<Word<I>> result = new ArrayList<>(inputs.size());
        final UniversalGraph<S, TransitionEdge<I, S>, ?, ?> tgv = dfa.transitionGraphView(inputs);
        final APSPResult<S, TransitionEdge<I, S>> apsp = Graphs.findAPSP(tgv, (edge) -> 0F);

        final List<S> acceptingStates = new ArrayList<>(dfa.size());
        for (S s : dfa) {
            if (dfa.isAccepting(s)) {
                acceptingStates.add(s);
            }
        }

        calls:
        for (I i : inputs) {
            if (callPredicate.test(i)) {
                for (S s : dfa) {
                    final S succ = dfa.getSuccessor(s, i);

                    if (succ != null) {
                        final List<TransitionEdge<I, S>> init2s = apsp.getShortestPath(init, s);

                        if (init2s != null) {
                            for (S acc : acceptingStates) {
                                final List<TransitionEdge<I, S>> succ2acc = apsp.getShortestPath(succ, acc);

                                if (succ2acc != null) {
                                    final WordBuilder<I> wb = new WordBuilder<>(init2s.size() + succ2acc.size() + 1);

                                    for (TransitionEdge<I, S> t : init2s) {
                                        wb.append(t.getInput());
                                    }
                                    wb.append(i);
                                    for (TransitionEdge<I, S> t : succ2acc) {
                                        wb.append(t.getInput());
                                    }

                                    result.add(wb.toWord());
                                    continue calls;
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Convenience method for {@link #isRedundancyFree(SPA, SPAAlphabet)} that uses {@link SPA#getInputAlphabet()} as
     * {@code alphabet}.
     *
     * @param spa
     *         the {@link SPA} to check
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if {@code spa} is redundancy-free, {@code false} otherwise.
     */
    public static <I> boolean isRedundancyFree(SPA<?, I> spa) {
        return isRedundancyFree(spa, spa.getInputAlphabet());
    }

    /**
     * Checks if a given {@link SPA} is redundancy-free, i.e. if for all {@link SPA#getProcedures() procedures} there
     * exist access, terminating and return sequences.
     *
     * @param spa
     *         the {@link SPA} to check
     * @param alphabet
     *         the {@link SPAAlphabet alphabet} which should be used for computing the respective sequences
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if {@code spa} is redundancy-free, {@code false} otherwise.
     */
    public static <I> boolean isRedundancyFree(SPA<?, I> spa, SPAAlphabet<I> alphabet) {
        return isRedundancyFree(alphabet, computeATRSequences(spa, alphabet));
    }

    /**
     * Checks if a pre-computed set of {@link ATRSequences} of an {@link SPA} is redundancy-free.
     *
     * @param alphabet
     *         the {@link SPAAlphabet alphabet} which should be used for computing the respective sequences
     * @param atrSequences
     *         the pre-computed {@link ATRSequences}
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if {@code spa} is redundancy-free, {@code false} otherwise.
     *
     * @see #isRedundancyFree(SPA, SPAAlphabet)
     */
    public static <I> boolean isRedundancyFree(SPAAlphabet<I> alphabet, ATRSequences<I> atrSequences) {
        final Alphabet<I> callAlphabet = alphabet.getCallAlphabet();
        return atrSequences.accessSequences.keySet().containsAll(callAlphabet) &&
               atrSequences.terminatingSequences.keySet().containsAll(callAlphabet) &&
               atrSequences.returnSequences.keySet().containsAll(callAlphabet);
    }

    /**
     * Checks if the two given {@link SPA}s are equivalent, i.e. whether there exists a {@link #findSeparatingWord(SPA,
     * SPA, SPAAlphabet) separating word} for them.
     *
     * @param spa1
     *         the first {@link SPA}
     * @param spa2
     *         the second {@link SPA}
     * @param alphabet
     *         the {@link SPAAlphabet} which should be considered for equivalence testing
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if the two {@link SPA}s are equivalent, {@code false} otherwise
     */
    public static <I> boolean testEquivalence(SPA<?, I> spa1, SPA<?, I> spa2, SPAAlphabet<I> alphabet) {
        return findSeparatingWord(spa1, spa2, alphabet) == null;
    }

    /**
     * Computes a separating word for the two given {@link SPA}s, if existent. A separating word is a {@link Word} such
     * that one {@link SPA} {@link SPA#accepts(Iterable) behaves} different than the other.
     *
     * @param spa1
     *         the first {@link SPA}
     * @param spa2
     *         the second {@link SPA}
     * @param alphabet
     *         the {@link SPAAlphabet} that should be considered for computing the separating word
     * @param <I>
     *         input symbol type
     *
     * @return a separating word, if existent, {@code null} otherwise.
     */
    public static <I> @Nullable Word<I> findSeparatingWord(SPA<?, I> spa1, SPA<?, I> spa2, SPAAlphabet<I> alphabet) {

        final ATRSequences<I> atr1 = computeATRSequences(spa1, alphabet);
        final ATRSequences<I> atr2 = computeATRSequences(spa2, alphabet);

        for (final I procedure : alphabet.getCallAlphabet()) {
            final DFA<?, I> p1 = spa1.getProcedures().get(procedure);
            final DFA<?, I> p2 = spa2.getProcedures().get(procedure);

            if (p1 != null && p2 != null) {
                final Word<I> as1 = atr1.accessSequences.get(procedure);
                final Word<I> ts1 = atr1.terminatingSequences.get(procedure);
                final Word<I> rs1 = atr1.returnSequences.get(procedure);

                final Word<I> as2 = atr2.accessSequences.get(procedure);
                final Word<I> ts2 = atr2.terminatingSequences.get(procedure);
                final Word<I> rs2 = atr2.returnSequences.get(procedure);

                if (as1 != null && ts1 != null && rs1 != null && as2 != null && ts2 != null && rs2 != null) {
                    // we can embed both procedures and both procedures have accepting runs

                    final Set<I> localAlphabet = new HashSet<>(atr1.terminatingSequences.keySet());
                    localAlphabet.retainAll(atr2.terminatingSequences.keySet());
                    localAlphabet.addAll(alphabet.getInternalAlphabet());

                    final Word<I> sepWord = Automata.findSeparatingWord(p1, p2, localAlphabet);

                    if (sepWord != null) {
                        // select ATR based on accepting DFA, so that we don't change acceptance due to incompatible access/return sequences
                        final ATRSequences<I> acceptingATR = p1.accepts(sepWord) ? atr1 : atr2;

                        final Word<I> as = acceptingATR.accessSequences.get(procedure);
                        final Word<I> ts = alphabet.expand(sepWord, acceptingATR.terminatingSequences::get);
                        final Word<I> rs = acceptingATR.returnSequences.get(procedure);

                        return Word.fromWords(as, ts, rs);
                    }
                } else if (as1 != null && ts1 != null && rs1 != null) {
                    return Word.fromWords(as1, ts1, rs1);
                } else if (as2 != null && ts2 != null && rs2 != null) {
                    return Word.fromWords(as2, ts2, rs2);
                } // else no procedures can be embedded
            } else if (p1 != null) {
                final Word<I> as = atr1.accessSequences.get(procedure);
                final Word<I> ts = atr1.terminatingSequences.get(procedure);
                final Word<I> rs = atr1.returnSequences.get(procedure);

                if (as != null && ts != null && rs != null) {
                    return Word.fromWords(as, ts, rs);
                }
            } else if (p2 != null) {
                final Word<I> as = atr2.accessSequences.get(procedure);
                final Word<I> ts = atr2.terminatingSequences.get(procedure);
                final Word<I> rs = atr2.returnSequences.get(procedure);

                if (as != null && ts != null && rs != null) {
                    return Word.fromWords(as, ts, rs);
                }
            } // else both procedures are null and therefore skip this call symbol
        }

        return null;
    }
}
