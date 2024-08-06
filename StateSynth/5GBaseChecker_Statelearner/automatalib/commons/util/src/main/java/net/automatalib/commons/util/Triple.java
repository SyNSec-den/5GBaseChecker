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
package net.automatalib.commons.util;

import java.io.IOException;
import java.util.Objects;

import net.automatalib.commons.util.strings.AbstractPrintable;
import net.automatalib.commons.util.strings.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Immutable triple class.
 * <p>
 * <b>Note</b>: this class should only be used for internal representations of tuples with value type semantics (e.g.
 * equality, only if all components are equal).
 * <p>
 * Whenever a public interface returns an aggregation of individual objects, a separate class should be created/used
 * that has meaningful identifiers for the individual components.
 *
 * @param <T1>
 *         type of the triple's first component.
 * @param <T2>
 *         type of the triple's second component.
 * @param <T3>
 *         type of the triple's third component.
 *
 * @author Malte Isberner
 * @author frohme
 */
public final class Triple<T1, T2, T3> extends AbstractPrintable {

    /*
     * Components
     */
    private final T1 first;
    private final T2 second;
    private final T3 third;

    private Triple(T1 first, T2 second, T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Convenience function for creating a triple, allowing the user to omit the type parameters.
     */
    public static <T1, T2, T3> Triple<T1, T2, T3> of(T1 first, T2 second, T3 third) {
        return new Triple<>(first, second, third);
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    public T3 getThird() {
        return third;
    }

    @Override
    public void print(Appendable a) throws IOException {
        a.append('(');
        StringUtil.appendObject(a, first);
        a.append(", ");
        StringUtil.appendObject(a, second);
        a.append(", ");
        StringUtil.appendObject(a, third);
        a.append(')');
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Triple)) {
            return false;
        }

        final Triple<?, ?, ?> that = (Triple<?, ?, ?>) o;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second) &&
               Objects.equals(third, that.third);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(first);
        result = 31 * result + Objects.hashCode(second);
        result = 31 * result + Objects.hashCode(third);
        return result;
    }
}
