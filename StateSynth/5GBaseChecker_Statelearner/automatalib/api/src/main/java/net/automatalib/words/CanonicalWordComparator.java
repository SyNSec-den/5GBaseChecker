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

import java.util.Comparator;

import net.automatalib.commons.util.comparison.CmpUtil;

class CanonicalWordComparator<I> implements Comparator<Word<I>> {

    private final Comparator<? super I> symComparator;

    CanonicalWordComparator(Comparator<? super I> symComparator) {
        this.symComparator = symComparator;
    }

    @Override
    public int compare(Word<I> o1, Word<I> o2) {
        int ldiff = o1.length() - o2.length();
        if (ldiff != 0) {
            return ldiff;
        }
        return CmpUtil.lexCompare(o1, o2, symComparator);
    }

}
