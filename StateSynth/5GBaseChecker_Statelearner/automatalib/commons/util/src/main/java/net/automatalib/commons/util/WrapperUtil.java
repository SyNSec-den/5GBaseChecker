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

import org.checkerframework.checker.nullness.qual.Nullable;

public final class WrapperUtil {

    private WrapperUtil() {
        // prevent instantiation
    }

    public static boolean booleanValue(@Nullable Boolean b) {
        return booleanValue(b, false);
    }

    public static boolean booleanValue(@Nullable Boolean b, boolean def) {
        return (b != null) ? b : def;
    }

    @SuppressWarnings("PMD.AvoidUsingShortType") // we don't perform arithmetic operations on Shorts, so usage is fine
    public static short shortValue(@Nullable Short s, short def) {
        return (s != null) ? s : def;
    }

    public static int intValue(@Nullable Integer i) {
        return intValue(i, 0);
    }

    public static int intValue(@Nullable Integer i, int def) {
        return (i != null) ? i : def;
    }

    public static long longValue(@Nullable Long l) {
        return longValue(l, 0L);
    }

    public static long longValue(@Nullable Long l, long def) {
        return (l != null) ? l : def;
    }

    public static float floatValue(@Nullable Float f) {
        return floatValue(f, 0.0f);
    }

    public static float floatValue(@Nullable Float f, float def) {
        return (f != null) ? f : def;
    }

    public static double doubleValue(@Nullable Double d) {
        return doubleValue(d, 0.0);
    }

    public static double doubleValue(@Nullable Double d, double def) {
        return (d != null) ? d : def;
    }

}
