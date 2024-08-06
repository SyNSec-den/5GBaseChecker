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
package net.automatalib.util.automata.random;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;
import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.spa.SPA;
import net.automatalib.automata.spa.StackSPA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.automata.spa.SPAUtil;
import net.automatalib.util.minimizer.OneSEVPAMinimizer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.VPDAlphabet;
import org.checkerframework.checker.index.qual.NonNegative;

public final class RandomAutomata {

    private RandomAutomata() {
        // prevent instantiation
    }

    /**
     * Randomly generates an initially connected DFA (ICDFA), i.e., a DFA where all states are reachable from the
     * initial state.
     *
     * @param rand
     *         the randomness source
     * @param numStates
     *         the number of states of the generated automaton
     * @param inputs
     *         the input alphabet
     * @param minimize
     *         determines whether the DFA will be minimized before being returned. Note that if {@code true} is passed
     *         for this parameter, the resulting automaton might have a {@link Automaton#size() size} less than
     *         {@code numStates}
     *
     * @return a randomly generated ICDFA
     */
    @SuppressWarnings("nullness") // false positive?
    public static <I> CompactDFA<I> randomICDFA(Random rand,
                                                @NonNegative int numStates,
                                                Alphabet<I> inputs,
                                                boolean minimize) {
        final CompactDFA<I> dfa =
                new RandomICAutomatonGenerator<Boolean, Void>().withStateProperties(Random::nextBoolean)
                                                               .generateICDeterministicAutomaton(numStates,
                                                                                                 inputs,
                                                                                                 new CompactDFA.Creator<>(),
                                                                                                 rand);
        return minimize ? DFAs.minimize(dfa) : dfa;
    }

    public static <I> DefaultOneSEVPA<I> randomOneSEVPA(Random r,
                                                        int locCount,
                                                        VPDAlphabet<I> alphabet,
                                                        double acceptanceProb,
                                                        double initialRetTransProb,
                                                        boolean minimize) {
        return randomOneSEVPA(r,
                              locCount,
                              alphabet,
                              acceptanceProb,
                              initialRetTransProb,
                              minimize,
                              new DefaultOneSEVPA<>(alphabet, locCount));
    }

    public static <I> DefaultOneSEVPA<I> randomOneSEVPA(Random r,
                                                        int locCount,
                                                        VPDAlphabet<I> alphabet,
                                                        double acceptanceProb,
                                                        double initialRetTransProb,
                                                        boolean minimize,
                                                        DefaultOneSEVPA<I> result) {
        result.addInitialLocation(r.nextDouble() < acceptanceProb);

        for (int i = 0; i < locCount - 1; i++) {
            if (alphabet.getNumInternals() == 0 || r.nextDouble() < initialRetTransProb) {
                I retSym;
                Location srcLoc;
                int stackSym;

                do {
                    retSym = alphabet.getReturnSymbol(r.nextInt(alphabet.getNumReturns()));
                    srcLoc = result.getLocation(r.nextInt(result.size()));

                    I callSym = alphabet.getCallSymbol(r.nextInt(alphabet.getNumCalls()));
                    final Location stackLoc = result.getLocation(r.nextInt(result.size()));
                    stackSym = result.encodeStackSym(stackLoc, callSym);
                } while (result.getReturnSuccessor(srcLoc, retSym, stackSym) != null);

                final Location newLoc = result.addLocation(r.nextDouble() < acceptanceProb);
                result.setReturnSuccessor(srcLoc, retSym, stackSym, newLoc);
            } else {
                I intSym;
                Location srcLoc;

                do {
                    intSym = alphabet.getInternalSymbol(r.nextInt(alphabet.getNumInternals()));
                    srcLoc = result.getLocation(r.nextInt(result.size()));
                } while (result.getInternalSuccessor(srcLoc, intSym) != null);

                final Location newLoc = result.addLocation(r.nextDouble() < acceptanceProb);
                result.setInternalSuccessor(srcLoc, intSym, newLoc);
            }
        }

        for (Location loc : result.getLocations()) {
            for (I intSym : alphabet.getInternalAlphabet()) {
                if (result.getInternalSuccessor(loc, intSym) == null) {
                    final Location tgtLoc = result.getLocation(r.nextInt(result.size()));
                    result.setInternalSuccessor(loc, intSym, tgtLoc);
                }
            }

            for (I callSym : alphabet.getCallAlphabet()) {
                for (Location stackLoc : result.getLocations()) {
                    int stackSym = result.encodeStackSym(stackLoc, callSym);
                    for (I retSym : alphabet.getReturnAlphabet()) {
                        if (result.getReturnSuccessor(loc, retSym, stackSym) == null) {
                            final Location tgtLoc = result.getLocation(r.nextInt(result.size()));
                            result.setReturnSuccessor(loc, retSym, stackSym, tgtLoc);
                        }
                    }
                }
            }
        }

        if (minimize) {
            return OneSEVPAMinimizer.minimize(result, alphabet);
        }

        return result;
    }

