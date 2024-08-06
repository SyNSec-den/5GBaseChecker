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
package net.automatalib.graphs;

import java.util.Map;

import net.automatalib.automata.concepts.FiniteRepresentation;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.ts.TransitionSystem;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a Context-Free Modal Transition System as defined in the paper <a href="http://dx.doi.org/10.1007/978-3-030-00244-2_15">M3C:
 * Modal Meta Model Checking</a>. Note that we use the term <i>process system</i> from the <a
 * href="https://link.springer.com/chapter/10.1007/BFb0084787">original paper</a> to prevent confusion with
 * AutomataLib's concept of {@link TransitionSystem}s.
 *
 * @param <L>
 *         edge label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author frohme
 * @author murtovi
 */
public interface ContextFreeModalProcessSystem<L, AP> extends FiniteRepresentation, GraphViewable {

    Map<L, ProceduralModalProcessGraph<?, L, ?, AP, ?>> getPMPGs();

    @Nullable L getMainProcess();

    @Override
    default int size() {
        return getPMPGs().values().stream().mapToInt(ProceduralModalProcessGraph::size).sum();
    }

    @Override
    default Graph<?, ?> graphView() {
        // explicit type specification is required by checker-framework
        return new CFMPSGraphView<@Nullable Object, L, @Nullable Object, AP>(getPMPGs());
    }
}
