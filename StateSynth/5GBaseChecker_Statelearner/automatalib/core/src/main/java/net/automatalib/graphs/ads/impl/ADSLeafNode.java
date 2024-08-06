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
package net.automatalib.graphs.ads.impl;

import net.automatalib.graphs.ads.ADSNode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An ADS-specific implementation of a leaf node.
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
public class ADSLeafNode<S, I, O> extends AbstractRecursiveADSLeafNode<S, I, O, ADSNode<S, I, O>>
        implements ADSNode<S, I, O> {

    public ADSLeafNode(@Nullable ADSNode<S, I, O> parent, S hypothesisState) {
        super(parent, hypothesisState);
    }
}
