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
package net.automatalib.util.automata.ads;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.automatalib.words.Word;

/**
 * Utility class originally used by the algorithm of {@link LeeYannakakis} but utilized by other ADS computations as
 * well.
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
class SplitTree<S, I, O> {

    private final Map<O, SplitTree<S, I, O>> successors;
    private final Map<S, S> mapping;
    private final Set<S> partition;

    private Word<I> sequence;

    SplitTree(final Set<S> partition) {
        this(partition, new HashMap<>());
    }

    SplitTree(final Set<S> partition, final Map<S, S> mapping) {
        this.partition = partition;
        this.mapping = mapping;

        this.successors = new HashMap<>();
        this.sequence = Word.epsilon();
    }

    public Map<O, SplitTree<S, I, O>> getSuccessors() {
        return successors;
    }

    public Map<S, S> getMapping() {
        return mapping;
    }

    public Set<S> getPartition() {
        return partition;
    }

    public Word<I> getSequence() {
        return sequence;
    }

    public void setSequence(Word<I> sequence) {
        this.sequence = sequence;
    }

    public Optional<SplitTree<S, I, O>> findLowestSubsetNode(final Set<S> nodes) {

        for (final SplitTree<S, I, O> st : successors.values()) {
            final Optional<SplitTree<S, I, O>> candidate = st.findLowestSubsetNode(nodes);

            if (candidate.isPresent()) {
                return candidate;
            }
        }

        if (this.partition.containsAll(nodes)) {
            return Optional.of(this);
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return String.format("[states: %s, seq=%s]", this.partition.toString(), this.sequence.toString());
    }
}
