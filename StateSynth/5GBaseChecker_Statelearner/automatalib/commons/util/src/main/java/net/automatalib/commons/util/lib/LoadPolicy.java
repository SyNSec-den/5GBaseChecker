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

/**
 * Specifies in which order a library to be loaded is searched for.
 *
 * @author Malte Isberner
 */
public enum LoadPolicy {
    /**
     * First try to load a compatible version of the requested library shipped with the loading class. If that fails,
     * try loading the system-provided version of that library.
     */
    PREFER_SHIPPED,
    /**
     * First try to load the system version of the requested library. If that fails, try loading a compatible shipped
     * version.
     */
    PREFER_SYSTEM,
    /**
     * Only try to load a compatible version of the requested library shipped with the loading class.
     */
    SHIPPED_ONLY,
    /**
     * Only try to load the system version of the requested library.
     */
    SYSTEM_ONLY
}
