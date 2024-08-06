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
package net.automatalib.automata.words.vpda;

import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Abstract test class defining several sanity checks for visibly-pushdown alphabets.
 *
 * @param <I>
 *         input symbol type
 * @param <M>
 *         concrete alphabet type
 *
 * @author frohme
 */
public abstract class AbstractVPDAlphabetTest<I, M extends VPDAlphabet<I>> {

    private List<I> callSymbols;
    private List<I> internalSymbols;
    private List<I> returnSymbols;
    private List<I> nonAlphabetSymbols;
    private M alphabet;

    @BeforeClass
    public void setUp() {
        this.callSymbols = getCallSymbols();
        this.internalSymbols = getInternalSymbols();
        this.returnSymbols = getReturnSymbols();
        this.nonAlphabetSymbols = getNonAlphabetSymbols();
        this.alphabet = getAlphabet();
    }

    protected abstract List<I> getCallSymbols();

    protected abstract List<I> getInternalSymbols();

    protected abstract List<I> getReturnSymbols();

    protected abstract List<I> getNonAlphabetSymbols();

    protected abstract M getAlphabet();

    @Test
    public void testSize() {
        Assert.assertEquals(callSymbols.size() + internalSymbols.size() + returnSymbols.size(), alphabet.size());
    }

    @Test
    public void testIndices() {
        testIndices(callSymbols, alphabet.getCallAlphabet());
        testIndices(internalSymbols, alphabet.getInternalAlphabet());
        testIndices(returnSymbols, alphabet.getReturnAlphabet());
    }

    private void testIndices(final List<I> source, final Alphabet<I> alphabet) {
        for (int i = 0; i < source.size(); i++) {
            final I sym = source.get(i);

            Assert.assertEquals(i, alphabet.getSymbolIndex(sym));
            Assert.assertEquals(sym, alphabet.getSymbol(i));
        }
    }

    @Test
    public void testOutOfBoundsIndex() {
        testOutOfBoundsIndex(callSymbols, alphabet.getCallAlphabet());
        testOutOfBoundsIndex(internalSymbols, alphabet.getInternalAlphabet());
        testOutOfBoundsIndex(returnSymbols, alphabet.getReturnAlphabet());
    }

    private void testOutOfBoundsIndex(final List<I> source, final Alphabet<I> alphabet) {
        Assert.assertThrows(() -> alphabet.getSymbol(-1));
        Assert.assertThrows(() -> alphabet.getSymbol(source.size() + 1));
    }

    @Test
    public void testNonContainedSymbols() {
        testNonContainedSymbols(alphabet.getCallAlphabet());
        testNonContainedSymbols(alphabet.getInternalAlphabet());
        testNonContainedSymbols(alphabet.getReturnAlphabet());
    }

    private void testNonContainedSymbols(final Alphabet<I> alphabet) {
        for (final I i : this.nonAlphabetSymbols) {
            Assert.assertThrows(() -> alphabet.getSymbolIndex(i));
        }
    }

    @Test
    public void testWellMatchednessChecks() {
        final M alphabet = getAlphabet();
        final I c1 = getNthElementIfPossible(callSymbols, 0);
        final I c2 = getNthElementIfPossible(callSymbols, 1);
        final I i1 = getNthElementIfPossible(internalSymbols, 0);
        final I i2 = getNthElementIfPossible(internalSymbols, 1);
        final I r1 = getNthElementIfPossible(returnSymbols, 0);
        final I r2 = getNthElementIfPossible(returnSymbols, 1);

        final Word<I> w1 = Word.fromSymbols(c1, c2, i1, i1, i2, r1, i2, c2, r2, r2);

        Assert.assertTrue(alphabet.isWellMatched(w1));
        Assert.assertTrue(alphabet.isCallMatched(w1));
        Assert.assertTrue(alphabet.isReturnMatched(w1));
        Assert.assertEquals(alphabet.callReturnBalance(w1), 0);
        Assert.assertEquals(alphabet.longestWellMatchedPrefix(w1), w1);
        Assert.assertEquals(alphabet.longestWellMatchedSuffix(w1), w1);

        final Word<I> w2 = Word.fromSymbols(i1, c2, c1, i1, i2, r2);

        Assert.assertFalse(alphabet.isWellMatched(w2));
        Assert.assertFalse(alphabet.isCallMatched(w2));
        Assert.assertTrue(alphabet.isReturnMatched(w2));
        Assert.assertEquals(alphabet.callReturnBalance(w2), 1);
        Assert.assertEquals(alphabet.longestWellMatchedPrefix(w2), w2.prefix(1));
        Assert.assertEquals(alphabet.longestWellMatchedSuffix(w2), w2.suffix(4));

        final Word<I> w3 = Word.fromSymbols(i1, c1, i2, r2, i2, r1, i1, r1, c2);

        Assert.assertFalse(alphabet.isWellMatched(w3));
        Assert.assertFalse(alphabet.isCallMatched(w3));
        Assert.assertFalse(alphabet.isReturnMatched(w3));
        Assert.assertEquals(alphabet.callReturnBalance(w3), -1);
        Assert.assertEquals(alphabet.longestWellMatchedPrefix(w3), w3.prefix(5));
        Assert.assertEquals(alphabet.longestWellMatchedSuffix(w3), w3.suffix(0));
    }

    private I getNthElementIfPossible(List<I> alphabet, int idx) {
        Assert.assertFalse(alphabet.isEmpty());
        return alphabet.get(Math.min(alphabet.size() - 1, idx));
    }
}
