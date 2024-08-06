package org.example.uelearner;

/*
 *  Copyright (c) 2016 Joeri de Ruiter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.*;

/**
 * @author Joeri de Ruiter (j.deruiter@cs.bham.ac.uk)
 * <p>
 * Based on the original by Malte Isberner
 */
public class ModifiedWMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        implements EquivalenceOracle<A, I, D> {
    private final MembershipOracle<I, D> sulOracle;
    private int maxDepth;

    /**
     * Constructor.
     *
     * @param maxDepth  the maximum length of the "middle" part of the test cases
     * @param sulOracle interface to the system under learning
     */
    public ModifiedWMethodEQOracle(int maxDepth, MembershipOracle<I, D> sulOracle) {
        this.maxDepth = maxDepth;
        this.sulOracle = sulOracle;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object,
     * java.util.Collection)
     */
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        List<Word<I>> transCover = Automata.transitionCover(hypothesis, inputs);
        List<Word<I>> charSuffixes = Automata.characterizingSet(hypothesis, inputs);

        // Special case: List of characterizing suffixes may be empty,
        // but in this case we still need to test!
        if (charSuffixes.isEmpty())
            charSuffixes = Collections.singletonList(Word.epsilon());

        WordBuilder<I> wb = new WordBuilder<>();

        DefaultQuery<I, D> query;
        D hypOutput;
        String output;
        Word<I> queryWord;
        boolean blacklisted;

        HashSet<Word<I>> blacklist = new HashSet<Word<I>>();

        for (Word<I> trans : transCover) {
            query = new DefaultQuery<>(trans);
            sulOracle.processQueries(Collections.singleton(query));

            hypOutput = hypothesis.computeOutput(trans);
            if (!Objects.equals(hypOutput, query.getOutput()))
                return query;

            output = query.getOutput().toString();

            // Detect closed connection to continue with queries with different prefixes
            if (output.endsWith("ConnectionClosed") || output.endsWith("ConnectionClosedEOF") || output.endsWith("ConnectionClosedException")) {
                blacklist.add(trans);
                continue;
            }

            //for(int start = 1; start < maxDepth; start++) {
            for (List<? extends I> middle : CollectionsUtil.allTuples(inputs, 1, maxDepth)) {
                wb.append(trans).append(middle);
                queryWord = wb.toWord();
                wb.clear();

                // Check if trans | middle has a prefix on the blacklist
                blacklisted = false;
                for (Word<I> w : blacklist) {
                    if (w.isPrefixOf(queryWord)) {
                        blacklisted = true;
                        break;
                    }
                }
                if (blacklisted) continue;

                query = new DefaultQuery<>(queryWord);
                sulOracle.processQueries(Collections.singleton(query));

                hypOutput = hypothesis.computeOutput(queryWord);

                if (!Objects.equals(hypOutput, query.getOutput()))
                    return query;

                output = query.getOutput().toString();

                if (output.endsWith("ConnectionClosed") || output.endsWith("ConnectionClosedEOF") || output.endsWith("ConnectionClosedException")) {
                    // Remember this prefix and ignore queries starting with this after this
                    blacklist.add(queryWord);
                    continue;
                }

                for (Word<I> suffix : charSuffixes) {
                    wb.append(trans).append(middle).append(suffix);
                    queryWord = wb.toWord();
                    wb.clear();

                    query = new DefaultQuery<>(queryWord);
                    hypOutput = hypothesis.computeOutput(queryWord);
                    sulOracle.processQueries(Collections.singleton(query));

                    if (!Objects.equals(hypOutput, query.getOutput()))
                        return query;

                    output = query.getOutput().toString();
                    if (output.endsWith("ConnectionClosed") || output.endsWith("ConnectionClosedEOF") || output.endsWith("ConnectionClosedException")) {
                        // Remember this prefix and ignore queries starting with this after this
                        blacklist.add(queryWord);
                    }
                }
            }
            //}
        }

        return null;
    }

    public static class DFAModifiedWMethodEQOracle<I> extends ModifiedWMethodEQOracle<DFA<?, I>, I, Boolean>
            implements DFAEquivalenceOracle<I> {
        public DFAModifiedWMethodEQOracle(int maxDepth, MembershipOracle<I, Boolean> sulOracle) {
            super(maxDepth, sulOracle);
        }
    }

    public static class MealyModifiedWMethodEQOracle<I, O> extends
            ModifiedWMethodEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>> implements MealyEquivalenceOracle<I, O> {
        public MealyModifiedWMethodEQOracle(int maxDepth, MembershipOracle<I, Word<O>> sulOracle) {
            super(maxDepth, sulOracle);
        }
    }
}
