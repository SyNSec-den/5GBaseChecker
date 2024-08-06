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
package net.automatalib.modelcheckers.m3c.solver;

import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;

/**
 * An interface for a generic M3C solver which may need to parse the given formula and thus may throw an exception when
 * doing so.
 *
 * @param <F>
 *         formula type
 *
 * @author frohme
 */
public interface M3CSolver<F> {

    /**
     * Checks whether the given formula is satisfied.
     *
     * @param formula
     *         the formula whose satisfiability should be checked
     *
     * @return {@code true} if the formula is satisfied, {@code false} otherwise.
     *
     * @throws ParseException
     *         when the given formula object cannot be parsed
     */
    boolean solve(F formula) throws ParseException;

    /**
     * A specialized {@link M3CSolver} which no longer throws a {@link ParseException} when solving a formula, but
     * requires a type-safe formula object.
     *
     * @param <F>
     *         formula type
     *
     * @author frohme
     */
    interface TypedM3CSolver<F> extends M3CSolver<F> {

        /**
         * Checks whether the given formula is satisfied.
         *
         * @param formula
         *         the formula whose satisfiability should be checked
         *
         * @return {@code true} if the formula is satisfied, {@code false} otherwise.
         */
        @Override
        boolean solve(F formula);
    }
}
