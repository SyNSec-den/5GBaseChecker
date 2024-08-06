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
package net.automatalib.automata.words.spa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import net.automatalib.automata.words.vpda.AbstractVPDAlphabetTest;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public abstract class AbstractSPAAlphabetTest<M extends SPAAlphabet<Character>>
        extends AbstractVPDAlphabetTest<Character, M> {

    private final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
    private final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
    private final char returnSymbol = 'R';

    protected abstract M getAlphabet(Alphabet<Character> internalAlphabet,
                                     Alphabet<Character> callAlphabet,
                                     char returnSymbol);

    @Override
    protected M getAlphabet() {
        return getAlphabet(internalAlphabet, callAlphabet, returnSymbol);
    }

    @Override
    protected List<Character> getCallSymbols() {
        return new ArrayList<>(callAlphabet);
    }

    @Override
    protected List<Character> getInternalSymbols() {
        return new ArrayList<>(internalAlphabet);
    }

    @Override
    protected List<Character> getReturnSymbols() {
        return Collections.singletonList(returnSymbol);
    }

    @Override
    protected List<Character> getNonAlphabetSymbols() {
        return new ArrayList<>(Alphabets.characters('x', 'z'));
    }

    @Test
    public void testFindCallIndex() {
        final M alphabet = getAlphabet();
        final Word<Character> word = Word.fromCharSequence("SaSTcRRaR");
        //                                                  012345678

        Assert.assertEquals(alphabet.findCallIndex(word, -1), -1);
        Assert.assertEquals(alphabet.findCallIndex(word, 0), -1); //S
        Assert.assertEquals(alphabet.findCallIndex(word, 1), 0); //a
        Assert.assertEquals(alphabet.findCallIndex(word, 2), 0); //S
        Assert.assertEquals(alphabet.findCallIndex(word, 3), 2); //T
        Assert.assertEquals(alphabet.findCallIndex(word, 4), 3); //c
        Assert.assertEquals(alphabet.findCallIndex(word, 5), 3); //R
        Assert.assertEquals(alphabet.findCallIndex(word, 6), 2); //R
        Assert.assertEquals(alphabet.findCallIndex(word, 7), 0); //a
        Assert.assertEquals(alphabet.findCallIndex(word, 8), 0); //R
        Assert.assertEquals(alphabet.findCallIndex(word, 9), -1);
        Assert.assertEquals(alphabet.findCallIndex(word, 10), -1);
    }

    @Test
    public void testFindReturnIndex() {
        final M alphabet = getAlphabet();
        final Word<Character> word = Word.fromCharSequence("SaSTcRRaR");
        //                                                  012345678

        Assert.assertEquals(alphabet.findReturnIndex(word, -1), -1);
        Assert.assertEquals(alphabet.findReturnIndex(word, 0), -1); //S
        Assert.assertEquals(alphabet.findReturnIndex(word, 1), 8); //a
        Assert.assertEquals(alphabet.findReturnIndex(word, 2), 8); //S
        Assert.assertEquals(alphabet.findReturnIndex(word, 3), 6); //T
        Assert.assertEquals(alphabet.findReturnIndex(word, 4), 5); //c
        Assert.assertEquals(alphabet.findReturnIndex(word, 5), 5); //R
        Assert.assertEquals(alphabet.findReturnIndex(word, 6), 6); //R
        Assert.assertEquals(alphabet.findReturnIndex(word, 7), 8); //a
        Assert.assertEquals(alphabet.findReturnIndex(word, 8), 8); //R
        Assert.assertEquals(alphabet.findReturnIndex(word, 9), -1);
        Assert.assertEquals(alphabet.findReturnIndex(word, 10), -1);
    }

    @Test
    public void testExpand() {
        final M alphabet = getAlphabet();
        final Mapping<Character, Word<Character>> ts =
                ImmutableMap.of('S', Word.fromLetter('x'), 'T', Word.fromLetter('y'))::get;

        Assert.assertEquals(alphabet.expand(Word.epsilon(), ts), Word.epsilon());
        Assert.assertEquals(alphabet.expand(Word.fromCharSequence("aSa"), ts), Word.fromCharSequence("aSxRa"));
        Assert.assertEquals(alphabet.expand(Word.fromCharSequence("bTb"), ts), Word.fromCharSequence("bTyRb"));
        Assert.assertEquals(alphabet.expand(Word.fromCharSequence("aSbTbSc"), ts),
                            Word.fromCharSequence("aSxRbTyRbSxRc"));
    }

    @Test
    public void testNormalize() {
        final M alphabet = getAlphabet();
        final Word<Character> word = Word.fromCharSequence("SaSTcRRaR");

        Assert.assertEquals(alphabet.normalize(Word.epsilon(), 0), Word.epsilon());
        Assert.assertEquals(alphabet.normalize(word, -1), Word.fromLetter('S'));
        Assert.assertEquals(alphabet.normalize(word, 0), Word.fromLetter('S')); //S
        Assert.assertEquals(alphabet.normalize(word, 1), Word.fromCharSequence("aSaR")); //a
        Assert.assertEquals(alphabet.normalize(word, 2), Word.fromCharSequence("SaR")); //S
        Assert.assertEquals(alphabet.normalize(word, 3), Word.fromCharSequence("TRaR")); //T
        Assert.assertEquals(alphabet.normalize(word, 4), Word.fromCharSequence("cRRaR")); //c
        Assert.assertEquals(alphabet.normalize(word, 5), Word.fromCharSequence("RRaR")); //R
        Assert.assertEquals(alphabet.normalize(word, 6), Word.fromCharSequence("RaR")); //R
        Assert.assertEquals(alphabet.normalize(word, 7), Word.fromCharSequence("aR")); //a
        Assert.assertEquals(alphabet.normalize(word, 8), Word.fromLetter('R')); //R
        Assert.assertEquals(alphabet.normalize(word, 9), Word.epsilon());
        Assert.assertEquals(alphabet.normalize(word, 10), Word.epsilon());
    }
}
