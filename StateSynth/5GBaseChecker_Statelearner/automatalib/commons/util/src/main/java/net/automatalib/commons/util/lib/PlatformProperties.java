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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlatformProperties {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformProperties.class);
    private static final String PLATFORM_ALIASES = "platform-aliases.properties";

    public static final String OS_NAME;
    public static final String OS_ARCH;
    public static final String OS_VERSION;

    static {
        Properties aliases = new Properties();
        try (InputStream is = PlatformProperties.class.getResourceAsStream('/' + PLATFORM_ALIASES)) {
            if (is == null) {
                LOG.warn("Could not find '{}'.", PLATFORM_ALIASES);
            } else {
                aliases.load(is);
            }
        } catch (IOException ex) {
            LOG.warn("Could not load platform aliases file.", ex);
            LOG.warn("You may experience issues with the resolution of native libraries.");
        }

        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT).replace(' ', '_').replace('/', '_');
        OS_NAME = aliases.getProperty("os." + osName, osName);

        String osArch = System.getProperty("os.arch").toLowerCase(Locale.ROOT).replace(' ', '_').replace('/', '_');
        OS_ARCH = aliases.getProperty("arch." + osArch, osArch);

        OS_VERSION = System.getProperty("os.version");
    }

    private PlatformProperties() {
        // prevent instantiation
    }
}
