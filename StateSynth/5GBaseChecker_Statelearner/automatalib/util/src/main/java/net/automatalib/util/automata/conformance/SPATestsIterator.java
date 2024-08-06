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
package net.automatalib.util.automata.conformance;

import java.util.Iterator;
import java.util.function.BiFunction;

import com.google.common.base.Preconditions;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.spa.SPA;
import net.automatalib.commons.util.collections.AbstractTwoLevelIterator;
import net.automatalib.util.automata.spa.ATRSequences;
import net.automatalib.util.automata.spa.SPAUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A conformance test for {@link SPA}s that applies a given regular conformance test to each procedure of the {@link
 * SPA}.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class SPATestsIterator<I> extends AbstractTwoLevelIterator<I, Word<I>, Word<I>> {

    private final SPA<?, I> spa;
    private final SPAAlphabet<I> alphabet;
    private final BiFunction<DFA<?, I>, Alphabet<I>, Iterator<Word<I>>> conformanceTestProvider;

    private final ATRSequences<I> atrSequences;

    public SPATestsIterator(SPA<?, I> spa,
                            BiFunction<DFA<?, I>, Alphabet<I>, Iterator<Word<I>>> conformanceTestProvider) {
        this(spa, spa.getInputAlphabet(), conformanceTestProvider);
    }

    public SPATestsIterator(SPA<?, I> spa,
                            SPAAlphabet<I> alphabet,
                            BiFunction<DFA<?, I>, Alphabet<I>, Iterator<Word<I>>> conformanceTestProvider) {
        super(alphabet.getCallAlphabet().iterator());

        this.spa = spa;
        this.alphabet = alphabet;
        this.conformanceTestProvider = conformanceTestProvider;
        this.atrSequences = SPAUtil.computeATRSequences(spa, alphabet);

        Preconditions.checkArgument(SPAUtil.isRedundancyFree(alphabet, this.atrSequences),
                                    "The given SPA contains redundant procedures. You require me to check procedures I cannot check.");
    }

    @Override
    protected Iterator<Word<I>> l2Iterator(I callSymbol) {
        @SuppressWarnings("assignment.type.incompatible") // we check redundancy-free-ness in the constructor
        final @NonNull DFA<?, I> dfa = spa.getProcedures().get(callSymbol);
        return conformanceTestProvider.apply(dfa, alphabet.getProceduralAlphabet());
    }

    @Override
    protected Word<I> combine(I callSymbol, Word<I> testSequence) {
        @SuppressWarnings("assignment.type.incompatible") // we check redundancy-free-ness in the constructor
        final @NonNull Word<I> as = this.atrSequences.accessSequences.get(callSymbol);
        @SuppressWarnings("assignment.type.incompatible") // we check redundancy-free-ness in the constructor
        final Word<I> ts = this.alphabet.expand(testSequence, this.atrSequences.terminatingSequences::get);
        @SuppressWarnings("assignment.type.incompatible") // we check redundancy-free-ness in the constructor
        final @NonNull Word<I> rs = this.atrSequences.returnSequences.get(callSymbol);

        return Word.fromWords(as, ts, rs);
    }
}
