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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class WordBuilderTest {

    @Test
    public void constructorTest() {
        WordBuilder<Character> wb;

        wb = new WordBuilder<>();
        Assert.assertEquals(0, wb.size());
        Assert.assertEquals(Word.epsilon(), wb.toWord());

        wb = new WordBuilder<>(-1);
        Assert.assertEquals(0, wb.size());
        Assert.assertEquals(Word.epsilon(), wb.toWord());

        final Word<Character> aaaaa = Word.fromCharSequence("aaaaa");

        wb = new WordBuilder<>('a', 5);
        Assert.assertEquals(5, wb.size());
        Assert.assertEquals(aaaaa, wb.toWord());

        wb = new WordBuilder<>(null, 5);
        Assert.assertEquals(5, wb.size());
        Assert.assertEquals(Word.fromSymbols(null, null, null, null, null), wb.toWord());

        wb = new WordBuilder<>(10, 'a', 5);
        Assert.assertEquals(5, wb.size());
        Assert.assertEquals(aaaaa, wb.toWord());

        wb = new WordBuilder<>(-1, 'a', 5);
        Assert.assertEquals(5, wb.size());
        Assert.assertEquals(aaaaa, wb.toWord());

        final Word<Character> abc = Word.fromCharSequence("abc");

        wb = new WordBuilder<>(abc);
        Assert.assertEquals(3, wb.size());
        Assert.assertEquals(abc, wb.toWord());

        wb = new WordBuilder<>(-1, abc);
        Assert.assertEquals(3, wb.size());
        Assert.assertEquals(abc, wb.toWord());
    }

    @Test
    public void appendTest() {
        WordBuilder<Character> wb = new WordBuilder<>();
        final Word<Character> aaa = Word.fromCharSequence("aaa");
        final Word<Character> abc = Word.fromCharSequence("abc");
        final Word<Character> abcabcabc = abc.concat(abc, abc);

        wb.append('a');
        Assert.assertEquals(1, wb.size());
        Assert.assertEquals(Word.fromSymbols('a'), wb.toWord());

        wb.clear();
        wb.append('a', 'b', 'c');
        Assert.assertEquals(3, wb.size());
        Assert.assertEquals(abc, wb.toWord());

        wb.clear();
        wb.append(abc);
        Assert.assertEquals(3, wb.size());
        Assert.assertEquals(abc, wb.toWord());

        wb.clear();
        wb.append(Arrays.asList('a', 'b', 'c'));
        Assert.assertEquals(3, wb.size());
        Assert.assertEquals(abc, wb.toWord());

        wb.clear();
        wb.append(abc, abc, abc);
        Assert.assertEquals(9, wb.size());
        Assert.assertEquals(abcabcabc, wb.toWord());

        wb.clear();
        wb.repeatAppend(3, 'a');
        Assert.assertEquals(3, wb.size());
        Assert.assertEquals(aaa, wb.toWord());

        wb.clear();
        wb.repeatAppend(3, abc);
        Assert.assertEquals(9, wb.size());
        Assert.assertEquals(abcabcabc, wb.toWord());


        final int bigChunkSize = 27;
        wb.clear();
        wb.repeatAppend(bigChunkSize, abc);
        Assert.assertEquals(bigChunkSize * 3, wb.size());

        Word<Character> buffer = Word.epsilon();
        for (int i = 0; i < bigChunkSize; i++) {
            buffer = buffer.concat(abc);
        }
        Assert.assertEquals(buffer, wb.toWord());
    }

    @Test
    public void reverseTest() {
        WordBuilder<Character> wb = new WordBuilder<>();
        final Word<Character> abc = Word.fromCharSequence("abc");
        final Word<Character> cba = Word.fromCharSequence("cba");

        wb.append(abc).reverse();
        Assert.assertEquals(cba, wb.toWord());
    }

    @Test
    public void toWordTest() {
        WordBuilder<Character> wb = new WordBuilder<>();
        final Word<Character> abc = Word.fromCharSequence("abc");

        wb.repeatAppend(3, abc);
        Assert.assertEquals(abc, wb.toWord(3, 6));

        Assert.assertThrows(IndexOutOfBoundsException.class, () -> wb.toWord(-1, wb.size()));
        Assert.assertThrows(IndexOutOfBoundsException.class, () -> wb.toWord(0, wb.size() + 1));
    }

    @Test
    public void truncateTest() {
        WordBuilder<Character> wb = new WordBuilder<>();
        final Word<Character> abc = Word.fromCharSequence("abc");
        final Word<Character> abcabc = abc.concat(abc);
        final Word<Character> abcabcabc = abcabc.concat(abc);

        wb.repeatAppend(3, abc).truncate(12);
        Assert.assertEquals(abcabcabc, wb.toWord());

        wb.clear();
        wb.repeatAppend(3, abc).truncate(6);
        Assert.assertEquals(abcabc, wb.toWord());
    }

}
