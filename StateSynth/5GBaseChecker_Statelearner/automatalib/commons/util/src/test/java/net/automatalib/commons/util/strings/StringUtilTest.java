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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class StringUtilTest {

    @Test
    public void testEnquote() {
        Assert.assertEquals(StringUtil.enquote(""), "\"\"");
        Assert.assertEquals(StringUtil.enquote("abc"), "\"abc\"");
        Assert.assertEquals(StringUtil.enquote("\"abc\""), "\"\\\"abc\\\"\"");
        Assert.assertEquals(StringUtil.enquote("ab\"c"), "\"ab\\\"c\"");
        Assert.assertEquals(StringUtil.enquote("ab\\\"c"), "\"ab\\\\\\\"c\"");
    }

    @Test
    public void testEnquoteIfNecessary() {
        Assert.assertEquals(StringUtil.enquoteIfNecessary(""), "");
        Assert.assertEquals(StringUtil.enquoteIfNecessary("abc"), "abc");
        Assert.assertEquals(StringUtil.enquoteIfNecessary("\"abc\""), "\"\\\"abc\\\"\"");
        Assert.assertEquals(StringUtil.enquoteIfNecessary("ab\"c"), "\"ab\\\"c\"");
        Assert.assertEquals(StringUtil.enquoteIfNecessary("ab\\\"c"), "\"ab\\\\\\\"c\"");
    }

    @Test
    public void testUnquote() {
        Assert.expectThrows(IllegalArgumentException.class, () -> StringUtil.unquote(""));
        Assert.expectThrows(IllegalArgumentException.class, () -> StringUtil.unquote("abc"));
        Assert.assertEquals(StringUtil.unquote("\"abc\""), "abc");
        Assert.assertEquals(StringUtil.unquote("\"ab\"c\""), "ab\"c");
        Assert.assertEquals(StringUtil.unquote("\"ab\\\"c\""), "ab\"c");
        Assert.assertEquals(StringUtil.unquote("\"ab\\\\\"c\""), "ab\\\"c");
    }

    @Test
    public void testEscapeQuotes() {
        Assert.assertEquals(StringUtil.escapeQuotes(""), "");
        Assert.assertEquals(StringUtil.escapeQuotes("abc"), "abc");
        Assert.assertEquals(StringUtil.escapeQuotes("\"abc\""), "\\\"abc\\\"");
        Assert.assertEquals(StringUtil.escapeQuotes("ab\"c"), "ab\\\"c");
        Assert.assertEquals(StringUtil.escapeQuotes("ab\\\"c"), "ab\\\\\\\"c");
        Assert.assertEquals(StringUtil.escapeQuotes("ab\\\\\"c"), "ab\\\\\\\\\\\"c");
    }

    @Test
    public void testUnescapeQuotes() {
        Assert.assertEquals(StringUtil.unescapeQuotes(""), "");
        Assert.assertEquals(StringUtil.unescapeQuotes("abc"), "abc");
        Assert.assertEquals(StringUtil.unescapeQuotes("\"abc\""), "\"abc\"");
        Assert.assertEquals(StringUtil.unescapeQuotes("\\\"abc\\\""), "\"abc\"");
        Assert.assertEquals(StringUtil.unescapeQuotes("ab\"c"), "ab\"c");
        Assert.assertEquals(StringUtil.unescapeQuotes("ab\\\"c"), "ab\"c");
        Assert.assertEquals(StringUtil.unescapeQuotes("ab\\\\\"c"), "ab\\\"c");
        Assert.assertEquals(StringUtil.unescapeQuotes("ab\\\\c"), "ab\\c");
    }

    @Test
    public void testInverseness() {
        Assert.assertEquals(StringUtil.unescapeQuotes(StringUtil.escapeQuotes("")), "");
        Assert.assertEquals(StringUtil.unescapeQuotes(StringUtil.escapeQuotes("abc")), "abc");
        Assert.assertEquals(StringUtil.unescapeQuotes(StringUtil.escapeQuotes("\"abc\"")), "\"abc\"");
        Assert.assertEquals(StringUtil.unescapeQuotes(StringUtil.escapeQuotes("ab\"c")), "ab\"c");
        Assert.assertEquals(StringUtil.unescapeQuotes(StringUtil.escapeQuotes("ab\\\"c")), "ab\\\"c");

        Assert.assertEquals(StringUtil.unquote(StringUtil.enquote("")), "");
        Assert.assertEquals(StringUtil.unquote(StringUtil.enquote("abc")), "abc");
        Assert.assertEquals(StringUtil.unquote(StringUtil.enquote("\"abc\"")), "\"abc\"");
        Assert.assertEquals(StringUtil.unquote(StringUtil.enquote("ab\"c")), "ab\"c");
        Assert.assertEquals(StringUtil.unquote(StringUtil.enquote("ab\\\"c")), "ab\\\"c");
    }

    @Test
    public void testAppend() throws Exception {
        final StringBuilder sb1 = new StringBuilder();
        final StringBuilder sb2 = new StringBuilder();
        final Object[] data = new Object[] {1, 2, 3};

        StringUtil.appendArray(sb1, data, ",");
        StringUtil.appendArrayEnquoted(sb2, data, ",");

        Assert.assertEquals(sb1.toString(), "1,2,3");
        Assert.assertEquals(sb2.toString(), "\"1\",\"2\",\"3\"");
    }
}
