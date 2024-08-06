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
package net.automatalib.commons.util.ref;

/**
 * Utility functions for dealing with references.
 *
 * @author Malte Isberner
 */
public final class Refs {

    private Refs() {}

    /**
     * Creates a strong reference to the given referent.
     *
     * @param referent
     *         the referent.
     *
     * @return a strong reference to the referent.
     */
    public static <T> StrongRef<T> strong(T referent) {
        return new StrongRef<>(referent);
    }

    /**
     * Creates a weak reference to the given referent.
     *
     * @param referent
     *         the referent.
     *
     * @return a weak reference to the referent.
     */
    public static <T> WeakRef<T> weak(T referent) {
        return new WeakRef<>(referent);
    }
}

