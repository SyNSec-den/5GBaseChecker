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
package net.automatalib.incremental.dfa.dag;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Incrementally builds an (acyclic) DFA, from a set of positive and negative words. Using {@link #insert(Word,
 * boolean)}, either the set of words definitely in the target language or definitely <i>not</i> in the target language
 * is augmented. The {@link #lookup(Word)} method then returns, for a given word, whether this word is in the set of
 * definitely accepted words ({@link Acceptance#TRUE}), definitely rejected words ({@link Acceptance#FALSE}), or neither
 * ({@link Acceptance#DONT_KNOW}).
 *
 * @param <I>
 *         input symbol class
 *
 * @author Malte Isberner
 */
public class IncrementalDFADAGBuilder<I> extends AbstractIncrementalDFADAGBuilder<I> {

    /**
     * Constructor. Initializes the incremental builder.
     *
     * @param inputAlphabet
     *         the input alphabet to use
     */
    public IncrementalDFADAGBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    /**
     * Checks the ternary acceptance status for a given word.
     *
     * @param word
     *         the word to check
     *
     * @return the acceptance status for the given word
     */
    @Override
    public Acceptance lookup(Word<? extends I> word) {
        State s = getState(word);
        if (s == null) {
            return Acceptance.DONT_KNOW;
        }
        return s.getAcceptance();
    }

    /**
     * Inserts a word into either the set of accepted or rejected words.
     *
     * @param word
     *         the word to insert
     * @param accepting
     *         whether to insert this word into the set of accepted or rejected words.
     */
    @Override
    public void insert(Word<? extends I> word, boolean accepting) {
        int len = word.length();
        Acceptance acc = Acceptance.fromBoolean(accepting);

        State curr = init;
        State conf = null;

        Deque<PathElem> path = new ArrayDeque<>();

        // Find the internal state in the automaton that can be reached by a
        // maximal prefix of the word (i.e., a path of secured information)
        for (I sym : word) {
            // During this, store the *first* confluence state (i.e., state with multiple incoming edges).
            if (conf == null && curr.isConfluence()) {
                conf = curr;
            }

            int idx = inputAlphabet.getSymbolIndex(sym);
            State succ = curr.getSuccessor(idx);
            if (succ == null) {
                break;
            }
            path.push(new PathElem(curr, idx));
            curr = succ;
        }

        int prefixLen = path.size();

        State last = curr;

        if (prefixLen == len) {
            // structural skeleton for this word already present
            Acceptance currAcc = curr.getAcceptance();
            if (currAcc == acc) {
                // consistent with our existing knowledge, nothing changes
                return;
            }

            if (currAcc != Acceptance.DONT_KNOW) {
                // conflict
                throw new ConflictException("Incompatible acceptances: " + currAcc + " vs " + acc);
            }
            if (conf != null || last.isConfluence()) {
                // there is a confluence (maybe in the last node), so duplicate
                // the last node (this will have to be propagated)
                last = clone(last, acc);
            } else if (last == init) {
                // we inserted the empty word, so update the acceptance of the initial
                // state (nothing else changes)
                updateInitSignature(acc);
                return;
            } else {
                // no confluence, so just update the signature
                last = updateSignature(last, acc);
            }
        } else {
            // we had to abort after processing a prefix
            if (conf != null) {
                if (conf == last) {
                    // if the first confluence is the last node, this confluence gets resolved
                    // directly by cloning (so act as if there was no confluence)
                    conf = null;
                }
                // confluence always requires cloning, to separate this path from other paths
                last = hiddenClone(last);
                if (conf == null) {
                    PathElem peek = path.peek();
                    assert peek != null;
                    State prev = peek.state;
                    if (prev != init) {
                        updateSignature(prev, peek.transIdx, last);
                    } else {
                        updateInitSignature(peek.transIdx, last);
                    }
                }
            } else if (last != init) {
                hide(last);
            }

            // We then create a suffix path, i.e., a linear sequence of states corresponding to
            // the suffix (more precisely: the suffix minus the first symbol, since this is the
            // transition which is used for gluing the suffix path to the existing automaton).
            @SuppressWarnings("assignment.type.incompatible") // TODO maybe properly enforce @KeyFor(this)?
            Word<? extends I> suffix = word.subWord(prefixLen);
            I sym = suffix.firstSymbol();
            int suffTransIdx = inputAlphabet.getSymbolIndex(sym);
            State suffixState = createSuffix(suffix.subWord(1), acc);

            if (last != init) {
                last = unhide(last, suffTransIdx, suffixState);

                // the suffixState may be part of our current path and become confluent due to un-hiding
                if (suffixState.isConfluence()) {
                    // update the reference with whatever state comes first
                    final Iterator<PathElem> iter = path.descendingIterator();
                    while (iter.hasNext()) {
                        final State s = iter.next().state;
                        if (s == conf || s == suffixState) {
                            conf = s;
                            break;
                        }
                    }
                }
            } else {
                updateInitSignature(suffTransIdx, suffixState);
            }
        }

        if (path.isEmpty()) {
            return;
        }

        if (conf != null) {
            PathElem next;
            do {
                next = path.pop();
                State state = next.state;
                int idx = next.transIdx;
                state = clone(state, idx, last);
                last = state;
            } while (next.state != conf);
        }

        while (path.size() > 1) {
            PathElem next = path.pop();
            State state = next.state;
            int idx = next.transIdx;

            // when extending the path we previously traversed (i.e. expanding the suffix), it may happen that we end up
            // adding a cyclic transition. If this is the case, simply clone the current state and update the parent in
            // the next iteration
            if (state == last) {
                last = clone(state, idx, last);
                continue;
            }

            State updated = updateSignature(state, idx, last);
            if (state == updated) {
                return;
            }
            last = updated;
        }

        int finalIdx = path.pop().transIdx;

        updateInitSignature(finalIdx, last);
    }

    /**
     * Creates a suffix state sequence, i.e., a linear sequence of states connected by transitions labeled by the
     * letters of the given suffix word.
     *
     * @param suffix
     *         the suffix word
     * @param acc
     *         the acceptance status of the final state
     *
     * @return the first state in the sequence
     */
    private State createSuffix(Word<? extends I> suffix, Acceptance acc) {
        StateSignature sig = new StateSignature(alphabetSize, acc);
        sig.updateHashCode();
        State last = replaceOrRegister(sig);

        int len = suffix.length();
        for (int i = len - 1; i >= 0; i--) {
            sig = new StateSignature(alphabetSize, Acceptance.DONT_KNOW);
            I sym = suffix.getSymbol(i);
            int idx = inputAlphabet.getSymbolIndex(sym);
            sig.successors.array[idx] = last;
            sig.updateHashCode();
            last = replaceOrRegister(sig);
        }

        return last;
    }

    /**
     * Retrieves the state reached by a given word.
     *
     * @param word
     *         the word
     *
     * @return the state reached by the given word, or {@code null} if no state is reachable by that word
     */
    @Override
    protected @Nullable State getState(Word<? extends I> word) {
        State s = init;

        for (I sym : word) {
            int idx = inputAlphabet.getSymbolIndex(sym);
            s = s.getSuccessor(idx);
            if (s == null) {
                return null;
            }
        }
        return s;
    }

}
