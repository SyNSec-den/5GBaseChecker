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

import java.io.File;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class AutomataLibSettingsTest {

    @BeforeSuite
    public static void setUp() {
        final URL resource = AutomataLibSettingsTest.class.getResource("/automatalib.properties");
        assert resource != null;
        final File properties = new File(resource.getFile());
        System.setProperty("automatalib.properties", properties.getAbsolutePath());
        System.setProperty(AutomataLibProperty.WORD_EMPTY_REP.getPropertyKey(), "OVERRIDDEN");
    }

    @Test
    public void testProperties() {
        AutomataLibSettings settings = AutomataLibSettings.getInstance();

        for (AutomataLibProperty p : AutomataLibProperty.values()) {
            switch (p) {
                case DOT_EXE_DIR:
                    Assert.assertEquals("dot", settings.getProperty(AutomataLibProperty.DOT_EXE_DIR));
                    break;
                case DOT_EXE_NAME:
                    Assert.assertEquals("dot.exe", settings.getProperty(AutomataLibProperty.DOT_EXE_NAME));
                    break;
                case LTSMIN_PATH:
                    Assert.assertEquals("ltsmin", settings.getProperty(AutomataLibProperty.LTSMIN_PATH));
                    break;
                case LTSMIN_VERBOSE:
                    Assert.assertEquals("false", settings.getProperty(AutomataLibProperty.LTSMIN_VERBOSE));
                    break;
                case VISUALIZATION_PROVIDER:
                    Assert.assertEquals("provider", settings.getProperty(AutomataLibProperty.VISUALIZATION_PROVIDER));
                    break;
                case WORD_DELIM_LEFT:
                    Assert.assertEquals("delim_left", settings.getProperty(AutomataLibProperty.WORD_DELIM_LEFT));
                    break;
                case WORD_DELIM_RIGHT:
                    Assert.assertEquals("delim_right", settings.getProperty(AutomataLibProperty.WORD_DELIM_RIGHT));
                    break;
                case WORD_EMPTY_REP:
                    Assert.assertEquals("OVERRIDDEN", settings.getProperty(AutomataLibProperty.WORD_EMPTY_REP));
                    break;
                case WORD_SYMBOL_DELIM_LEFT:
                    Assert.assertEquals("symbol_delim_left",
                                        settings.getProperty(AutomataLibProperty.WORD_SYMBOL_DELIM_LEFT));
                    break;
                case WORD_SYMBOL_DELIM_RIGHT:
                    Assert.assertEquals("symbol_delim_right",
                                        settings.getProperty(AutomataLibProperty.WORD_SYMBOL_DELIM_RIGHT));
                    break;
                case WORD_SYMBOL_SEPARATOR:
                    Assert.assertEquals("symbol_sep", settings.getProperty(AutomataLibProperty.WORD_SYMBOL_SEPARATOR));
                    break;
                default:
                    throw new IllegalStateException("Unhandled property " + p);

            }
        }

    }
}
