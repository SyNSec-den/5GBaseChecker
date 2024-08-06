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
package net.automatalib.automata.vpda;

/**
 * A specialized interface for 1-SEVPAs (1-module single entry visibly push-down automata). Note that contrary to the
 * original definition of Alur et al. where 1-SEVPAs consist of one (separate) "main" module and one module for all call
 * symbols, this implementation assumes a single module for both the "main" procedure and all call symbols, i.e., the
 * initial location and all module entries coincide.
 *
 * @param <L>
 *         location type
 * @param <I>
 *         input alphabet type
 *
 * @author Malte Isberner
 */
public interface OneSEVPA<L, I> extends SEVPA<L, I> {

    @Override
    default L getModuleEntry(I callSym) {
        return getInitialLocation();
    }

}
