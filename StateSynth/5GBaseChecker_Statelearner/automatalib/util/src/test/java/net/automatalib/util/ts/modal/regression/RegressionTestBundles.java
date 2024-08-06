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

import java.util.ArrayList;
import java.util.List;

public final class RegressionTestBundles {

    public static final List<CompositionTest> COMPOSITION_TESTS;
    public static final List<DecompositionTest> DECOMPOSITION_TESTS;

    static {
        COMPOSITION_TESTS = new ArrayList<>();
        COMPOSITION_TESTS.add(new CompositionTest("/modal/integration/problem101-system-4.dot",
                                                  "/modal/integration/problem101-extended-context-5.dot",
                                                  "/modal/integration/problem101-merged-4.dot"));
        COMPOSITION_TESTS.add(new CompositionTest("/modal/integration/problem101-system-6.dot",
                                                  "/modal/integration/problem101-merged-1.dot",
                                                  "/modal/integration/problem101-merged-2.dot"));
        COMPOSITION_TESTS.add(new CompositionTest("/modal/integration/problem101-system-2.dot",
                                                  "/modal/integration/problem101-system-5.dot",
                                                  "/modal/integration/problem101-merged-3.dot"));
        COMPOSITION_TESTS.add(new CompositionTest("/modal/integration/problem101-extended-context-1.dot",
                                                  "/modal/integration/problem101-system-1.dot",
                                                  "/modal/integration/problem101-merged-5.dot"));
        COMPOSITION_TESTS.add(new CompositionTest("/modal/integration/problem101-system-3.dot",
                                                  "/modal/integration/problem101-extended-context-3.dot",
                                                  "/modal/integration/problem101-merged-1.dot"));

        DECOMPOSITION_TESTS = new ArrayList<>();
        DECOMPOSITION_TESTS.add(new DecompositionTest("/modal/integration/problem101-context-4.dot",
                                                      "/modal/integration/problem101-mc-4.dot",
                                                      "/modal/integration/3-phils-component21.dot",
                                                      "/modal/integration/problem101-system-4.dot"));
        DECOMPOSITION_TESTS.add(new DecompositionTest("/modal/integration/problem101-context-6.dot",
                                                      "/modal/integration/problem101-mc-6.dot",
                                                      "/modal/integration/3-phils-component01.dot",
                                                      "/modal/integration/problem101-system-6.dot"));
        DECOMPOSITION_TESTS.add(new DecompositionTest("/modal/integration/problem101-context-8.dot",
                                                      "/modal/integration/problem101-mc-8.dot",
                                                      "/modal/integration/problem101-extended-context-6.dot",
                                                      "/modal/integration/problem101-system-8.dot"));
        DECOMPOSITION_TESTS.add(new DecompositionTest("/modal/integration/problem101-context-2.dot",
                                                      "/modal/integration/problem101-mc-2.dot",
                                                      "/modal/integration/3-phils-component22.dot",
                                                      "/modal/integration/problem101-system-2.dot"));
        DECOMPOSITION_TESTS.add(new DecompositionTest("/modal/integration/problem101-context-7.dot",
                                                      "/modal/integration/problem101-mc-7.dot",
                                                      "/modal/integration/problem101-extended-context-2.dot",
                                                      "/modal/integration/problem101-system-7.dot"));
        DECOMPOSITION_TESTS.add(new DecompositionTest("/modal/integration/problem101-context-1.dot",
                                                      "/modal/integration/problem101-mc-1.dot",
                                                      "/modal/integration/3-phils-component02.dot",
                                                      "/modal/integration/problem101-system-1.dot"));
        DECOMPOSITION_TESTS.add(new DecompositionTest("/modal/integration/problem101-context-3.dot",
                                                      "/modal/integration/problem101-mc-3.dot",
                                                      "/modal/integration/3-phils-component12.dot",
                                                      "/modal/integration/problem101-system-3.dot"));
        DECOMPOSITION_TESTS.add(new DecompositionTest("/modal/integration/problem101-context-5.dot",
                                                      "/modal/integration/problem101-mc-5.dot",
                                                      "/modal/integration/3-phils-component11.dot",
                                                      "/modal/integration/problem101-system-5.dot"));
    }

    private RegressionTestBundles() {
        // prevent instantiation
    }
}
