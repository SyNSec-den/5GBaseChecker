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

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class EmptyWordTest extends AbstractWordTest {

    @Test
    public void testLength() {
        Assert.assertEquals(0, testWord.length());
    }

    @Override
    protected Word<Object> testWord() {
        return new EmptyWord();
    }

    @Override
    protected List<Word<Object>> equalWords() {
        return Arrays.asList(new SharedWord<>(new Object[0]),
                             new SharedWord<>(Collections.emptyList()),
                             new SharedWord<>(new Object[3], 2, 0));
    }

    @Override
    protected List<Word<Object>> unequalWords() {
        return Arrays.asList(new LetterWord<>(new Object()),
                             new SharedWord<>(new Object[3]),
                             new SharedWord<>(Arrays.<Object>asList(1, 2, 3)));
    }

    @Override
    @Test
    public void testIsEmpty() {
        super.testIsEmpty();
        Assert.assertTrue(testWord.isEmpty());
    }

}
