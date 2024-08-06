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
package net.automatalib.automata.words.growing;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.GrowingAlphabet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Abstract test class defining several sanity checks for regular, growing alphabets.
 *
 * @param <I>
 *         input symbol type
 * @param <M>
 *         concrete alphabet type
 *
 * @author frohme
 */
public abstract class AbstractGrowingAlphabetTest<I, M extends GrowingAlphabet<I>> {

    private List<I> initialAlphabetSymbols;
    private List<I> additionalAlphabetSymbols;
    private List<I> allInputSymbols;
    private M alphabet;

    @BeforeClass
    public void setUp() {
        this.initialAlphabetSymbols = getInitialAlphabetSymbols();
        this.additionalAlphabetSymbols = getAdditionalAlphabetSymbols();
        this.alphabet = getInitialAlphabet();

        allInputSymbols = new ArrayList<>(initialAlphabetSymbols.size() + additionalAlphabetSymbols.size());
        allInputSymbols.addAll(initialAlphabetSymbols);
        allInputSymbols.addAll(additionalAlphabetSymbols);
    }

    protected abstract List<I> getInitialAlphabetSymbols();

    protected abstract List<I> getAdditionalAlphabetSymbols();

    protected abstract M getInitialAlphabet();

    @Test
    public void testInitialSize() {
        Assert.assertEquals(initialAlphabetSymbols.size(), alphabet.size());
    }

    @Test(dependsOnMethods = "testInitialSize")
    public void testAddAdditionalSymbols() {

        final int oldMaxIndex = initialAlphabetSymbols.size() - 1;

        for (final I i : additionalAlphabetSymbols) {
            Assert.assertTrue(alphabet.addSymbol(i) >= oldMaxIndex);
        }

        for (int i = 0; i < allInputSymbols.size(); i++) {
            final I sym = allInputSymbols.get(i);

            Assert.assertEquals(i, this.alphabet.getSymbolIndex(sym));
            Assert.assertEquals(sym, this.alphabet.getSymbol(i));
        }
    }

    @Test(dependsOnMethods = "testAddAdditionalSymbols")
    public void testAddInitialSymbols() {

        for (final I i : initialAlphabetSymbols) {
            Assert.assertTrue(alphabet.addSymbol(i) >= 0);
        }

        // adding existing symbols, shouldn't alter existing order
        for (int i = 0; i < allInputSymbols.size(); i++) {
            final I sym = allInputSymbols.get(i);

            Assert.assertTrue(this.alphabet.getSymbolIndex(sym) >= i);
            Assert.assertEquals(sym, this.alphabet.getSymbol(i));
        }
    }
}