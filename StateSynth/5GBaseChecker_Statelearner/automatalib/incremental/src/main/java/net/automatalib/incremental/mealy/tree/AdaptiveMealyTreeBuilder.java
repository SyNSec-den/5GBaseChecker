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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import net.automatalib.incremental.mealy.AdaptiveMealyBuilder;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AdaptiveMealyTreeBuilder<I, O> extends AbstractAlphabetBasedMealyTreeBuilder<I, O>
        implements AdaptiveMealyBuilder<I, O> {

    private final Map<Node<O>, Word<I>> nodeToQuery;

    public AdaptiveMealyTreeBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
        this.nodeToQuery = new LinkedHashMap<>();
    }

    @Override
    public boolean insert(Word<? extends I> input, Word<? extends O> outputWord) {
        Node<O> curr = root;
        boolean hasOverwritten = false;

        for (int i = 0; i < input.length(); i++) {
            I sym = input.getSymbol(i);
            O out = outputWord.getSymbol(i);
            Edge<Node<O>, O> edge = getEdge(curr, sym);
            if (edge == null) {
                curr = insertNode(curr, sym, out);
            } else {
                if (!Objects.equals(out, edge.getOutput())) {
                    hasOverwritten = true;
                    removeQueries(edge.getTarget());
                    removeEdge(curr, sym);
                    curr = insertNode(curr, sym, out);
                } else {
                    curr = edge.getTarget();
                }
            }
        }

        assert curr != null;
        // Make sure it uses the new ages.
        nodeToQuery.remove(curr);
        nodeToQuery.put(curr, Word.upcast(input));

        return hasOverwritten;
    }

    private void removeQueries(Node<O> node) {
        GraphTraversal.bfIterator(this.asGraph(), Collections.singleton(node)).forEachRemaining(nodeToQuery::remove);
    }

    private void removeEdge(Node<O> node, I symbol) {
        node.setEdge(this.getInputAlphabet().getSymbolIndex(symbol), null);
    }

    @Override
    public @Nullable Word<I> getOldestInput() {
        final Iterator<Word<I>> iter = nodeToQuery.values().iterator();
        return iter.hasNext() ? iter.next() : null;
    }
}
