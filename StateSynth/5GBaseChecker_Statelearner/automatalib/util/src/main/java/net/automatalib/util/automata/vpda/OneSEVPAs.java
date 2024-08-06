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

import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.commons.util.Pair;
import net.automatalib.util.minimizer.OneSEVPAMinimizer;
import net.automatalib.util.ts.acceptors.AcceptanceCombiner;
import net.automatalib.words.VPDAlphabet;

/**
 * Operations on {@link OneSEVPA}s.
 *
 * @author frohme
 */
public final class OneSEVPAs {

    private OneSEVPAs() {
        // prevent instantiation
    }

    /**
     * Calculates the conjunction ("and") of two SEVPA, and returns the result as a new SEVPA.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a new SEVPA representing the conjunction of the specified SEVPA
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> and(final OneSEVPA<L1, I> sevpa1,
                                                            final OneSEVPA<L2, I> sevpa2,
                                                            final VPDAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.AND);
    }

    /**
     * Most general way of combining two SEVPAs. The {@link AcceptanceCombiner} specified via the {@code combiner}
     * parameter specifies how acceptance values of the SEVPAs will be combined to an acceptance value in the result
     * SEVPAs.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     * @param combiner
     *         combination method for acceptance values
     *
     * @return a new SEVPA representing the conjunction of the specified SEVPA
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> combine(final OneSEVPA<L1, I> sevpa1,
                                                                final OneSEVPA<L2, I> sevpa2,
                                                                final VPDAlphabet<I> alphabet,
                                                                final AcceptanceCombiner combiner) {
        return new ProductOneSEVPA<>(alphabet, sevpa1, sevpa2, combiner);
    }

    /**
     * Calculates the disjunction ("or") of two SEVPA, and returns the result as a new SEVPA.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a new SEVPA representing the conjunction of the specified SEVPA
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> or(final OneSEVPA<L1, I> sevpa1,
                                                           final OneSEVPA<L2, I> sevpa2,
                                                           final VPDAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.OR);
    }

    /**
     * Calculates the exclusive-or ("xor") of two SEVPA, and stores the result in a given mutable SEVPA.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a new SEVPA representing the conjunction of the specified SEVPA
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> xor(final OneSEVPA<L1, I> sevpa1,
                                                            final OneSEVPA<L2, I> sevpa2,
                                                            final VPDAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.XOR);
    }

    /**
     * Calculates the equivalence ("&lt;=&gt;") of two SEVPA, and stores the result in a given mutable SEVPA.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a new SEVPA representing the conjunction of the specified SEVPA
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> equiv(final OneSEVPA<L1, I> sevpa1,
                                                              final OneSEVPA<L2, I> sevpa2,
                                                              final VPDAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.EQUIV);
    }

    /**
     * Calculates the implication ("=&gt;") of two SEVPA, and stores the result in a given mutable SEVPA.
     *
     * @param sevpa1
     *         the first SEVPA
     * @param sevpa2
     *         the second SEVPA
     * @param alphabet
     *         the input alphabet
     *
     * @return a new SEVPA representing the conjunction of the specified SEVPA
     */
    public static <L1, L2, I> OneSEVPA<Pair<L1, L2>, I> impl(final OneSEVPA<L1, I> sevpa1,
                                                             final OneSEVPA<L2, I> sevpa2,
                                                             final VPDAlphabet<I> alphabet) {
        return combine(sevpa1, sevpa2, alphabet, AcceptanceCombiner.IMPL);
    }

    /**
     * Minimizes the given SEVPA over the given alphabet. This method does not modify the given SEVPA, but returns the
     * minimized version as a new instance. <b>Note:</b> the SEVPA must be completely specified.
     *
     * @param sevpa
     *         the SEVPA to be minimized
     * @param alphabet
     *         the input alphabet to consider for minimization (this will also be the input alphabet of the resulting
     *         automaton)
     *
     * @return a minimized version of the specified SEVPA
     */
    public static <I> DefaultOneSEVPA<I> minimize(OneSEVPA<?, I> sevpa, VPDAlphabet<I> alphabet) {
        return OneSEVPAMinimizer.minimize(sevpa, alphabet);
    }

}
