/* Copyright (C) 2013-2022 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package de.learnlib.algorithms.discriminationtree;

import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFA;
import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFABuilder;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.testsupport.AbstractResumableLearnerDFATest;
import net.automatalib.words.Alphabet;

/**
 * @author bainczyk
 */
public class DTLearnerDFAResumableLearnerTest
        extends AbstractResumableLearnerDFATest<DTLearnerDFA<Character>, DTLearnerState<Character, Boolean, Boolean, Void>> {

    @Override
    protected DTLearnerDFA<Character> getLearner(final MembershipOracle<Character, Boolean> oracle,
                                                 final Alphabet<Character> alphabet) {
        return new DTLearnerDFABuilder<Character>().withAlphabet(alphabet).withOracle(oracle).create();
    }

    @Override
    protected int getRounds() {
        return 5;
    }
}
