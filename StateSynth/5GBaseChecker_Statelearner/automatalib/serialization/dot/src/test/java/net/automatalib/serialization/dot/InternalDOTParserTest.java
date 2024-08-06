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
package net.automatalib.serialization.dot;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.automatalib.commons.util.IOUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class InternalDOTParserTest {

    private List<Node> nodes;
    private List<Edge> edges;

    private int nodesChecked;

    @BeforeMethod
    public void setUp() throws IOException {
        try (InputStream is = InternalDOTParserTest.class.getResourceAsStream(DOTSerializationUtil.PARSER_RESOURCE);
             Reader r = IOUtil.asUncompressedBufferedNonClosingUTF8Reader(is)) {

            final InternalDOTParser parser = new InternalDOTParser(r);
            parser.parse();

            this.nodes = parser.getNodes();
            this.edges = new LinkedList<>(parser.getEdges());
        }
    }

    @Test
    public void checkNodes() {
        checkNodeProperties("n1", "label", "Node 1", "color", "blue");
        checkNodeProperties("n2", "label", "Node 2", "style", "dashed", "color", "red");
        checkNodeProperties("", "label", "empty", "style", "dashed");
        checkNodeProperties("n3", "style", "dashed");
        checkNodeProperties("n4", "label", "Sub 1", "style", "dashed");
        checkNodeProperties("n5", "label", "Sub 2", "style", "dashed");
        checkNodeProperties("n6", "style", "dashed");
        checkNodeProperties("n7", "style", "dashed");

        checkNodeProperties("n10", "style", "dashed", "shape", "octagon");
        checkNodeProperties("n11", "style", "dashed", "shape", "octagon");
        checkNodeProperties("n12", "style", "dashed", "shape", "octagon");
        checkNodeProperties("n13", "style", "dashed", "shape", "octagon");
        checkNodeProperties("n14", "style", "dashed", "shape", "octagon");
        checkNodeProperties("n15", "style", "dashed", "shape", "octagon");

        Assert.assertEquals(nodesChecked, 14);
    }

    @Test
    public void checkEdges() {
        Assert.assertEquals(edges.size(), 27);

        checkEdgeProperties("n1", "n2", "label", "Input 1", "color", "green");
        checkEdgeProperties("n1", "n2", "label", "Input 2", "color", "red", "style", "solid");
        checkEdgeProperties("n1", "n3", "label", "arg", "color", "green", "style", "dashed");
        checkEdgeProperties("n3", "n2", "label", "arg", "color", "green", "style", "dashed");

        // subgraph edges
        checkEdgeProperties("n3", "n4", "color", "green", "style", "dashed");
        checkEdgeProperties("n3", "n5", "color", "green", "style", "dashed");

        checkEdgeProperties("n2", "n5", "color", "green", "style", "dashed");

        checkEdgeProperties("n6", "n1", "color", "green", "style", "dashed");
        checkEdgeProperties("n7", "n1", "color", "green", "style", "dashed");

        // first set of nested transitions
        checkEdgeProperties("n10", "n12", "color", "red", "style", "dashed");
        checkEdgeProperties("n10", "n13", "color", "red", "style", "dashed");
        checkEdgeProperties("n10", "n14", "color", "red", "style", "dashed");
        checkEdgeProperties("n10", "n15", "color", "red", "style", "dashed");
        checkEdgeProperties("n11", "n12", "color", "red", "style", "dashed");
        checkEdgeProperties("n11", "n13", "color", "red", "style", "dashed");
        checkEdgeProperties("n11", "n14", "color", "red", "style", "dashed");
        checkEdgeProperties("n11", "n15", "color", "red", "style", "dashed");
        checkEdgeProperties("n13", "n14", "color", "blue", "style", "dashed");
        checkEdgeProperties("n13", "n15", "color", "blue", "style", "dashed");

        // second set of nested transitions
        checkEdgeProperties("n10", "n12", "color", "green", "style", "dashed");
        checkEdgeProperties("n10", "n13", "color", "green", "style", "dashed");
        checkEdgeProperties("n11", "n12", "color", "green", "style", "dashed");
        checkEdgeProperties("n11", "n13", "color", "green", "style", "dashed");
        checkEdgeProperties("n12", "n14", "color", "green", "style", "dashed");
        checkEdgeProperties("n12", "n15", "color", "green", "style", "dashed");
        checkEdgeProperties("n13", "n14", "color", "green", "style", "dashed");
        checkEdgeProperties("n13", "n15", "color", "green", "style", "dashed");

        Assert.assertEquals(edges.size(), 0);
    }

    private void checkNodeProperties(String id, String... props) {

        assert props.length % 2 == 0;

        Node node = null;
        for (Node n : nodes) {
            if (id.equals(n.id)) {
                node = n;
                break;
            }
        }

        Assert.assertNotNull(node);
        Map<String, String> attributes = node.attributes;

        // Assure that we check all properties
        Assert.assertEquals(attributes.size(), props.length / 2);

        for (int i = 0; i < props.length; i += 2) {
            String key = props[i];
            String value = props[i + 1];

            Assert.assertTrue(attributes.containsKey(key));
            Assert.assertEquals(attributes.get(key), value);
        }

        nodesChecked++;
    }

    private void checkEdgeProperties(String src, String tgt, String... props) {

        assert props.length % 2 == 0;

        Iterator<Edge> iter = this.edges.iterator();

        edge:
        while (iter.hasNext()) {
            Edge edge = iter.next();

            // potential match
            if (src.equals(edge.src) && tgt.equals(edge.tgt) && edge.attributes.size() == props.length / 2) {
                for (int i = 0; i < props.length; i += 2) {
                    String key = props[i];
                    String value = props[i + 1];

                    if (!edge.attributes.containsKey(key) || !value.equals(edge.attributes.get(key))) {
                        // not a match, continue
                        continue edge;
                    }
                }

                // mark edge as checked by removing it
                iter.remove();
                return;
            }
        }
        Assert.fail("Expected edge not found");
    }
}
