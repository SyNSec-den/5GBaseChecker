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
package net.automatalib.util.automata.conformance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An iterator that enumerates the test cases as obtained through the <i>W method</i> conformance test in an incremental
 * fashion.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class IncrementalWMethodTestsIterator<I> implements Iterator<Word<I>> {

    private final Alphabet<I> alphabet;
    private final StrictPriorityQueue<Item<I>> itemQueue;
    private final List<Word<I>> prefixes = new ArrayList<>();
    private final List<Word<I>> suffixes = new ArrayList<>();
    private int maxDepth;

    public IncrementalWMethodTestsIterator(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
        this.itemQueue = new StrictPriorityQueue<>(new ItemComparator<>(alphabet), new ItemMerge<>());
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void update(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton) {
        int oldNumPrefixes = prefixes.size();
        boolean newPrefixes = Covers.incrementalTransitionCover(automaton, alphabet, prefixes, prefixes);

        int oldNumSuffixes = suffixes.size();
        boolean newSuffixes = Automata.incrementalCharacterizingSet(automaton, alphabet, suffixes, suffixes);

        // old prefixes with all *new* suffixes
        if (newSuffixes && oldNumPrefixes > 0) {
            Item<I> item = new Item<>();
            item.prefixIdx = 0;
            item.minPrefix = 0;
            item.maxPrefix = oldNumPrefixes;
            item.suffixIdx = oldNumSuffixes;
            item.minSuffix = oldNumSuffixes;
            item.middle = Word.epsilon();
            itemQueue.insert(item);
        }
        // new prefixes with *all* suffixes
        if (newPrefixes) {
            Item<I> item = new Item<>();
            item.prefixIdx = oldNumPrefixes;
            item.minPrefix = oldNumPrefixes;
            item.maxPrefix = prefixes.size();
            item.suffixIdx = 0;
            item.minSuffix = 0;
            item.middle = Word.epsilon();
            itemQueue.insert(item);
        }
    }

    @Override
    public boolean hasNext() {
        return !itemQueue.isEmpty();
    }

    @Override
    public Word<I> next() {
        Item<I> nextItem = itemQueue.extractMin();

        Word<I> result = assembleWord(nextItem);
        @Nullable Item<I> inc = increment(nextItem);
        if (inc != null) {
            itemQueue.insert(inc);
        }
        return result;
    }

    private Word<I> assembleWord(Item<I> item) {
        Word<I> prefix = prefixes.get(item.prefixIdx);
        if (suffixes.isEmpty()) {
            return prefix.concat(item.middle);
        }
        return prefix.concat(item.middle, suffixes.get(item.suffixIdx));
    }

    private @Nullable Item<I> increment(Item<I> item) {
        item.suffixIdx++;
        if (item.suffixIdx >= suffixes.size()) {
            item.suffixIdx = item.minSuffix;

            item.prefixIdx++;
            if (item.prefixIdx >= item.maxPrefix) {
                item.prefixIdx = item.minPrefix;

                item.middle = item.middle.canonicalNext(alphabet);
                if (item.middle.length() > maxDepth) {
                    return null;
                }
            }
        }

        return item;
    }

    private static final class Item<I> {

        private int prefixIdx;
        private int suffixIdx;
        private Word<I> middle;

        private int minSuffix;
        private int minPrefix;
        private int maxPrefix;

        @Override
        public String toString() {
            return prefixIdx + " | " + middle + " | " + suffixIdx;
        }
    }

    private static final class ItemMerge<I> implements StrictPriorityQueue.MergeOperation<Item<I>> {

        @Override
        public Item<I> merge(Item<I> oldObject, Item<I> newObject) {
            oldObject.minSuffix = Math.min(oldObject.minSuffix, newObject.minSuffix);
            oldObject.minPrefix = Math.min(oldObject.minPrefix, newObject.minPrefix);
            oldObject.maxPrefix = Math.max(oldObject.maxPrefix, newObject.maxPrefix);
            return oldObject;
        }

    }

    private static final class ItemComparator<I> implements Comparator<Item<I>> {

        private final Comparator<Word<I>> canonicalCmp;

        ItemComparator(Comparator<? super I> symComparator) {
            this.canonicalCmp = Word.canonicalComparator(symComparator);
        }

        @Override
        public int compare(Item<I> o1, Item<I> o2) {
            int cmp = canonicalCmp.compare(o1.middle, o2.middle);
            if (cmp != 0) {
                return cmp;
            }

            cmp = o1.prefixIdx - o2.prefixIdx;
            if (cmp != 0) {
                return cmp;
            }

            return o1.suffixIdx - o2.suffixIdx;
        }
    }

}
