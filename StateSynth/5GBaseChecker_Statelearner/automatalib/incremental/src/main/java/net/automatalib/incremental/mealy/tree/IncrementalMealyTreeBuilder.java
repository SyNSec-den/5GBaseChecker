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

import java.util.Objects;

import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class IncrementalMealyTreeBuilder<I, O> extends AbstractAlphabetBasedMealyTreeBuilder<I, O>
        implements IncrementalMealyBuilder<I, O> {

    public IncrementalMealyTreeBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public void insert(Word<? extends I> input, Word<? extends O> outputWord) {
        Node<O> curr = root;

        for (int i = 0; i < input.length(); i++) {
            I sym = input.getSymbol(i);
            O out = outputWord.getSymbol(i);
            Edge<Node<O>, O> edge = getEdge(curr, sym);
            if (edge == null) {
                curr = insertNode(curr, sym, out);
            } else {
                if (!Objects.equals(out, edge.getOutput())) {
                    throw new ConflictException();
                }
                curr = edge.getTarget();
            }
        }
    }
}
