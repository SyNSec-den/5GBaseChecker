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
package net.automatalib.util.ts.modal.regression;

import java.io.IOException;

import net.automatalib.ts.modal.CompactMC;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.util.ts.modal.TestUtils;

public class DecompositionInstance {

    public final CompactMTS<String> system;
    public final CompactMTS<String> context;
    public final CompactMTS<String> origSys;
    public final CompactMC<String> modalContract;

    public DecompositionInstance(DecompositionTest decompositionTest) throws IOException {
        system = TestUtils.loadMTSFromPath(decompositionTest.system);
        context = TestUtils.loadMTSFromPath(decompositionTest.context);
        origSys = TestUtils.loadMTSFromPath(decompositionTest.origSys);
        modalContract = TestUtils.loadMCFromPath(decompositionTest.modalContract);
    }
}
