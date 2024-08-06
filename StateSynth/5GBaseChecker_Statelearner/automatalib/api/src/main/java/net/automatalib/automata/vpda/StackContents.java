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
package net.automatalib.automata.vpda;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simplified stack implementation that allows to store integer values.
 *
 * @author Malte Isberner
 */
public class StackContents {

    private final int topElem;
    private final @Nullable StackContents rest;

    public StackContents(final int topElem) {
        this(topElem, null);
    }

    public StackContents(final int topElem, final @Nullable StackContents rest) {
        this.topElem = topElem;
        this.rest = rest;
    }

    public int peek() {
        return topElem;
    }

    public @Nullable StackContents pop() {
        return rest;
    }

    public StackContents push(final int elem) {
        return new StackContents(elem, this);
    }

    public static StackContents push(final int elem, final @Nullable StackContents rest) {
        return new StackContents(elem, rest);
    }
}
