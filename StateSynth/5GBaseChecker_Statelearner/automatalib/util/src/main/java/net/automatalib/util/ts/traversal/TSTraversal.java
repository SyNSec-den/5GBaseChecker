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
package net.automatalib.util.ts.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import net.automatalib.commons.util.Holder;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.traversal.TraversalOrder;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author Malte Isberner
 */
public final class TSTraversal {

    public static final int NO_LIMIT = -1;

    private TSTraversal() {}

    public static <S, I, T, D> boolean depthFirst(TransitionSystem<S, I, T> ts,
                                                  Collection<? extends I> inputs,
                                                  TSTraversalVisitor<S, I, T, D> vis) {
        return depthFirst(ts, NO_LIMIT, inputs, vis);
    }

    public static <S, I, T, D> boolean depthFirst(TransitionSystem<S, ? super I, T> ts,
                                                  int limit,
                                                  Collection<? extends I> inputs,
                                                  TSTraversalVisitor<S, I, T, D> vis) {
        Deque<DFRecord<S, I, T, D>> dfsStack = new ArrayDeque<>();

        Holder<D> dataHolder = new Holder<>();

        // setting the following to false means that the traversal had to be aborted
        // due to reaching the limit
        boolean complete = true;
        int stateCount = 0;

        for (S initS : ts.getInitialStates()) {
            dataHolder.value = null;
            TSTraversalAction act = vis.processInitial(initS, dataHolder);
            switch (act) {
                case ABORT_INPUT:
                case ABORT_STATE:
                case IGNORE:
                    continue;
                case ABORT_TRAVERSAL:
                    return complete;
                case EXPLORE:
                    if (stateCount != limit) {
                        dfsStack.push(new DFRecord<>(initS, inputs, dataHolder.value));
                        stateCount++;
                    } else {
                        complete = false;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown action " + act);
            }
        }

        while (!dfsStack.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull DFRecord<S, I, T, D> current = dfsStack.peek();

            S source = current.state;
            D data = current.data;

            if (current.start(ts) && !vis.startExploration(source, data)) {
                dfsStack.pop();
                continue;
            }

            if (!current.hasNextTransition(ts)) {
                dfsStack.pop();
                continue;
            }

            I input = current.input();
            T trans = current.transition();

            S succ = ts.getSuccessor(trans);
            dataHolder.value = null;
            TSTraversalAction act = vis.processTransition(source, data, input, trans, succ, dataHolder);

            switch (act) {
                case ABORT_INPUT:
                    current.advanceInput(ts);
                    break;
                case ABORT_STATE:
                    dfsStack.pop();
                    break;
                case ABORT_TRAVERSAL:
                    return complete;
                case IGNORE:
                    current.advance(ts);
                    break;
                case EXPLORE:
                    if (stateCount != limit) {
                        dfsStack.push(new DFRecord<>(succ, inputs, dataHolder.value));
                        stateCount++;
                    } else {
                        complete = false;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown action " + act);
            }
        }

        return complete;
    }

    public static <S, I, T, D> boolean breadthFirst(TransitionSystem<S, ? super I, T> ts,
                                                    Collection<? extends I> inputs,
                                                    TSTraversalVisitor<S, I, T, D> vis) {
        return breadthFirst(ts, NO_LIMIT, inputs, vis);
    }

    /**
     * Traverses the given transition system in a breadth-first fashion. The traversal is steered by the specified
     * visitor.
     *
     * @param ts
     *         the transition system.
     * @param inputs
     *         the input alphabet.
     * @param vis
     *         the visitor.
     */
    public static <S, I, T, D> boolean breadthFirst(TransitionSystem<S, ? super I, T> ts,
                                                    int limit,
                                                    Collection<? extends I> inputs,
                                                    TSTraversalVisitor<S, I, T, D> vis) {
        Deque<BFSRecord<S, D>> bfsQueue = new ArrayDeque<>();

        // setting the following to false means that the traversal had to be aborted
        // due to reaching the limit
        boolean complete = true;
        int stateCount = 0;

        Holder<D> dataHolder = new Holder<>();

        for (S initS : ts.getInitialStates()) {
            dataHolder.value = null;
            TSTraversalAction act = vis.processInitial(initS, dataHolder);
            switch (act) {
                case ABORT_TRAVERSAL:
                    return complete;
                case EXPLORE:
                    if (stateCount != limit) {
                        bfsQueue.offer(new BFSRecord<>(initS, dataHolder.value));
                        stateCount++;
                    } else {
                        complete = false;
                    }
                    break;
                default: // case ABORT_INPUT: case ABORT_STATE: case IGNORE:
            }
        }

        while (!bfsQueue.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull BFSRecord<S, D> current = bfsQueue.poll();

            S state = current.state;
            D data = current.data;

            if (!vis.startExploration(state, data)) {
                continue;
            }

            inputs_loop:
            for (I input : inputs) {
                Collection<T> transitions = ts.getTransitions(state, input);

                for (T trans : transitions) {
                    S succ = ts.getSuccessor(trans);

                    dataHolder.value = null;
                    TSTraversalAction act = vis.processTransition(state, data, input, trans, succ, dataHolder);

                    switch (act) {
                        case ABORT_INPUT:
                            continue inputs_loop;
                        case ABORT_STATE:
                            break inputs_loop;
                        case ABORT_TRAVERSAL:
                            return complete;
                        case EXPLORE:
                            if (stateCount != limit) {
                                bfsQueue.offer(new BFSRecord<>(succ, dataHolder.value));
                                stateCount++;
                            } else {
                                complete = false;
                            }
                            break;
                        case IGNORE:
                            break;
                        default:
                            throw new IllegalStateException("Unknown action " + act);
                    }
                }
            }
        }

        return complete;
    }

    public static <S, I, T, D> boolean traverse(TraversalOrder order,
                                                TransitionSystem<S, ? super I, T> ts,
                                                Collection<? extends I> inputs,
                                                TSTraversalVisitor<S, I, T, D> vis) {
        return traverse(order, ts, NO_LIMIT, inputs, vis);
    }

    public static <S, I, T, D> boolean traverse(TraversalOrder order,
                                                TransitionSystem<S, ? super I, T> ts,
                                                int limit,
                                                Collection<? extends I> inputs,
                                                TSTraversalVisitor<S, I, T, D> vis) {
        switch (order) {
            case BREADTH_FIRST:
                return breadthFirst(ts, limit, inputs, vis);
            case DEPTH_FIRST:
                return depthFirst(ts, limit, inputs, vis);
            default:
                throw new IllegalArgumentException("Unknown traversal order: " + order);
        }
    }

}
