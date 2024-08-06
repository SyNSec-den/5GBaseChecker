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
package net.automatalib.util.partitionrefinement;

import java.util.Arrays;
import java.util.function.IntFunction;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.simple.SimpleDeterministicAutomaton;
import net.automatalib.automata.simple.SimpleDeterministicAutomaton.FullIntAbstraction;
import net.automatalib.commons.util.functions.BiIntFunction;
import net.automatalib.words.Alphabet;

/**
 * This class provides methods for translating the result of a {@link PaigeTarjan} coarsest stable partition computation
 * into several common, more usable forms such as automata.
 * <p>
 * Most of the methods defined in this class expect the partition data to be in a certain form, and moreover may require
 * additional information, both of which is provided by corresponding methods defined in {@link
 * PaigeTarjanInitializers}.
 *
 * @author Malte Isberner
 */
public final class PaigeTarjanExtractors {

    private PaigeTarjanExtractors() {}

    /**
     * Translates the results of a coarsest stable partition computation into a deterministic automaton.
     * <p>
     * This method is designed to match the following methods from {@link PaigeTarjanInitializers}:
     * <ul>
     * <li> {@link PaigeTarjanInitializers#initCompleteDeterministic(PaigeTarjan, FullIntAbstraction, IntFunction,
     * boolean)}
     * </li>
     * <li> {@link PaigeTarjanInitializers#initCompleteDeterministic(PaigeTarjan,
     * net.automatalib.automata.UniversalDeterministicAutomaton.FullIntAbstraction, AutomatonInitialPartitioning,
     * boolean)}
     * </li>
     * <li> and {@link PaigeTarjanInitializers#initDeterministic(PaigeTarjan, FullIntAbstraction, IntFunction, Object)}
     * if called with {@code pruneUnreachable = true}.
     * </li>
     * </ul>
     * <p>
     * Both the {@code spExtractor} and the {@code tpExtractor} can be {@code null}, in which case they are replaced by
     * a function always returning {@code null}.
     *
     * @param pt
     *         the partition refinement data structure, after computing the coarsest stable partition
     * @param creator
     *         an {@link AutomatonCreator} for creating the resulting automaton
     * @param inputs
     *         the input alphabet to use
     * @param absOriginal
     *         the abstraction of the original automaton that was used to build the partition refinement data structure
     * @param spExtractor
     *         the state property extractor, or {@code null}
     * @param tpExtractor
     *         the transition property extractor, or {@code null}
     * @param pruneUnreachable
     *         {@code true} if unreachable states should be pruned during construction, {@code false} otherwise
     *
     * @return an automaton created using the specified creator, over the specified input alphabet, and reflecting the
     * partition data of the specified {@link PaigeTarjan} object
     */
    public static <I, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A toDeterministic(PaigeTarjan pt,
                                                                                                 AutomatonCreator<A, I> creator,
                                                                                                 Alphabet<I> inputs,
                                                                                                 SimpleDeterministicAutomaton.FullIntAbstraction absOriginal,
                                                                                                 IntFunction<? extends SP> spExtractor,
                                                                                                 BiIntFunction<? extends TP> tpExtractor,
                                                                                                 boolean pruneUnreachable) {
        if (pruneUnreachable) {
            return toDeterministicPruned(pt, creator, inputs, absOriginal, spExtractor, tpExtractor);
        }
        return toDeterministicUnpruned(pt, creator, inputs, absOriginal, spExtractor, tpExtractor);
    }

    private static <I, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A toDeterministicPruned(PaigeTarjan pt,
                                                                                                        AutomatonCreator<A, I> creator,
                                                                                                        Alphabet<I> inputs,
                                                                                                        SimpleDeterministicAutomaton.FullIntAbstraction absOriginal,
                                                                                                        IntFunction<? extends SP> spExtractor,
                                                                                                        BiIntFunction<? extends TP> tpExtractor) {

        int numBlocks = pt.getNumBlocks();
        int numInputs = inputs.size();
        int[] repMap = new int[numBlocks];
        int[] stateMap = new int[numBlocks];
        Arrays.fill(stateMap, -1);

        A result = creator.createAutomaton(inputs, numBlocks);
        MutableDeterministic.FullIntAbstraction<?, SP, TP> resultAbs = result.fullIntAbstraction(inputs);

        int origInit = absOriginal.getIntInitialState();
        SP initSp = spExtractor.apply(origInit);
        int resInit = resultAbs.addIntInitialState(initSp);

        Block initBlock = pt.getBlockForState(origInit);
        stateMap[initBlock.id] = resInit;
        repMap[resInit] = origInit;

        int statesPtr = 0;
        int numStates = 1;
        while (statesPtr < numStates) {
            int resState = statesPtr++;
            int rep = repMap[resState];
            for (int i = 0; i < numInputs; i++) {
                int succ = absOriginal.getSuccessor(rep, i);
                if (succ >= 0) {
                    TP tp = tpExtractor.apply(rep, i);
                    Block succBlock = pt.getBlockForState(succ);
                    int succBlockId = succBlock.id;
                    int resSucc = stateMap[succBlockId];
                    if (resSucc < 0) {
                        SP sp = spExtractor.apply(succ);
                        resSucc = resultAbs.addIntState(sp);
                        stateMap[succBlockId] = resSucc;
                        repMap[resSucc] = succ;
                        numStates++;
                    }
                    resultAbs.setTransition(resState, i, resSucc, tp);
                }
            }
        }

        return result;
    }

    private static <I, T, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A toDeterministicUnpruned(PaigeTarjan pt,
                                                                                                             AutomatonCreator<A, I> creator,
                                                                                                             Alphabet<I> inputs,
                                                                                                             SimpleDeterministicAutomaton.FullIntAbstraction absOriginal,
                                                                                                             IntFunction<? extends SP> spExtractor,
                                                                                                             BiIntFunction<? extends TP> tpExtractor) {

        int numBlocks = pt.getNumBlocks();
        int numInputs = inputs.size();

        A result = creator.createAutomaton(inputs, numBlocks);
        MutableDeterministic.FullIntAbstraction<?, SP, TP> resultAbs = result.fullIntAbstraction(inputs);

        for (int i = 0; i < numBlocks; i++) {
            resultAbs.addIntState();
        }

        for (Block curr : pt.blockList()) {
            int blockId = curr.id;
            int rep = pt.getRepresentative(curr);
            SP sp = spExtractor.apply(rep);
            resultAbs.setStateProperty(blockId, sp);

            for (int i = 0; i < numInputs; i++) {
                int succ = absOriginal.getSuccessor(rep, i);
                if (succ >= 0) {
                    int resSucc = pt.getBlockForState(succ).id;
                    TP tp = tpExtractor.apply(rep, i);
                    resultAbs.setTransition(blockId, i, resSucc, tp);
                }
            }
        }
        int origInit = absOriginal.getIntInitialState();
        resultAbs.setInitialState(pt.getBlockForState(origInit).id);

        return result;
    }
}
