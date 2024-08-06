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

import java.util.BitSet;
import java.util.Collection;

import net.automatalib.commons.smartcollections.AbstractBasicLinkedListEntry;
import net.automatalib.commons.smartcollections.IntrusiveLinkedList;
import net.automatalib.commons.smartcollections.UnorderedCollection;

/**
 * Represents a transition label in the automaton model the minimizer operates on.
 *
 * @param <S>
 *         state class.
 * @param <EP>
 *         edge property class.
 *
 * @author Malte Isberner
 */
final class TransitionLabel<S, EP> extends AbstractBasicLinkedListEntry<TransitionLabel<S, EP>, TransitionLabel<S, EP>> {

    // The original label
    private final EP originalLabel;
    // The list of states that have an outgoing transition
    // with this label, considered in the respective step.
    private final UnorderedCollection<State<S, EP>> list = new UnorderedCollection<>();
    // Keeps track of the states that are contained in the above list.
    private final BitSet setContents = new BitSet();

    // The bucket, used for the weak sort of the algorithm.
    private final IntrusiveLinkedList<State<S, EP>> bucket = new IntrusiveLinkedList<>();

    /**
     * Constructor.
     *
     * @param originalLabel
     *         the original transition label.
     */
    TransitionLabel(EP originalLabel) {
        this.originalLabel = originalLabel;
    }

    /**
     * Retrieves the bucket.
     *
     * @return the bucket.
     */
    public IntrusiveLinkedList<State<S, EP>> getBucket() {
        return bucket;
    }

    /**
     * Adds a state to this label's bucket.
     *
     * @param state
     *         the state to be added to the bucket.
     *
     * @return <code>true</code> if this is the first state to be added to the bucket, <code>false</code> otherwise.
     */
    public boolean addToBucket(State<S, EP> state) {
        boolean first = false;

        if (bucket.isEmpty()) {
            first = true;
        }
        bucket.pushBack(state);

        return first;
    }

    /**
     * Retrieves the original transition label.
     *
     * @return the original transition label.
     */
    public EP getOriginalLabel() {
        return originalLabel;
    }

    /**
     * Clears the state set associated with this label.
     */
    public void clearSet() {
        setContents.clear();
        list.quickClear();
    }

    /**
     * Adds a state to the associated state set. Note that a state can be in the sets of various transition labels.
     *
     * @param state
     *         the state to be added.
     *
     * @return <code>true</code> if this was the first state to be added to the set, <code>false</code> otherwise.
     */
    public boolean addToSet(State<S, EP> state) {
        boolean first = list.isEmpty();
        if (first || !setContents.get(state.getId())) {
            list.add(state);
            setContents.set(state.getId());
        }
        return first;
    }

    /**
     * Retrieves the state set associated with this transition label. Note that despite the fact that no {@link
     * java.util.Set} is returned, each state is guaranteed to occur at most once.
     *
     * @return the state set of this label.
     */
    public Collection<State<S, EP>> getSet() {
        return list;
    }

    @Override
    public String toString() {
        return String.valueOf(originalLabel);
    }

    @Override
    public TransitionLabel<S, EP> getElement() {
        return this;
    }
}
