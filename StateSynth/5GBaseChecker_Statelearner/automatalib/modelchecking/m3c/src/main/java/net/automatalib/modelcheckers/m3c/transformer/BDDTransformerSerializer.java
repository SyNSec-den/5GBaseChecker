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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanLogicDDManager;
import info.scce.addlib.serializer.DDProperty;
import info.scce.addlib.serializer.XDDSerializer;

/**
 * This class can be used to serialize and deserialize {@link BDDTransformer}s.
 *
 * @param <L>
 *         edge label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public class BDDTransformerSerializer<L, AP> implements TransformerSerializer<BDDTransformer<L, AP>, L, AP> {

    private final BDDManager bddManager;

    public BDDTransformerSerializer(BDDManager bddManager) {
        this.bddManager = bddManager;
    }

    @Override
    public List<String> serialize(BDDTransformer<L, AP> transformer) {
        final XDDSerializer<Boolean> xddSerializer = new XDDSerializer<>();
        final List<String> serializedBDDs = new ArrayList<>();
        final BooleanLogicDDManager ddManager = new BooleanLogicDDManager();

        for (int i = 0; i < transformer.getNumberOfVars(); i++) {
            final XDD<Boolean> bddAsXDD = transformer.getBDD(i).toXDD(ddManager);
            serializedBDDs.add(xddSerializer.serialize(bddAsXDD));
        }

        ddManager.quit();
        return serializedBDDs;
    }

    @Override
    public BDDTransformer<L, AP> deserialize(List<String> data) {
        final BooleanLogicDDManager ddManager = new BooleanLogicDDManager();
        final XDDSerializer<Boolean> serializer = new XDDSerializer<>();
        final List<XDD<Boolean>> xdds = new ArrayList<>();

        for (String serializedDD : data) {
            xdds.add(serializer.deserialize(ddManager, serializedDD, DDProperty.VARNAMEANDVARINDEX));
        }

        final BDD[] bdds = new BDD[xdds.size()];

        for (int i = 0; i < bdds.length; i++) {
            bdds[i] = toBDD(xdds.get(i), bddManager, new HashMap<>());
        }

        ddManager.quit();
        return new BDDTransformer<>(bddManager, bdds);
    }

    private BDD toBDD(XDD<Boolean> xdd, BDDManager bddManager, Map<XDD<Boolean>, BDD> xdd2bdd) {
        BDD bdd = xdd2bdd.get(xdd);
        if (bdd == null) {
            if (xdd.isConstant()) {
                if (xdd.v()) {
                    bdd = bddManager.readOne();
                } else {
                    bdd = bddManager.readLogicZero();
                }
            } else {
                final BDD falseSucc = toBDD(xdd.e(), bddManager, xdd2bdd);
                final BDD trueSucc = toBDD(xdd.t(), bddManager, xdd2bdd);
                final BDD ithBDD = bddManager.ithVar(bddManager.varIdx(xdd.readName()));
                bdd = ithBDD.ite(trueSucc, falseSucc);
            }
            xdd2bdd.put(xdd, bdd);
        }

        return bdd;
    }
}
