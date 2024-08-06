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
package net.automatalib.commons.util.functions;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class provides utility methods for Java 8 {@link Function} objects (and for the corresponding primitive
 * specializations).
 *
 * @author Malte Isberner
 */
public final class FunctionsUtil {

    private FunctionsUtil() {
        // prevent instantiation
    }

    /**
     * Returns a default function if the argument is {@code null}. The default function's {@code Function#apply(Object)
     * apply} method will always return {@code null}. If a non-{@code null} function is passed to this method, it is
     * returned as-is.
     *
     * @param func
     *         the function reference (may be {@code null})
     *
     * @return a non-{@code null} object identical to the passed function, if it is non-{@code null}, or a function
     * object always returning {@code null} otherwise
     */
    public static <T, R> Function<T, ? extends @Nullable R> safeDefault(@Nullable Function<T, R> func) {
        if (func == null) {
            return (x) -> null;
        }
        return func;
    }

    /**
     * Returns a default function if the argument is {@code null}. The default function's {@link IntFunction#apply(int)
     * apply} method will always return {@code null}. If a non-{@code null} function is passed to this method, it is
     * returned as-is.
     *
     * @param func
     *         the function reference (may be {@code null})
     *
     * @return a non-{@code null} object identical to the passed function, if it is non-{@code null}, or a function
     * object always returning {@code null} otherwise
     */
    public static <R> IntFunction<? extends @Nullable R> safeDefault(@Nullable IntFunction<R> func) {
        if (func == null) {
            return (i) -> null;
        }
        return func;
    }

    /**
     * Returns a default function if the argument is {@code null}. The default function's {@link
     * BiIntFunction#apply(int, int) apply} method will always return {@code null}. If a non-{@code null} function is
     * passed to this method, it is returned as-is.
     *
     * @param func
     *         the function reference (may be {@code null})
     *
     * @return a non-{@code null} object identical to the passed function, if it is non-{@code null}, or a function
     * object always returning {@code null} otherwise
     */
    public static <R> BiIntFunction<? extends @Nullable R> safeDefault(@Nullable BiIntFunction<R> func) {
        if (func == null) {
            return (i1, i2) -> null;
        }
        return func;
    }

    public static <T> Predicate<T> safeToTrue(@Nullable Predicate<T> func) {
        if (func == null) {
            return (x) -> true;
        }
        return func;
    }

    public static <T> Predicate<T> safeToFalse(@Nullable Predicate<T> func) {
        if (func == null) {
            return (x) -> false;
        }
        return func;
    }

}
