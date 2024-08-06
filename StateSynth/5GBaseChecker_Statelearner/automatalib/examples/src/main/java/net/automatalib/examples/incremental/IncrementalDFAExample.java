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
package net.automatalib.examples.incremental;

import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.incremental.dfa.dag.IncrementalDFADAGBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalDFATreeBuilder;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IncrementalDFAExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncrementalDFAExample.class);
    private static final Alphabet<Character> ALPHABET = Alphabets.characters('a', 'c');

    private IncrementalDFAExample() {}

    public static void main(String[] args) {
        LOGGER.info("Incremental construction using a Tree");
        IncrementalDFABuilder<Character> incDfaTree = new IncrementalDFATreeBuilder<>(ALPHABET);
        build(incDfaTree);

        LOGGER.info("Incremental construction using a DAG");
        IncrementalDFABuilder<Character> incDfaDag = new IncrementalDFADAGBuilder<>(ALPHABET);
        build(incDfaDag);
    }

    public static void build(IncrementalDFABuilder<Character> incDfa) {
        Word<Character> w1 = Word.fromString("abc");
        Word<Character> w2 = Word.fromString("ac");
        Word<Character> w3 = Word.fromString("acb");
        Word<Character> w4 = Word.epsilon();

        LOGGER.info("  Inserting {} as accepted", w1);
        incDfa.insert(w1, true);
        Visualization.visualize(incDfa.asGraph());

        LOGGER.info("  Inserting {} as rejected", w2);
        incDfa.insert(w2, false);
        Visualization.visualize(incDfa.asGraph());

        LOGGER.info("  Inserting {} as accepted", w3);
        incDfa.insert(w3, true);
        Visualization.visualize(incDfa.asGraph());

        LOGGER.info("  Inserting {} as accepted", w4);
        incDfa.insert(w4, true);
        Visualization.visualize(incDfa.asGraph());
    }
}
