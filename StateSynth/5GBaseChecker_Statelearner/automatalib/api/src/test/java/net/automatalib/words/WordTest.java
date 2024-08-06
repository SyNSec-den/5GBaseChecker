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
package net.automatalib.words;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.automatalib.AutomataLibSettingsTest;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Tests for static utility methods.
 *
 * @author frohme
 */
public class WordTest {

    @BeforeSuite
    public static void setUp() {
        AutomataLibSettingsTest.setUp();
    }

    @Test
    public void fromTest() {
        final Word<Character> reference = Word.fromCharSequence("abc");
        final Character[] referenceAsArray = new Character[] {'a', 'b', 'c'};
        final Word<Character> a = Word.fromLetter('a');
        final Word<Character> b = Word.fromLetter('b');
        final Word<Character> c = Word.fromLetter('c');

        Assert.assertEquals(Word.fromList(Collections.emptyList()), Word.epsilon());
        Assert.assertEquals(Word.fromList(Arrays.asList('a', 'b', 'c')), reference);

        Assert.assertEquals(Word.fromSymbols(), Word.epsilon());
        Assert.assertEquals(Word.fromSymbols('a', 'b', 'c'), reference);

        Assert.assertEquals(Word.fromWords(), Word.epsilon());
        Assert.assertEquals(Word.fromWords(a, b, c), reference);

        Assert.assertEquals(Word.fromWords(Collections.emptyList()), Word.epsilon());
        Assert.assertEquals(Word.fromWords(Arrays.asList(a, b, c)), reference);

        Assert.assertEquals(Word.fromArray(referenceAsArray, 0, 0), Word.epsilon());

        final Word<Character> wordFromArray = Word.fromArray(referenceAsArray, 1, 2);
        Assert.assertEquals(wordFromArray, reference.subWord(1));

        // check that mutating the source does not alter the word
        referenceAsArray[1] = 'x';
        Assert.assertEquals(wordFromArray, reference.subWord(1));
    }

    @Test
    public void toArrayTest() {
        final Word<Character> reference = Word.fromCharSequence("abc");
        Character[] referenceAsArray;

        referenceAsArray = new Character[3];
        reference.writeToArray(0, referenceAsArray, 0, 3);
        Assert.assertEquals('a', referenceAsArray[0].charValue());
        Assert.assertEquals('b', referenceAsArray[1].charValue());
        Assert.assertEquals('c', referenceAsArray[2].charValue());

        referenceAsArray = new Character[1];
        reference.writeToArray(2, referenceAsArray, 0, 1);
        Assert.assertEquals('c', referenceAsArray[0].charValue());

        referenceAsArray = new Character[3];
        reference.writeToArray(1, referenceAsArray, 2, 1);
        Assert.assertNull(referenceAsArray[0]);
        Assert.assertNull(referenceAsArray[1]);
        Assert.assertEquals('b', referenceAsArray[2].charValue());

        final int[] wordAsInt = reference.toIntArray(x -> x - 'a');
        Assert.assertEquals(0, wordAsInt[0]);
        Assert.assertEquals(1, wordAsInt[1]);
        Assert.assertEquals(2, wordAsInt[2]);
    }

    @Test
    public void transformTest() {
        final Word<Character> source = Word.fromCharSequence("abc");
        final Word<String> target = Word.fromSymbols("aa", "bb", "cc");

        final Word<String> transform = source.transform(c -> new String(new char[] {c, c}));

        Assert.assertEquals(target, transform);
    }

    @Test
    public void subwordTest() {
        final Word<Character> b = Word.fromLetter('b');
        final Word<Character> c = Word.fromLetter('c');
        final Word<Character> bc = Word.fromSymbols('b', 'c');
        final Word<Character> abc = Word.fromCharSequence("abc");

        Assert.assertEquals(c, abc.subWord(2));
        Assert.assertEquals(bc, abc.subWord(1));
        Assert.assertEquals(abc, abc.subWord(0));
        Assert.assertEquals(b, abc.subWord(1, 2));
    }

    @Test
    public void prefixSuffixSetTest() {
        final Word<Character> w = Word.fromCharSequence("abcdefg");
        final int n = w.size();

        List<Word<Character>> prefixes = w.prefixes(false);
        List<Word<Character>> suffixes = w.suffixes(false);

        Assert.assertEquals(prefixes.size(), n + 1);
        Assert.assertEquals(suffixes.size(), n + 1);

        for (int i = 0; i <= n; i++) {
            Assert.assertEquals(prefixes.get(i), w.prefix(i));
            Assert.assertEquals(suffixes.get(i), w.suffix(i));
        }

        prefixes = w.prefixes(true);
        suffixes = w.suffixes(true);

        Assert.assertEquals(prefixes.size(), n + 1);
        Assert.assertEquals(suffixes.size(), n + 1);

        for (int i = 0; i <= n; i++) {
            Assert.assertEquals(prefixes.get(n - i), w.prefix(i));
            Assert.assertEquals(suffixes.get(n - i), w.suffix(i));
        }
    }

    @Test
    public void toStringTest() {
        final Word<Character> empty = Word.epsilon();
        final Word<Character> abc = Word.fromCharSequence("abc");

        // See configuration in AutomataLibSettingsTest & automatalib.properties
        Assert.assertEquals(empty.toString(), "OVERRIDDEN");
        Assert.assertEquals(abc.toString(), "delim_leftsymbol_delim_leftasymbol_delim_rightsymbol_sepsymbol_delim_leftbsymbol_delim_rightsymbol_sepsymbol_delim_leftcsymbol_delim_rightdelim_right");
    }
}
