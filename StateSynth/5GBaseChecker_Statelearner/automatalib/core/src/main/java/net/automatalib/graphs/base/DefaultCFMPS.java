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
package net.automatalib.graphs.base;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Preconditions;
import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.ProceduralModalProcessGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DefaultCFMPS<L, AP> implements ContextFreeModalProcessSystem<L, AP> {

    private final Map<L, ProceduralModalProcessGraph<?, L, ?, AP, ?>> pmpgs;
    private final L mainProcess;

    public DefaultCFMPS(L mainProcess, Map<L, ? extends ProceduralModalProcessGraph<?, L, ?, AP, ?>> pmpgs) {
        Preconditions.checkArgument(pmpgs.containsKey(mainProcess),
                                    "There exists no process graph for the main process");

        this.pmpgs = Collections.unmodifiableMap(pmpgs);
        this.mainProcess = mainProcess;
    }

    @Override
    public Map<L, ProceduralModalProcessGraph<?, L, ?, AP, ?>> getPMPGs() {
        return this.pmpgs;
    }

    @Override
    public @Nullable L getMainProcess() {
        return this.mainProcess;
    }
}
