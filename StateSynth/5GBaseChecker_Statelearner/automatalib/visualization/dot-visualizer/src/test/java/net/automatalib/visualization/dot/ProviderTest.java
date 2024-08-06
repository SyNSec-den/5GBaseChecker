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
package net.automatalib.visualization.dot;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import javax.swing.SwingUtilities;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.system.JVMUtil;
import net.automatalib.visualization.VPManager;
import net.automatalib.visualization.Visualization;
import net.automatalib.visualization.VisualizationProvider;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class ProviderTest {

    @BeforeClass
    public void checkDOT() {
        if (!DOT.checkUsable()) {
            // Do not fail on platforms, where DOT is not installed
            throw new SkipException("DOT is not installed");
        }
    }

    @Test
    public void testProviderConfiguration() {
        final VPManager vpManager = new VPManager();

        vpManager.load();

        final VisualizationProvider swingProvider = vpManager.getProviderById(GraphVizSwingVisualizationProvider.ID);
        final VisualizationProvider browserProvider =
                vpManager.getProviderById(GraphVizBrowserVisualizationProvider.ID);

        Assert.assertTrue(swingProvider instanceof GraphVizSwingVisualizationProvider);
        Assert.assertTrue(browserProvider instanceof GraphVizBrowserVisualizationProvider);
    }

    // Headless GUI testing is a pain. Therefore just check that we don't throw any exceptions for now.
    @Test(dependsOnMethods = "testProviderConfiguration", timeOut = 10000)
    public void testDisplay() throws InterruptedException, InvocationTargetException {

        final int canonicalSpecVersion = JVMUtil.getCanonicalSpecVersion();
        if (!(canonicalSpecVersion <= 8 || canonicalSpecVersion == 11)) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <=8");
        }

        final CompactDFA<Integer> dfa = TestUtil.generateRandomAutomaton(new Random(42));

        // invokeAndWait so that TestNG doesn't kill our GUI thread that we want to check.
        SwingUtilities.invokeAndWait(() -> Visualization.visualize(dfa, false));
    }

}
