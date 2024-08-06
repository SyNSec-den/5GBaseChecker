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

/**
 * An edge in the internal automaton representation.
 *
 * @param <S>
 *         state class.
 * @param <T>
 *         transition label class.
 *
 * @author Malte Isberner
 */
final class Edge<S, T> {

    // source state
    private final State<S, T> source;

    // target state
    private final State<S, T> target;

    // transition label
    private final TransitionLabel<S, T> transitionLabel;

    /**
     * Constructor.
     *
     * @param source
     *         the source state.
     * @param target
     *         the target state.
     * @param transitionLabel
     *         the transition label.
     */
    Edge(State<S, T> source, State<S, T> target, TransitionLabel<S, T> transitionLabel) {
        this.source = source;
        this.target = target;
        this.transitionLabel = transitionLabel;
    }

    /**
     * Retrieves the source state.
     *
     * @return the source state.
     */
    public State<S, T> getSource() {
        return source;
    }

    /**
     * Retrieves the transition label.
     *
     * @return the transition label.
     */
    public TransitionLabel<S, T> getTransitionLabel() {
        return transitionLabel;
    }

    /**
     * Retrieves the target state.
     *
     * @return the target state.
     */
    public State<S, T> getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "(" + source + ", " + transitionLabel + ", " + target + ")";
    }

}