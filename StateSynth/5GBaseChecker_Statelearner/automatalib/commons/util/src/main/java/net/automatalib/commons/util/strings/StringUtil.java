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
package net.automatalib.commons.util.strings;

import java.io.IOException;
import java.util.regex.Pattern;

import com.google.common.collect.Iterators;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Miscellaneous utility functions for {@link String}s.
 *
 * @author Malte Isberner
 */
public final class StringUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

    private StringUtil() {
        // prevent instantiation
    }

    public static String enquote(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        try {
            enquote(s, sb);
        } catch (IOException e) {
            LOGGER.error("Could not enquote String", e);
        }
        return sb.toString();
    }

    public static void enquote(String s, Appendable a) throws IOException {
        a.append('"');
        escapeQuotes(s, a);
        a.append('"');
    }

    public static Pattern getIdentifierPattern() {
        return LazyPatternHolder.INSTANCE;
    }

    public static String enquoteIfNecessary(String s) {
        StringBuilder sb = new StringBuilder();
        try {
            enquoteIfNecessary(s, sb);
            return sb.toString();
        } catch (IOException ex) {
            throw new AssertionError("StringBuilder should not throw", ex);
        }
    }

    public static void enquoteIfNecessary(String s, Appendable a) throws IOException {
        enquoteIfNecessary(s, a, getIdentifierPattern());
    }

    public static String enquoteIfNecessary(String s, Pattern p) {
        StringBuilder sb = new StringBuilder();
        try {
            enquoteIfNecessary(s, sb, p);
            return sb.toString();
        } catch (IOException ex) {
            throw new AssertionError("StringBuilder should not throw", ex);
        }
    }

    public static void enquoteIfNecessary(String s, Appendable a, Pattern valid) throws IOException {
        if (!valid.matcher(s).matches()) {
            enquote(s, a);
        } else {
            a.append(s);
        }
    }

    public static void enquoteIfNecessary(String s, Appendable a, Pattern valid, Pattern exception) throws IOException {
        if (!valid.matcher(s).matches() || exception.matcher(s).matches()) {
            enquote(s, a);
        } else {
            a.append(s);
        }
    }

    public static String unquote(String s) {
        if (s.length() < 2) {
            throw new IllegalArgumentException(
                    "Argument to StringUtil.unquote() must begin and end with a double quote ('\"').");
        }

        StringBuilder sb = new StringBuilder(s.length() - 2);
        try {
            unquote(s, sb);
        } catch (IOException e) {
            LOGGER.error("Could not unquote String", e);
        }
        return sb.toString();
    }

    public static void unquote(String s, Appendable a) throws IOException {
        if (s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"') {
            throw new IllegalArgumentException(
                    "Argument to StringUtil.unquote() must begin and end with a double quote ('\"').");
        }
        unescapeQuotes(s.substring(1, s.length() - 1), a);
    }

    /**
     * Unescapes escaped double quotes in a string, i.e. replaces <code>\"</code> by <code>"</code> and <code>\\</code>
     * by <code>\</code>.
     *
     * @param s
     *         the string in which to unescape double quotes.
     *
     * @return the unescaped string.
     */
    public static String unescapeQuotes(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        try {
            unescapeQuotes(s, sb);
        } catch (IOException e) {
            LOGGER.error("Could not unescape quotes", e);
        }
        return sb.toString();
    }

    public static void unescapeQuotes(String s, Appendable a) throws IOException {
        if (s.isEmpty()) {
            return;
        }

        int idx = 0;
        final int eos = s.length() - 1;

        for (; idx < eos; idx++) {
            char c = s.charAt(idx);
            if (c == '\\') {
                c = s.charAt(++idx);
                if (c != '"' && c != '\\') {
                    a.append('\\');
                }
            }
            a.append(c);
        }

        // only append last symbol, if it wasn't forwarded in the loop
        if (idx < s.length()) {
            a.append(s.charAt(eos));
        }
    }

    /**
     * Escapes double quotes in a string. Effectively, <code>"</code> is replaced by <code>\"</code> and <code>\</code>
     * is replaced by <code>\\</code>.
     *
     * @param s
     *         the string to escape.
     *
     * @return the escaped string.
     */
    public static String escapeQuotes(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        try {
            escapeQuotes(s, sb);
        } catch (IOException e) {
            LOGGER.error("Could not escape quotes", e);
        }
        return sb.toString();
    }

    public static void escapeQuotes(String s, Appendable a) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\\' || c == '"') {
                a.append('\\');
            }
            a.append(c);
        }
    }

    public static void appendArray(Appendable a, Object[] array, String sepString) throws IOException {
        appendIterable(a, () -> Iterators.forArray(array), sepString);
    }

    public static void appendArrayEnquoted(Appendable a, Object[] array, String sepString) throws IOException {
        appendIterableEnquoted(a, () -> Iterators.forArray(array), sepString);
    }

    public static void appendIterable(Appendable a, Iterable<?> it, String sepString) throws IOException {
        boolean first = true;

        for (Object o : it) {
            if (first) {
                first = false;
            } else {
                a.append(sepString);
            }
            appendObject(a, o);
        }
    }

    public static void appendIterableEnquoted(Appendable a, Iterable<?> it, String sepString) throws IOException {
        boolean first = true;

        for (Object o : it) {
            if (first) {
                first = false;
            } else {
                a.append(sepString);
            }
            enquote(String.valueOf(o), a);
        }
    }

    public static void appendObject(Appendable a, @Nullable Object obj) throws IOException {
        if (obj instanceof Printable) {
            ((Printable) obj).print(a);
        } else {
            a.append(String.valueOf(obj));
        }
    }

    /**
     * Lazy holder for identifier pattern. See
     * <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">
     * Initialization-on-demand holder idiom</a>
     */
    private static class LazyPatternHolder {

        private static final Pattern INSTANCE = Pattern.compile("[a-zA-Z_]*\\w*");
    }
}
