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
package net.automatalib.graphs.ads;

import java.util.Collection;

/**
 * A (simplified) node in an adaptive distinguishing sequence. See {@link RecursiveADSNode}.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
public interface ADSNode<S, I, O> extends RecursiveADSNode<S, I, O, ADSNode<S, I, O>> {

    @Override
    default Collection<ADSNode<S, I, O>> getNodes() {
        return getNodesForRoot(this);
    }
}
