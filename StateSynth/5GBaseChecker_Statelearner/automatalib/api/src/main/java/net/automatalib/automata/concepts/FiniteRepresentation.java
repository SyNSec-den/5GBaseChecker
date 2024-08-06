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
package net.automatalib.automata.concepts;

import net.automatalib.automata.simple.SimpleAutomaton;

/**
 * This interface marks automata types that have a finite representation, i.e. can be represented by a finite number of
 * entities. Note that this does not necessarily correspond to <i>states</i> in the usual sense (like in the case of
 * {@link SimpleAutomaton}). For example, push-down systems can have infinitely many states but may be representable
 * with a finite amount of locations.
 *
 * @author frohme
 */
public interface FiniteRepresentation {

    /**
     * Returns the number of entities required to represent this system.
     *
     * @return the number of entities required to represent this system
     */
    int size();
}
