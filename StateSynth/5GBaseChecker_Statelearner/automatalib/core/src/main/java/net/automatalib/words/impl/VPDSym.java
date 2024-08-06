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
package net.automatalib.words.impl;

import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.VPDAlphabet.SymbolType;

/**
 * Utility class used to wrap input symbols of a {@link VPDAlphabet}. Stores additional index information to allow fast
 * index-based access.
 *
 * @param <T>
 *         type of the payload
 *
 * @author Malte Isberner
 */
public final class VPDSym<T> {

    private final T userObject;
    private final SymbolType type;
    private final int localIndex, globalIndex;

    VPDSym(final T userObject, final SymbolType type, final int localIndex, final int globalIndex) {
        this.userObject = userObject;
        this.type = type;
        this.localIndex = localIndex;
        this.globalIndex = globalIndex;
    }

    public VPDAlphabet.SymbolType getType() {
        return type;
    }

    public T getUserObject() {
        return userObject;
    }

    public int getLocalIndex() {
        return localIndex;
    }

    public int getGlobalIndex() {
        return globalIndex;
    }

    @Override
    public String toString() {
        return String.valueOf(userObject);
    }
}
