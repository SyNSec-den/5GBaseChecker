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
package net.automatalib.commons.util.lib;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class LibLoaderTest {

    @BeforeClass
    public void setUp() {
        if (!PlatformProperties.OS_NAME.equals("linux") || !PlatformProperties.OS_ARCH.equals("x86_64")) {
            throw new SkipException("Native Libraries have currently only been built for 64 bit Linux systems");
        }
    }

    @Test
    public void testDefaultLibLoad() {
        LibLoader.getInstance().loadLibrary(LibLoaderTest.class, "automata_greeter");
        checkGreeter();
    }

    @Test
    public void testRepeatedLibLoad() {
        LibLoader.getInstance().loadLibrary(LibLoaderTest.class, "automata_greeter");
        checkGreeter();
    }

    @Test
    public void testPreferredSystemLibLoad() {
        LibLoader.getInstance().loadLibrary(LibLoaderTest.class, "automata_greeter.1", LoadPolicy.PREFER_SYSTEM);
        checkGreeter();
    }

    @Test(expectedExceptions = LoadLibraryException.class)
    public void testUnknownLibrary() {
        LibLoader.getInstance().loadLibrary(LibLoaderTest.class, "automata_greeter.2");
    }

    @Test(expectedExceptions = LoadLibraryException.class)
    public void testUnknownSystemLibrary() {
        LibLoader.getInstance().loadLibrary(LibLoaderTest.class, "automata_greeter.2", LoadPolicy.PREFER_SYSTEM);
    }

    private void checkGreeter() {
        final NativeGreeter nativeGreeter = new NativeGreeter();
        Assert.assertEquals("Hello John", nativeGreeter.greet("John"));
    }
}
