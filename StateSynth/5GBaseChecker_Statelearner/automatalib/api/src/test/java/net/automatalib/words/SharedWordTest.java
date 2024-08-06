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
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class SharedWordTest extends AbstractNonemptyWordTest {

    private static final Object[] DATA = new Object[] {2, 1, 3, 3, 7, 9};
    private static final int OFFSET = 1;
    private static final int LENGTH = 4;

    @Test
    public void testLength() {
        Assert.assertEquals(LENGTH, testWord.length());
    }

    @Override
    protected Word<Object> testWord() {
        return new SharedWord<>(DATA, OFFSET, LENGTH);
    }

    @Override
    protected List<Word<Object>> equalWords() {
        return Arrays.asList(new SharedWord<>(Arrays.asList(1, 3, 3, 7)), new SharedWord<>(new Object[] {1, 3, 3, 7}));
    }

    @Override
    protected List<Word<Object>> unequalWords() {
        return Arrays.asList(new SharedWord<>(DATA),
                             new SharedWord<>(Arrays.asList(DATA)),
                             new EmptyWord(),
                             new SharedWord<>(Arrays.asList(2, 4)));
    }

    @Override
    @Test
    public void testAsList() {
        super.testAsList();
        Assert.assertEquals(Arrays.asList(1, 3, 3, 7), testWord.asList());
    }

    @Override
    protected List<Word<Object>> realPrefixes() {
        return Arrays.asList(new EmptyWord(),
                             new SharedWord<>(Arrays.asList(1, 3)),
                             new SharedWord<>(new Object[] {1, 3, 3}),
                             new LetterWord<>(1));
    }

    @Override
    protected List<Word<Object>> realSuffixes() {
        return Arrays.asList(new EmptyWord(),
                             new SharedWord<>(Arrays.asList(3, 7)),
                             new SharedWord<>(new Object[] {3, 3, 7}),
                             new LetterWord<>(7));
    }

}
