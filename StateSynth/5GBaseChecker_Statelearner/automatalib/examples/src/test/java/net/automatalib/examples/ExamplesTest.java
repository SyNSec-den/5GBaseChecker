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
package net.automatalib.examples;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import net.automatalib.commons.util.system.JVMUtil;
import net.automatalib.examples.ads.ADSExample;
import net.automatalib.examples.brics.SimpleBricsExample;
import net.automatalib.examples.dot.DOTExample;
import net.automatalib.examples.graph.DFSExample;
import net.automatalib.examples.incremental.IncrementalDFAExample;
import net.automatalib.examples.incremental.IncrementalMealyExample;
import net.automatalib.examples.incremental.IncrementalPCDFAExample;
import net.automatalib.examples.modelchecking.LTSminExample;
import net.automatalib.examples.modelchecking.LTSminMonitorExample;
import net.automatalib.examples.modelchecking.M3CSPAExample;
import net.automatalib.examples.spa.PalindromeExample;
import net.automatalib.examples.vpda.OneSEVPAExample;
import net.automatalib.modelcheckers.ltsmin.LTSminUtil;
import net.automatalib.modelcheckers.ltsmin.LTSminVersion;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Run the examples as part of (integration) testing.
 *
 * @author frohme
 */
public class ExamplesTest {

    @BeforeClass
    public void setupAutoClose() {
        // As soon as we observe an event that indicates a new window, close it to prevent blocking the tests.
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            final WindowEvent windowEvent = (WindowEvent) event;
            final Window w = windowEvent.getWindow();
            w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
        }, AWTEvent.WINDOW_FOCUS_EVENT_MASK);
    }

    @Test
    public void testADSExample() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> ADSExample.main(new String[0]));
    }

    @Test
    public void testSimpleBricsExample() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> SimpleBricsExample.main(new String[0]));
    }

    @Test
    public void testDOTExample() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> {
            try {
                DOTExample.main(new String[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testDFSExample() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> DFSExample.main(new String[0]));
    }

    @Test
    public void testIncrementalDFAExample() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> IncrementalDFAExample.main(new String[0]));
    }

    @Test
    public void testIncrementalMealyExample() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> IncrementalMealyExample.main(new String[0]));
    }

    @Test
    public void testIncrementalPCDFAExample() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> IncrementalPCDFAExample.main(new String[0]));
    }

    @Test
    public void testLTSminExample() {
        if (!LTSminUtil.supports(LTSminVersion.of(3, 0, 0))) {
            throw new SkipException("LTSmin is not installed in the proper version");
        }

        LTSminExample.main(new String[0]);
    }

    @Test
    public void testLTSminMonitorExample() {
        if (!LTSminUtil.supports(LTSminVersion.of(3, 1, 0))) {
            throw new SkipException("LTSmin is not installed in the proper version");
        }

        LTSminMonitorExample.main(new String[0]);
    }

    @Test
    public void testM3CSPAExample() throws InterruptedException, InvocationTargetException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> {
            try {
                M3CSPAExample.main(new String[0]);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testSPAPalindromeExample() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> PalindromeExample.main(new String[0]));
    }

    @Test
    public void testOneSEVPAExample() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> OneSEVPAExample.main(new String[0]));
    }

    private static void checkJVMCompatibility() {
        final int canonicalSpecVersion = JVMUtil.getCanonicalSpecVersion();
        if (!(canonicalSpecVersion <= 8 || canonicalSpecVersion == 11)) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <=8");
        }
    }

}
