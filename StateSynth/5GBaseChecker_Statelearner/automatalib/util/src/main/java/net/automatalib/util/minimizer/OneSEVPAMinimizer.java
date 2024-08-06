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
package net.automatalib.util.minimizer;

import net.automatalib.automata.vpda.DefaultOneSEVPA;
import net.automatalib.automata.vpda.Location;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.util.partitionrefinement.Block;
import net.automatalib.util.partitionrefinement.PaigeTarjan;
import net.automatalib.util.partitionrefinement.PaigeTarjanInitializers;
import net.automatalib.words.VPDAlphabet;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A Paige/Tarjan partition refinement based minimizer for {@link OneSEVPA}s.
 *
 * @author Malte Isberner
 */
public final class OneSEVPAMinimizer {

    private OneSEVPAMinimizer() {}

    public static <I> DefaultOneSEVPA<I> minimize(final OneSEVPA<?, I> sevpa, final VPDAlphabet<I> alphabet) {
        final PaigeTarjan pt = new PaigeTarjan();
        initPaigeTarjan(pt, sevpa, alphabet);
        pt.initWorklist(false);
        pt.computeCoarsestStablePartition();

        return fromPaigeTarjan(pt, sevpa, alphabet);
    }

    private static <L, I> void initPaigeTarjan(PaigeTarjan pt, OneSEVPA<L, I> sevpa, VPDAlphabet<I> alphabet) {
        final int numStates = sevpa.size();
        final int numInputs =
                alphabet.getNumInternals() + alphabet.getNumCalls() * alphabet.getNumReturns() * sevpa.size() * 2;

        final int posDataLow = numStates;
        final int predOfsDataLow = posDataLow + numStates;
        final int numTransitions = numStates * numInputs;
        final int predDataLow = predOfsDataLow + numTransitions + 1;
        final int dataSize = predDataLow + numTransitions;

        final int[] data = new int[dataSize];
        final Block[] blockForState = new Block[numStates];

        final Block[] initBlocks = new Block[2];

        for (int i = 0; i < numStates; i++) {
            final L loc = sevpa.getLocation(i);
            final int initBlockIdx = sevpa.isAcceptingLocation(loc) ? 1 : 0;
            Block block = initBlocks[initBlockIdx];
            if (block == null) {
                block = pt.createBlock();
                block.high = 0;
                initBlocks[initBlockIdx] = block;
            }
            block.high++;
            blockForState[i] = block;

            int predCountBase = predOfsDataLow;

            for (I intSym : alphabet.getInternalAlphabet()) {
                final L succ = sevpa.getInternalSuccessor(loc, intSym);
                if (succ == null) {
                    throw new IllegalArgumentException("Partial OneSEVPAs are not supported");
                }

                final int succId = sevpa.getLocationId(succ);
                data[predCountBase + succId]++;
                predCountBase += numStates;
            }
            for (I callSym : alphabet.getCallAlphabet()) {
                for (I retSym : alphabet.getReturnAlphabet()) {
                    for (L src : sevpa.getLocations()) {
                        int stackSym = sevpa.encodeStackSym(src, callSym);
                        L succ = sevpa.getReturnSuccessor(loc, retSym, stackSym);
                        if (succ == null) {
                            throw new IllegalArgumentException("Partial OneSEVPAs are not supported");
                        }
                        int succId = sevpa.getLocationId(succ);
                        data[predCountBase + succId]++;
                        predCountBase += numStates;

                        stackSym = sevpa.encodeStackSym(loc, callSym);
                        succ = sevpa.getReturnSuccessor(src, retSym, stackSym);
                        if (succ == null) {
                            throw new IllegalArgumentException("Partial OneSEVPAs are not supported");
                        }
                        succId = sevpa.getLocationId(succ);
                        data[predCountBase + succId]++;
                        predCountBase += numStates;
                    }
                }
            }
        }

        int curr = 0;
        for (Block b : pt.blockList()) {
            curr += b.high;
            b.high = curr;
            b.low = curr;
        }

        data[predOfsDataLow] += predDataLow;
        PaigeTarjanInitializers.prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < numStates; i++) {
            final Block b = blockForState[i];
            final int pos = --b.low;
            data[pos] = i;
            data[posDataLow + i] = pos;
            int predOfsBase = predOfsDataLow;

            final L loc = sevpa.getLocation(i);
            for (I intSym : alphabet.getInternalAlphabet()) {
                final L succ = sevpa.getInternalSuccessor(loc, intSym);
                if (succ == null) {
                    throw new IllegalArgumentException("Partial OneSEVPAs are not supported");
                }

                final int succId = sevpa.getLocationId(succ);
                data[--data[predOfsBase + succId]] = i;
                predOfsBase += numStates;
            }
            for (I callSym : alphabet.getCallAlphabet()) {
                for (I retSym : alphabet.getReturnAlphabet()) {
                    for (L src : sevpa.getLocations()) {
                        int stackSym = sevpa.encodeStackSym(src, callSym);
                        L succ = sevpa.getReturnSuccessor(loc, retSym, stackSym);
                        if (succ == null) {
                            throw new IllegalArgumentException("Partial OneSEVPAs are not supported");
                        }
                        int succId = sevpa.getLocationId(succ);
                        data[--data[predOfsBase + succId]] = i;
                        predOfsBase += numStates;

                        stackSym = sevpa.encodeStackSym(loc, callSym);
                        succ = sevpa.getReturnSuccessor(src, retSym, stackSym);
                        if (succ == null) {
                            throw new IllegalArgumentException("Partial OneSEVPAs are not supported");
                        }
                        succId = sevpa.getLocationId(succ);
                        data[--data[predOfsBase + succId]] = i;
                        predOfsBase += numStates;
                    }
                }
            }
        }

