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
package net.automatalib.brics;

import dk.brics.automaton.Transition;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The properties of an edge in a Brics automaton.
 *
 * @author Malte Isberner
 */
public class BricsTransitionProperty {

    private final char min;
    private final char max;

    /**
     * Constructor. Constructs the property from a Brics {@link Transition}.
     *
     * @param trans
     *         the Brics transition object
     */
    public BricsTransitionProperty(Transition trans) {
        this(trans.getMin(), trans.getMax());
    }

    /**
     * Constructor.
     *
     * @param min
     *         lower bound of the character range.
     * @param max
     *         upper bound of the character range.
     */
    public BricsTransitionProperty(char min, char max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Retrieves the lower bound of the character range.
     *
     * @return the lower bound of the character range
     *
     * @see Transition#getMin()
     */
    public char getMin() {
        return min;
    }

    /**
     * Retrieves the upper bound of the character range.
     *
     * @return the upper bound of the character range
     *
     * @see Transition#getMax()
     */
    public char getMax() {
        return max;
    }

    @Override
    public String toString() {
        return toString(min, max);
    }

    public static String toString(char min, char max) {
        StringBuilder sb = new StringBuilder();
        sb.append('\'').append(min).append('\'');
        if (max > min) {
            sb.append("..'").append(max).append('\'');
        }
        return sb.toString();
    }

    @Override
    public final boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BricsTransitionProperty)) {
            return false;
        }

        final BricsTransitionProperty that = (BricsTransitionProperty) o;
        return min == that.min && max == that.max;
    }

    @Override
    public final int hashCode() {
        int result = 1;
        result = 31 * result + Character.hashCode(min);
        result = 31 * result + Character.hashCode(max);
        return result;
    }
}
