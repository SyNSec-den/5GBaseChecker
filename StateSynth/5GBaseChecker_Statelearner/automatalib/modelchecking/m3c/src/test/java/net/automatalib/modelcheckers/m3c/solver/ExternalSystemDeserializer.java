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
package net.automatalib.modelcheckers.m3c.solver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.collect.Maps;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.MutableProceduralModalProcessGraph;
import net.automatalib.graphs.base.DefaultCFMPS;
import net.automatalib.graphs.base.compact.CompactPMPG;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

final class ExternalSystemDeserializer {

    private ExternalSystemDeserializer() {
        // prevent instantiation
    }

    static <AP> ContextFreeModalProcessSystem<String, AP> parse(InputStream is)
            throws ParserConfigurationException, IOException, SAXException {

        final Element root = getRoot(is);
        final NodeList procedures = root.getElementsByTagName("pmpg");
        final Map<String, CompactPMPG<String, AP>> pmpgs =
                Maps.newLinkedHashMapWithExpectedSize(procedures.getLength());

        for (int i = 0; i < procedures.getLength(); i++) {
            final Element procedure = (Element) procedures.item(i);
            final String id = getFirstElementByTagName(procedure, "id").getTextContent();

            final Map<String, Integer> idToNode = new HashMap<>();
            final CompactPMPG<String, AP> pmpg = new CompactPMPG<>("");

            final Element statesElement = getFirstElementByTagName(procedure, "states");
            final NodeList states = statesElement.getElementsByTagName("state");
            for (int j = 0; j < states.getLength(); j++) {
                final Element state = (Element) states.item(j);
                addNode(pmpg, state, idToNode);
            }

            final Element transitionsElement = getFirstElementByTagName(procedure, "transitions");
            final NodeList transitions = transitionsElement.getElementsByTagName("transition");
            for (int j = 0; j < transitions.getLength(); j++) {
                final Element transition = (Element) transitions.item(j);
                addEdge(pmpg, transition, idToNode);
            }

            pmpgs.put(id, pmpg);
        }

        final String initialId = pmpgs.keySet().iterator().next();
        return new DefaultCFMPS<>(initialId, pmpgs);
    }

    private static Element getRoot(InputStream is) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(is);
        return document.getDocumentElement();
    }

    private static Element getFirstElementByTagName(Element elem, String tagName) {
        final Node node = elem.getElementsByTagName(tagName).item(0);
        assert node != null;
        return (Element) node;
    }

    private static boolean getBooleanAttributeByTagName(Element elem, String tagName) {
        final Element attributes = (Element) elem.getElementsByTagName("attributes").item(0);

        if (attributes == null) {
            return false;
        }

        final Node node = attributes.getElementsByTagName(tagName).item(0);
        return node != null && Boolean.parseBoolean(node.getTextContent());
    }

    private static <N> void addNode(MutableProceduralModalProcessGraph<N, String, ?, ?, ?> pmpg,
                                    Element state,
                                    Map<String, N> idToNode) {

        final String id = getFirstElementByTagName(state, "id").getTextContent();
        boolean isInitial = getBooleanAttributeByTagName(state, "isInitial");

        final N node = pmpg.addNode(Collections.emptySet());

        if (isInitial) {
            pmpg.setInitialNode(node);
        } else if ("end".equals(id)) {
            pmpg.setFinalNode(node);
        }

        idToNode.put(id, node);
    }

    private static <N, E> void addEdge(MutableProceduralModalProcessGraph<N, String, E, ?, ? extends MutableProceduralModalEdgeProperty> pmpg,
                                       Element transition,
                                       Map<String, N> idToNode) {
        final String sourceId = getFirstElementByTagName(transition, "sourceId").getTextContent();
        final String targetId = getFirstElementByTagName(transition, "targetId").getTextContent();
        final String label = getFirstElementByTagName(transition, "label").getTextContent();

        final boolean isMust = getBooleanAttributeByTagName(transition, "isMust");
        final boolean isProcedural = getBooleanAttributeByTagName(transition, "isProcedural");

        final E edge = pmpg.connect(idToNode.get(sourceId), idToNode.get(targetId));
        pmpg.getEdgeProperty(edge).setProceduralType(isProcedural ? ProceduralType.PROCESS : ProceduralType.INTERNAL);
        pmpg.getEdgeProperty(edge).setModalType(isMust ? ModalType.MUST : ModalType.MAY);
        pmpg.setEdgeLabel(edge, label);
    }

}
