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
package net.automatalib.util.traversal;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Enum to use for coloring nodes/states during traversal.
 * <p>
 * Note that this enum only declares two values. The value {@link #WHITE} for unvisited nodes/states is identified with
 * {@code null}.
 *
 * @author Malte Isberner
 */
public enum Color {
    /**
     * Color for nodes/states that have been discovered, but not yet fully explored.
     */
    GRAY,
    /**
     * Color for nodes/states that have been fully explored.
     */
    BLACK;

    /**
     * Color for nodes/states that have not yet been discovered.
     */
    public static final @Nullable Color WHITE = null;
}
