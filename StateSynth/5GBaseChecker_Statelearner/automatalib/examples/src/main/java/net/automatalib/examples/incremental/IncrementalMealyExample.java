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

import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IncrementalMealyExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncrementalMealyExample.class);

    private static final Alphabet<Character> ALPHABET = Alphabets.characters('a', 'c');
    private static final Word<Character> W_1 = Word.fromString("abc");
    private static final Word<Character> W_1_O = Word.fromString("xyz");
    private static final Word<Character> W_2 = Word.fromString("ac");
    private static final Word<Character> W_2_O = Word.fromString("xw");
    private static final Word<Character> W_3 = Word.fromString("acb");
    private static final Word<Character> W_3_O = Word.fromString("xwu");

    private IncrementalMealyExample() {}

    public static void main(String[] args) {
        LOGGER.info("Incremental construction using a tree");
        IncrementalMealyBuilder<Character, Character> incMealyTree = new IncrementalMealyTreeBuilder<>(ALPHABET);
        build(incMealyTree);

        LOGGER.info("Incremental construction using a DAG");
        IncrementalMealyBuilder<Character, Character> incMealyDag = new IncrementalMealyDAGBuilder<>(ALPHABET);
        build(incMealyDag);
    }

    public static void build(IncrementalMealyBuilder<Character, Character> incMealy) {
        LOGGER.info("  Inserting {} / {}", W_1, W_1_O);
        incMealy.insert(W_1, W_1_O);
        Visualization.visualize(incMealy.asGraph());

        LOGGER.info("  Inserting {} / {}", W_2, W_2_O);
        incMealy.insert(W_2, W_2_O);
        Visualization.visualize(incMealy.asGraph());

        LOGGER.info("  Inserting {} / {}", W_3, W_3_O);
        incMealy.insert(W_3, W_3_O);
        Visualization.visualize(incMealy.asGraph());
    }
}
