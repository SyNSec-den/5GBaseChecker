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
package net.automatalib.modelcheckers.m3c.transformer;

import java.util.Collections;
import java.util.List;

import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.XDDManager;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVector;
import info.scce.addlib.serializer.DDProperty;
import info.scce.addlib.serializer.XDDSerializer;

/**
 * This class can be used to serialize and deserialize {@link ADDTransformer}s.
 *
 * @param <L>
 *         edge label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public class ADDTransformerSerializer<L, AP> implements TransformerSerializer<ADDTransformer<L, AP>, L, AP> {

    private final XDDManager<BooleanVector> xddManager;

    public ADDTransformerSerializer(XDDManager<BooleanVector> xddManager) {
        this.xddManager = xddManager;
    }

    @Override
    public List<String> serialize(ADDTransformer<L, AP> transformer) {
        if (transformer.isIdentity()) {
            return Collections.emptyList();
        }

        final XDDSerializer<BooleanVector> xddSerializer = new XDDSerializer<>();
        return Collections.singletonList(xddSerializer.serialize(transformer.getAdd()));
    }

    @Override
    public ADDTransformer<L, AP> deserialize(List<String> data) {
        if (data.isEmpty()) {
            return new ADDTransformer<>(xddManager);
        }

        final XDDSerializer<BooleanVector> xddSerializer = new XDDSerializer<>();
        final XDD<BooleanVector> transformer = xddSerializer.deserialize(xddManager, data.get(0), DDProperty.VARNAMEANDVARINDEX);
        return new ADDTransformer<>(xddManager, transformer);
    }
}
