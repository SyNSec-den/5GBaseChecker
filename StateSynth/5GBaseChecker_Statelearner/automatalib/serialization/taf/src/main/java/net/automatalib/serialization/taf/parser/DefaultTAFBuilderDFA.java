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
package net.automatalib.serialization.taf.parser;

import java.util.Collection;
import java.util.Set;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;

final class DefaultTAFBuilderDFA extends AbstractTAFBuilder<Integer, String, Integer, Boolean, Void, CompactDFA<String>>
        implements TAFBuilderDFA {

    DefaultTAFBuilderDFA(InternalTAFParser parser) {
        super(parser);
    }

    @Override
    public void addTransitions(String source, Collection<String> symbols, String targetId) {
        doAddTransitions(source, symbols, targetId, null);
    }

    @Override
    public void addWildcardTransitions(String source, String targetId) {
        doAddWildcardTransitions(source, targetId, null);
    }

    @Override
    protected CompactDFA<String> createAutomaton(Alphabet<String> stringAlphabet) {
        return new CompactDFA<>(stringAlphabet);
    }

    @Override
    protected Boolean getStateProperty(Set<String> options) {
        return options.remove("accepting") | options.remove("acc");
    }

    @Override
    protected String translateInput(String s) {
        return s;
    }

}
