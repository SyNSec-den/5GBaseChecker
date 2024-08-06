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
package net.automatalib.incremental.mealy.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractAlphabetBasedMealyTreeBuilder<I, O> extends AbstractMealyTreeBuilder<Node<O>, I, O>
        implements InputAlphabetHolder<I> {

    private final Alphabet<I> inputAlphabet;
    private int alphabetSize;

    AbstractAlphabetBasedMealyTreeBuilder(Alphabet<I> inputAlphabet) {
        super(new Node<>(inputAlphabet.size()));
        this.inputAlphabet = inputAlphabet;
        this.alphabetSize = inputAlphabet.size();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!inputAlphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(inputAlphabet).addSymbol(symbol);
        }

        final int newAlphabetSize = inputAlphabet.size();
        // even if the symbol was already in the alphabet, we need to make sure to be able to store the new symbol
        if (alphabetSize < newAlphabetSize) {
            ensureInputCapacity(root, alphabetSize, newAlphabetSize);
            alphabetSize = newAlphabetSize;
        }
    }

    private void ensureInputCapacity(Node<O> node, int oldAlphabetSize, int newAlphabetSize) {
        node.ensureInputCapacity(newAlphabetSize);
        for (int i = 0; i < oldAlphabetSize; i++) {
            final Node<O> child = node.getSuccessor(i);
            if (child != null) {
                ensureInputCapacity(child, oldAlphabetSize, newAlphabetSize);
            }
        }
    }

    @Override
    protected @Nullable Edge<Node<O>, O> getEdge(Node<O> node, I symbol) {
        return node.getEdge(inputAlphabet.getSymbolIndex(symbol));
    }

    @Override
    protected Node<O> createNode() {
        return new Node<>(alphabetSize);
    }

    @Override
    protected Node<O> insertNode(Node<O> parent, I symIdx, O output) {
        Node<O> succ = createNode();
        Edge<Node<O>, O> edge = new Edge<>(output, succ);
        parent.setEdge(inputAlphabet.getSymbolIndex(symIdx), edge);
        return succ;
    }

    @Override
    protected Collection<AnnotatedEdge<Node<O>, I, O>> getOutgoingEdges(Node<O> node) {
        List<AnnotatedEdge<Node<O>, I, O>> result = new ArrayList<>(alphabetSize);
        for (int i = 0; i < alphabetSize; i++) {
            Edge<Node<O>, O> edge = node.getEdge(i);
            if (edge != null) {
                result.add(new AnnotatedEdge<>(edge, inputAlphabet.getSymbol(i)));
            }
        }
        return result;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }
}
