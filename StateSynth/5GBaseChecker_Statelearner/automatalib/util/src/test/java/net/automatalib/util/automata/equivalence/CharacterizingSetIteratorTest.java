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
package net.automatalib.util.automata.equivalence;

import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.cover.CoverIteratorsTest;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
@Test
public class CharacterizingSetIteratorTest {

    @Test
    public void testCharacterizingSetIterator() {
        CoverIteratorsTest.compareCovers(Automata::characterizingSet, CharacterizingSets::characterizingSetIterator);
    }

    @Test
    public void testIncrementalStateCoverIterator() {
        CoverIteratorsTest.compareIncrementalCovers(CharacterizingSets::findIncrementalCharacterizingSet,
                                                    CharacterizingSets::incrementalCharacterizingSetIterator);
    }
}
