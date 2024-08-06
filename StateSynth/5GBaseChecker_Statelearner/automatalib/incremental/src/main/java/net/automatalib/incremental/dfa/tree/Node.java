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
package net.automatalib.incremental.dfa.tree;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.incremental.dfa.Acceptance;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A node in the tree internally used by {@link IncrementalDFATreeBuilder}.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public final class Node<I> {

    private Acceptance acceptance;
    private @Nullable ResizingArrayStorage<Node<I>> children;

    /**
     * Constructor. Constructs a new node with no children and an acceptance value of {@link Acceptance#DONT_KNOW}
     */
    public Node() {
        this(Acceptance.DONT_KNOW);
    }

    /**
     * Constructor. Constructs a new node with no children and the specified acceptance value.
     *
     * @param acceptance
     *         the acceptance value for the node
     */
    public Node(Acceptance acceptance) {
        this.acceptance = acceptance;
    }

    /**
     * Retrieves the acceptance value of this node.
     *
     * @return the acceptance value of this node
     */
    public Acceptance getAcceptance() {
        return acceptance;
    }

    /**
     * Sets the acceptance value for this node.
     *
     * @param acceptance
     *         the new acceptance value for this node
     */
    public void setAcceptance(Acceptance acceptance) {
        this.acceptance = acceptance;
    }

    /**
     * Retrieves, for a given index, the respective child of this node.
     *
     * @param idx
     *         the alphabet symbol index
     *
     * @return the child for the given index, or {@code null} if there is no such child
     */
    public @Nullable Node<I> getChild(int idx) {
        if (children == null) {
            return null;
        }
        return children.array[idx];
    }

    /**
     * Sets the child for a given index.
     *
     * @param idx
     *         the alphabet symbol index
     * @param alphabetSize
     *         the overall alphabet size; this is needed if a new children array needs to be created
     * @param child
     *         the new child
     */
    public void setChild(int idx, int alphabetSize, Node<I> child) {
        if (children == null) {
            children = new ResizingArrayStorage<>(Node.class, alphabetSize);
        }
        children.array[idx] = child;
    }

    public void makeSink() {
        children = null;
        acceptance = Acceptance.FALSE;
    }

    /**
     * See {@link ResizingArrayStorage#ensureCapacity(int)}.
     */
    boolean ensureInputCapacity(int capacity) {
        if (this.children == null) {
            return false;
        }

        return this.children.ensureCapacity(capacity);
    }
}