    public static <I> SPA<?, I> randomSPA(Random random, SPAAlphabet<I> alphabet, int procedureSize) {
        return randomSPA(random, alphabet, procedureSize, true);
    }

    public static <I> SPA<?, I> randomSPA(Random random, SPAAlphabet<I> alphabet, int procedureSize, boolean minimize) {

        final Map<I, DFA<?, I>> dfas = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        final Alphabet<I> proceduralAlphabet = alphabet.getProceduralAlphabet();

        for (final I procedure : alphabet.getCallAlphabet()) {
            final DFA<?, I> dfa = RandomAutomata.randomDFA(random, procedureSize, proceduralAlphabet, minimize);
            dfas.put(procedure, dfa);
        }

        return new StackSPA<>(alphabet, alphabet.getCallSymbol(random.nextInt(alphabet.getNumCalls())), dfas);
    }

    public static <I> SPA<?, I> randomRedundancyFreeSPA(Random random, SPAAlphabet<I> alphabet, int procedureSize) {
        return randomRedundancyFreeSPA(random, alphabet, procedureSize, true);
    }

    public static <I> SPA<?, I> randomRedundancyFreeSPA(Random random,
                                                        SPAAlphabet<I> alphabet,
                                                        int procedureSize,
                                                        boolean minimize) {
        SPA<?, I> result;

        do {
            result = randomSPA(random, alphabet, procedureSize, minimize);
        } while (!SPAUtil.isRedundancyFree(result));

        return result;
    }

    public static <S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>> A randomDeterministic(Random rand,
                                                                                                           @NonNegative int numStates,
                                                                                                           Collection<? extends I> inputs,
                                                                                                           Collection<? extends SP> stateProps,
                                                                                                           Collection<? extends TP> transProps,
                                                                                                           A out) {
        return randomDeterministic(rand, numStates, inputs, stateProps, transProps, out, true);
    }

    public static <S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>> A randomDeterministic(Random rand,
                                                                                                           @NonNegative int numStates,
                                                                                                           Collection<? extends I> inputs,
                                                                                                           Collection<? extends SP> stateProps,
                                                                                                           Collection<? extends TP> transProps,
                                                                                                           A out,
                                                                                                           boolean minimize) {

        RandomDeterministicAutomatonGenerator<S, I, T, SP, TP, A> gen =
                new RandomDeterministicAutomatonGenerator<>(rand, inputs, stateProps, transProps, out);

        gen.addStates(numStates);
        gen.addTransitions();
        gen.chooseInitial();

        if (minimize) {
            Automata.invasiveMinimize(out, inputs);
        }

        return out;
    }

    public static <I> CompactDFA<I> randomDFA(Random rand,
                                              @NonNegative int numStates,
                                              Alphabet<I> inputs,
                                              boolean minimize) {
        return randomDeterministic(rand,
                                   numStates,
                                   inputs,
                                   DFA.STATE_PROPERTIES,
                                   DFA.TRANSITION_PROPERTIES,
                                   new CompactDFA<>(inputs),
                                   minimize);
    }

    public static <I> CompactDFA<I> randomDFA(Random rand, @NonNegative int numStates, Alphabet<I> inputs) {
        return randomDFA(rand, numStates, inputs, true);
    }

    public static <I, O> CompactMealy<I, O> randomMealy(Random rand,
                                                        @NonNegative int numStates,
                                                        Alphabet<I> inputs,
                                                        Collection<? extends O> outputs,
                                                        boolean minimize) {
        return randomDeterministic(rand,
                                   numStates,
                                   inputs,
                                   Collections.singleton(null),
                                   outputs,
                                   new CompactMealy<>(inputs),
                                   minimize);
    }

    public static <I, O> CompactMealy<I, O> randomMealy(Random rand,
                                                        @NonNegative int numStates,
                                                        Alphabet<I> inputs,
                                                        Collection<? extends O> outputs) {
        return randomMealy(rand, numStates, inputs, outputs, true);
    }

    public static <I, O> CompactMoore<I, O> randomMoore(Random rand,
                                                        @NonNegative int numStates,
                                                        Alphabet<I> inputs,
                                                        Collection<? extends O> outputs,
                                                        boolean minimize) {
        return randomDeterministic(rand,
                                   numStates,
                                   inputs,
                                   outputs,
                                   Collections.singleton(null),
                                   new CompactMoore<>(inputs),
                                   minimize);
    }

    public static <I, O> CompactMoore<I, O> randomMoore(Random rand,
                                                        @NonNegative int numStates,
                                                        Alphabet<I> inputs,
                                                        Collection<? extends O> outputs) {
        return randomMoore(rand, numStates, inputs, outputs, true);
    }

}
