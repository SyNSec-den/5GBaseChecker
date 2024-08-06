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
package net.automatalib;

import java.util.Properties;

import net.automatalib.commons.util.settings.SettingsSource;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author Malte Isberner
 */
public final class AutomataLibSettings {

    private static final AutomataLibSettings INSTANCE = new AutomataLibSettings();
    private final Properties properties;

    private AutomataLibSettings() {
        properties = SettingsSource.readSettings(AutomataLibSettingsSource.class);
    }

    public static AutomataLibSettings getInstance() {
        return INSTANCE;
    }

    public @Nullable String getProperty(AutomataLibProperty property) {
        return properties.getProperty(property.getPropertyKey());
    }

    public String getProperty(AutomataLibProperty property, String defaultValue) {
        return properties.getProperty(property.getPropertyKey(), defaultValue);
    }
}
