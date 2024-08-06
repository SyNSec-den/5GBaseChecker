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
package net.automatalib.incremental.mealy.tree;

public final class AnnotatedEdge<N, I, O> {

    private final Edge<N, O> edge;
    private final I input;

    public AnnotatedEdge(Edge<N, O> edge, I input) {
        this.edge = edge;
        this.input = input;
    }

    public Edge<N, O> getEdge() {
        return edge;
    }

    public I getInput() {
        return input;
    }

    public O getOutput() {
        return edge.getOutput();
    }

    public N getTarget() {
        return edge.getTarget();
    }
}
