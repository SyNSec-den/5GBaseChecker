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
package net.automatalib.commons.util.system;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * Utility class for Java/JVM specific utilities.
 *
 * @author frohme
 */
public final class JVMUtil {

    private JVMUtil() {
        // prevent instantiation
    }

    /**
     * Returns the canonized number of the currently available Java language specification version.
     * <p>
     * The numbering scheme of the specification versions is structured as follows:
     * <ul>
     * <li>Java SE 7: 1.7</li>
     * <li>Java SE 8: 1.8</li>
     * <li>Java SE 9: 9</li>
     * <li>Java SE 10: 10</li>
     * </ul>
     * This method returns:
     * <ul>
     * <li>Java SE 7: 7</li>
     * <li>Java SE 8: 8</li>
     * <li>Java SE 9: 9</li>
     * <li>Java SE 10: 10</li>
     * </ul>
     *
     * @return the canonized java specification version
     *
     * @see RuntimeMXBean#getSpecVersion()
     */
    public static int getCanonicalSpecVersion() {
        final String version = ManagementFactory.getRuntimeMXBean().getSpecVersion();
        final String[] split = version.split("\\.");

        return split.length > 1 ? Integer.parseInt(split[1]) : Integer.parseInt(split[0]);
    }
}