        pt.setBlockData(data);
        pt.setPosData(data, posDataLow);
        pt.setPredOfsData(data, predOfsDataLow);
        pt.setPredData(data);
        pt.setBlockForState(blockForState);
        pt.setSize(numStates, numInputs);
    }

    private static <L, I> DefaultOneSEVPA<I> fromPaigeTarjan(final PaigeTarjan pt,
                                                             final OneSEVPA<L, I> original,
                                                             final VPDAlphabet<I> alphabet) {

        final int numBlocks = pt.getNumBlocks();
        final DefaultOneSEVPA<I> result = new DefaultOneSEVPA<>(alphabet, numBlocks);

        final Location[] resultLocs = new Location[numBlocks];
        for (int i = 0; i < resultLocs.length; i++) {
            resultLocs[i] = result.addLocation(false);
        }

        for (Block curr : pt.blockList()) {
            final int blockId = curr.id;
            final int rep = pt.getRepresentative(curr);
            final L repLoc = original.getLocation(rep);

            final Location resultLoc = resultLocs[blockId];
            resultLoc.setAccepting(original.isAcceptingLocation(repLoc));

            for (I intSym : alphabet.getInternalAlphabet()) {
                @SuppressWarnings("nullness") // partiality is handled during initialization
                final @NonNull L origSucc = original.getInternalSuccessor(repLoc, intSym);
                final int origSuccId = original.getLocationId(origSucc);
                final int resSuccId = pt.getBlockForState(origSuccId).id;
                final Location resSucc = resultLocs[resSuccId];
                result.setInternalSuccessor(resultLoc, intSym, resSucc);
            }
            for (I callSym : alphabet.getCallAlphabet()) {
                for (I retSym : alphabet.getReturnAlphabet()) {
                    for (Block b : pt.blockList()) {
                        final int stackRepId = pt.getRepresentative(b);
                        final L stackRep = original.getLocation(stackRepId);
                        final Location resultStackRep = resultLocs[b.id];

                        final int origStackSym = original.encodeStackSym(stackRep, callSym);
                        @SuppressWarnings("nullness") // partiality is handled during initialization
                        final @NonNull L origSucc = original.getReturnSuccessor(repLoc, retSym, origStackSym);
                        final int origSuccId = original.getLocationId(origSucc);
                        final int resSuccId = pt.getBlockForState(origSuccId).id;
                        final Location resSucc = resultLocs[resSuccId];

                        final int stackSym = result.encodeStackSym(resultStackRep, callSym);
                        result.setReturnSuccessor(resultLoc, retSym, stackSym, resSucc);
                    }
                }
            }
        }

        final int origInit = original.getLocationId(original.getInitialLocation());
        result.setInitialLocation(resultLocs[pt.getBlockForState(origInit).id]);

        return result;
    }

}
