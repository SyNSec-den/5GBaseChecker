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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.math.LongMath;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.ads.ADSNode;
import net.automatalib.graphs.ads.impl.ADSSymbolNode;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Utility class, that offers some operations revolving around adaptive distinguishing sequences.
 *
 * @author frohme
 */
public final class ADSUtil {

    private ADSUtil() {}

    public static <S, I, O> int computeLength(final ADSNode<S, I, O> node) {
        if (node.isLeaf()) {
            return 0;
        }

        return 1 + node.getChildren().values().stream().mapToInt(ADSUtil::computeLength).max().orElse(0);
    }

    public static <S, I, O> int countSymbolNodes(final ADSNode<S, I, O> node) {
        if (node.isLeaf()) {
            return 0;
        }

        return 1 + node.getChildren().values().stream().mapToInt(ADSUtil::countSymbolNodes).sum();
    }

    public static <S, I, T, O> Pair<ADSNode<S, I, O>, ADSNode<S, I, O>> buildFromTrace(final MealyMachine<S, I, T, O> automaton,
                                                                                       final Word<I> trace,
                                                                                       final S state) {
        final Iterator<I> sequenceIter = trace.iterator();
        final I input = sequenceIter.next();
        final ADSNode<S, I, O> head = new ADSSymbolNode<>(null, input);

        ADSNode<S, I, O> tempADS = head;
        I tempInput = input;
        S tempState = state;

        while (sequenceIter.hasNext()) {
            final I nextInput = sequenceIter.next();
            final ADSNode<S, I, O> nextNode = new ADSSymbolNode<>(tempADS, nextInput);

            final T trans = automaton.getTransition(tempState, tempInput);
            assert trans != null;
            final O oldOutput = automaton.getTransitionOutput(trans);

            tempADS.getChildren().put(oldOutput, nextNode);

            tempADS = nextNode;
            tempState = automaton.getSuccessor(trans);
            tempInput = nextInput;
        }

        return Pair.of(head, tempADS);
    }

    public static <S, I, O> Set<ADSNode<S, I, O>> collectLeaves(final ADSNode<S, I, O> root) {
        final Set<ADSNode<S, I, O>> result = new LinkedHashSet<>();
        collectLeavesRecursively(result, root);
        return result;
    }

    private static <S, I, O> void collectLeavesRecursively(final Set<ADSNode<S, I, O>> nodes,
                                                           final ADSNode<S, I, O> current) {
        if (current.isLeaf()) {
            nodes.add(current);
        } else {
            for (ADSNode<S, I, O> n : current.getChildren().values()) {
                collectLeavesRecursively(nodes, n);
            }
        }
    }

    public static <S, I, O> Pair<Word<I>, Word<O>> buildTraceForNode(final ADSNode<S, I, O> node) {

        ADSNode<S, I, O> parentIter = node.getParent();
        ADSNode<S, I, O> nodeIter = node;

        final WordBuilder<I> inputBuilder = new WordBuilder<>();
        final WordBuilder<O> outputBuilder = new WordBuilder<>();

        while (parentIter != null) {
            inputBuilder.append(parentIter.getSymbol());
            outputBuilder.append(getOutputForSuccessor(parentIter, nodeIter));

            nodeIter = parentIter;
            parentIter = parentIter.getParent();
        }

        return Pair.of(inputBuilder.reverse().toWord(), outputBuilder.reverse().toWord());
    }

    public static <S, I, O> O getOutputForSuccessor(final ADSNode<S, I, O> node, final ADSNode<S, I, O> successor) {

        if (!node.equals(successor.getParent())) {
            throw new IllegalArgumentException("No parent relationship");
        }

        for (Map.Entry<O, ADSNode<S, I, O>> entry : node.getChildren().entrySet()) {
            if (entry.getValue().equals(successor)) {
                return entry.getKey();
            }
        }

        throw new IllegalArgumentException("No child relationship");
    }

    /**
     * Computes an upper bound for the length of a splitting word. Based on
     * <p>
     * I.V. Kogan. "Estimated Length of a Minimal Simple Conditional Diagnostic Experiment". In: Automation and Remote
     * Control 34 (1973)
     *
     * @param n
     *         the size of the automaton (number of states)
     * @param i
     *         the number of states that should be distinguished by the current splitting word
     * @param m
     *         the number of states that should originally be distinguished
     *
     * @return upper bound for the length of a splitting word
     */
    public static long computeMaximumSplittingWordLength(final int n, final int i, final int m) {
        if (m == 2) {
            return n;
        }

        return LongMath.binomial(n, i) - LongMath.binomial(m - 1, i - 1) - 1;
    }

}
