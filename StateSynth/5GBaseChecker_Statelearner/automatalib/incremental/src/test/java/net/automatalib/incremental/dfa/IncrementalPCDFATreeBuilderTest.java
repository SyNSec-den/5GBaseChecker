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
package net.automatalib.incremental.dfa;

import net.automatalib.incremental.dfa.tree.IncrementalPCDFATreeBuilder;
import net.automatalib.words.Alphabet;
import org.testng.annotations.Test;

@Test
public class IncrementalPCDFATreeBuilderTest extends AbstractIncrementalPCDFABuilderTest {

    @Override
    protected <I> IncrementalDFABuilder<I> createIncrementalPCDFABuilder(Alphabet<I> alphabet) {
        return new IncrementalPCDFATreeBuilder<>(alphabet);
    }

}
