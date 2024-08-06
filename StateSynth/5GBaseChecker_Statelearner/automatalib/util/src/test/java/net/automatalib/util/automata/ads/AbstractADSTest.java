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
package net.automatalib.util.automata.ads;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.ads.ADSNode;
import net.automatalib.words.Word;
import org.testng.Assert;

/**
 * @author frohme
 */
public abstract class AbstractADSTest {

    protected <I, O> void verifySuccess(final CompactMealy<I, O> mealy) {
        this.verifySuccess(mealy, mealy.getStates());
    }

    protected <I, O> void verifySuccess(final CompactMealy<I, O> mealy, final Collection<Integer> targets) {
        this.verifySuccess(mealy, new HashSet<>(targets));
    }

    protected <I, O> void verifySuccess(final CompactMealy<I, O> mealy, final Set<Integer> targets) {
        final Optional<ADSNode<Integer, I, O>> defaultADS = ADS.compute(mealy, mealy.getInputAlphabet(), targets);
        final Optional<ADSNode<Integer, I, O>> bestEffortADS =
                BacktrackingSearch.compute(mealy, mealy.getInputAlphabet(), targets);
        final Optional<ADSNode<Integer, I, O>> bfsMinLengthADS = BacktrackingSearch.computeOptimal(mealy,
                                                                                                   mealy.getInputAlphabet(),
                                                                                                   targets,
                                                                                                   BacktrackingSearch.CostAggregator.MIN_LENGTH);
        final Optional<ADSNode<Integer, I, O>> bfsMinSizeADS = BacktrackingSearch.computeOptimal(mealy,
                                                                                                 mealy.getInputAlphabet(),
                                                                                                 targets,
                                                                                                 BacktrackingSearch.CostAggregator.MIN_SIZE);

        Assert.assertTrue(defaultADS.isPresent());
        Assert.assertTrue(bestEffortADS.isPresent());
        Assert.assertTrue(bfsMinLengthADS.isPresent());
        Assert.assertTrue(bfsMinSizeADS.isPresent());

        final ADSNode<Integer, I, O> def = defaultADS.get();
        final ADSNode<Integer, I, O> bestEffort = bestEffortADS.get();
        final ADSNode<Integer, I, O> bfsMinLength = bestEffortADS.get();
        final ADSNode<Integer, I, O> bfsMinSize = bestEffortADS.get();

        this.verifySuccess(mealy, targets, def);
        this.verifySuccess(mealy, targets, bestEffort);
        this.verifySuccess(mealy, targets, bfsMinLength);
        this.verifySuccess(mealy, targets, bfsMinSize);

        final int defaultLength = ADSUtil.computeLength(def);
        final int bestEffortLength = ADSUtil.computeLength(bestEffort);
        final int bfsMinLengthLength = ADSUtil.computeLength(bfsMinLength);
        final int bfsMinSizeLength = ADSUtil.computeLength(bfsMinSize);

        Assert.assertTrue(bfsMinLengthLength <= defaultLength);
        Assert.assertTrue(bfsMinLengthLength <= bestEffortLength);
        Assert.assertTrue(bfsMinLengthLength <= bfsMinSizeLength);

        final int defaultSize = ADSUtil.countSymbolNodes(def);
        final int bestEffortSize = ADSUtil.countSymbolNodes(bestEffort);
        final int bfsMinLengthSize = ADSUtil.countSymbolNodes(bfsMinLength);
        final int bfsMinSizeSize = ADSUtil.countSymbolNodes(bfsMinSize);

        Assert.assertTrue(bfsMinSizeSize <= defaultSize);
        Assert.assertTrue(bfsMinSizeSize <= bestEffortSize);
        Assert.assertTrue(bfsMinSizeSize <= bfsMinLengthSize);
    }

    protected <I, O> void verifySuccess(final CompactMealy<I, O> mealy,
                                        final Set<Integer> targets,
                                        final ADSNode<Integer, I, O> ads) {

        final Set<ADSNode<Integer, I, O>> leaves = ADSUtil.collectLeaves(ads);

        Assert.assertEquals(targets, leaves.stream().map(ADSNode::getHypothesisState).collect(Collectors.toSet()));

        final Map<ADSNode<Integer, I, O>, Pair<Word<I>, Word<O>>> traces =
                Maps.asMap(leaves, ADSUtil::buildTraceForNode);

        // check matching outputs
        for (Map.Entry<ADSNode<Integer, I, O>, Pair<Word<I>, Word<O>>> entry : traces.entrySet()) {
            final Integer state = entry.getKey().getHypothesisState();
            final Word<I> input = entry.getValue().getFirst();
            final Word<O> output = entry.getValue().getSecond();
            Assert.assertEquals(mealy.computeStateOutput(state, input), output);
        }

        // check uniqueness of ADSs
        final Set<Word<O>> outputSet = traces.values().stream().map(Pair::getSecond).collect(Collectors.toSet());
        Assert.assertEquals(traces.size(), outputSet.size());
    }

    protected <I, O> void verifyFailure(final CompactMealy<I, O> mealy) {
        this.verifyFailure(mealy, mealy.getStates());
    }

    protected <I, O> void verifyFailure(final CompactMealy<I, O> mealy, final Collection<Integer> targets) {
        this.verifyFailure(mealy, new HashSet<>(targets));
    }

    protected <I, O> void verifyFailure(final CompactMealy<I, O> mealy, final Set<Integer> targets) {
        final Optional<ADSNode<Integer, I, O>> defaultADS = ADS.compute(mealy, mealy.getInputAlphabet(), targets);
        final Optional<ADSNode<Integer, I, O>> bestEffortADS =
                BacktrackingSearch.compute(mealy, mealy.getInputAlphabet(), targets);
        final Optional<ADSNode<Integer, I, O>> bfsMinLengthADS = BacktrackingSearch.computeOptimal(mealy,
                                                                                                   mealy.getInputAlphabet(),
                                                                                                   targets,
                                                                                                   BacktrackingSearch.CostAggregator.MIN_LENGTH);
        final Optional<ADSNode<Integer, I, O>> bfsMinSizeADS = BacktrackingSearch.computeOptimal(mealy,
                                                                                                 mealy.getInputAlphabet(),
                                                                                                 targets,
                                                                                                 BacktrackingSearch.CostAggregator.MIN_SIZE);

        Assert.assertFalse(defaultADS.isPresent());
        Assert.assertFalse(bestEffortADS.isPresent());
        Assert.assertFalse(bfsMinLengthADS.isPresent());
        Assert.assertFalse(bfsMinSizeADS.isPresent());
    }
}
