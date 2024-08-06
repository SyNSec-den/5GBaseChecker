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
package net.automatalib.commons.util.mappings;

/**
 * Mapping realized by the composition of two mappings.
 *
 * @param <D>
 *         domain class of the first (and final) mapping.
 * @param <I>
 *         intermediate object class, i.e. range of the first and domain of the second.
 * @param <R>
 *         range class of the second (and final) mapping.
 *
 * @author Malte Isberner
 */
final class MappingComposition<D, I, R> implements Mapping<D, R> {

    private final Mapping<D, ? extends I> first;
    private final Mapping<? super I, R> second;

    /**
     * Constructor.
     *
     * @param first
     *         first mapping (defines domain).
     * @param second
     *         second mapping (defines range).
     */
    MappingComposition(Mapping<D, ? extends I> first, Mapping<? super I, R> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public R get(D elem) {
        I i = first.get(elem);
        return second.get(i);
    }

}
