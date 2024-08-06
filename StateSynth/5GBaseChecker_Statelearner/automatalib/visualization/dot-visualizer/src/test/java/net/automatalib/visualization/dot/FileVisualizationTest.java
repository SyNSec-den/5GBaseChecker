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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.VisualizationHelper;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A small test for checking, if writing directly to files is working.
 *
 * @author frohme
 */
public class FileVisualizationTest {

    @BeforeClass
    public void checkDOT() {
        if (!DOT.checkUsable()) {
            // Do not fail on platforms, where DOT is not installed
            throw new SkipException("DOT is not installed");
        }
    }

    @Test
    public void testFileVisualization() throws IOException {
        final File dotFile = File.createTempFile("automaton", ".dot");
        final File dotOutputFile = File.createTempFile("automaton-as-png", ".png");

        dotFile.deleteOnExit();
        dotOutputFile.deleteOnExit();

        final CompactDFA<Integer> dfa = TestUtil.generateRandomAutomaton(new Random(42));

        // make sure our DOT process generates output on STD_ERR
        GraphDOT.write(dfa,
                       dfa.getInputAlphabet(),
                       IOUtil.asBufferedUTF8Writer(dotFile),
                       new InvalidVisualizationHelper<>());
        DOT.runDOT(dotFile, "png", dotOutputFile);

        Assert.assertTrue(dotOutputFile.exists());
        Assert.assertTrue(dotOutputFile.length() > 0);
    }

    private static final class InvalidVisualizationHelper<N, E> implements VisualizationHelper<N, E> {

        private final Random random = new Random(123);

        @Override
        public boolean getNodeProperties(N node, Map<String, String> properties) {
            properties.put(NodeAttrs.SHAPE, "thisisnotavalidshape" + random.nextInt());
            return true;
        }

        @Override
        public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
            return true;
        }
    }

}
