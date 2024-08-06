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
package net.automatalib.visualization;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.checkerframework.checker.nullness.qual.Nullable;

public class VPManager {

    private final Map<String, VisualizationProvider> providers = new HashMap<>();
    private VisualizationProvider bestProvider = new DummyVP();

    public void load() {
        providers.clear();
        ServiceLoader<VisualizationProvider> loader = ServiceLoader.load(VisualizationProvider.class);

        bestProvider = new DummyVP();
        for (VisualizationProvider vp : loader) {
            registerProvider(vp);
        }
    }

    public void registerProvider(VisualizationProvider vp) {
        if (!vp.checkUsable()) {
            return;
        }
        providers.put(vp.getId(), vp);

        if (vp.getPriority() > bestProvider.getPriority()) {
            bestProvider = vp;
        }
    }

    public VisualizationProvider getBestProvider() {
        return bestProvider;
    }

    public @Nullable VisualizationProvider getProviderById(String id) {
        return providers.get(id);
    }

}
