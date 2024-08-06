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
package net.automatalib.util.ts.acceptors;

public interface AcceptanceCombiner {

    AcceptanceCombiner AND = (a1, a2) -> a1 && a2;
    AcceptanceCombiner OR = (a1, a2) -> a1 || a2;
    AcceptanceCombiner XOR = (a1, a2) -> a1 ^ a2;
    AcceptanceCombiner EQUIV = (a1, a2) -> a1 == a2;
    AcceptanceCombiner IMPL = (a1, a2) -> !a1 || a2;

    /**
     * Combine two acceptance values.
     *
     * @param a1 first value
     * @param a2 second value
     *
     * @return the combined value
     */
    boolean combine(boolean a1, boolean a2);
}
