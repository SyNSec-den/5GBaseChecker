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

import org.testng.annotations.Test;

@Test
public class LetterWordTest extends AbstractNonemptyWordTest {

    @Override
    protected List<Word<Object>> realPrefixes() {
        return realSuffixes();
    }

    @Override
    protected List<Word<Object>> realSuffixes() {
        return Arrays.asList(new EmptyWord(),
                             new SharedWord<>(new Object[0]),
                             new SharedWord<>(Collections.emptyList()));
    }

    @Override
    protected Word<Object> testWord() {
        return new LetterWord<>(5);
    }

    @Override
    protected List<Word<Object>> equalWords() {
        return Arrays.asList(new LetterWord<>(5),
                             new SharedWord<>(new Object[] {5}),
                             new SharedWord<>(Collections.<Object>singletonList(5)));
    }

    @Override
    protected List<Word<Object>> unequalWords() {
        return Arrays.asList(new LetterWord<>(6),
                             new LetterWord<>(null),
                             new SharedWord<>(new Object[] {4, 2}),
                             new EmptyWord());
    }

}
