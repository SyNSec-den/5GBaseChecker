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
package net.automatalib.commons.util.settings;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import net.automatalib.commons.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClassPathFileSource implements SettingsSource {

    private final String fileName;

    protected AbstractClassPathFileSource(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void loadSettings(Properties properties) {
        Logger log = LoggerFactory.getLogger(getClass());

        try {
            final Enumeration<URL> resourceUrls;
            final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl == null) {
                final ClassLoader scl = ClassLoader.getSystemClassLoader();
                if (scl == null) {
                    log.error("Couldn't find classloader, aborting reading '{}'", fileName);
                    return;
                }
                resourceUrls = scl.getResources(fileName);
            } else {
                resourceUrls = ccl.getResources(fileName);
            }

            while (resourceUrls.hasMoreElements()) {
                URL url = resourceUrls.nextElement();
                try (Reader r = IOUtil.asBufferedUTF8Reader(url.openStream())) {
                    properties.load(r);
                } catch (IOException ex) {
                    log.error("Could not read property file " + url + ".", ex);
                }
            }
        } catch (IOException ex) {
            log.error("Could not enumerate " + fileName + " files,", ex);
        }
    }

}
