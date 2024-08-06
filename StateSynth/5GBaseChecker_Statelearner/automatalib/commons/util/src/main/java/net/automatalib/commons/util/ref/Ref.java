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

import java.lang.ref.WeakReference;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An abstraction for (weak or strong) references.
 * <p>
 * This class allows for treating normal ("strong") references the same way as {@link WeakReference}s.
 *
 * @param <T>
 *         referent class.
 *
 * @author Malte Isberner
 */
public interface Ref<T> {

    /**
     * Retrieves the referent. In case of {@link WeakRef}s, the return value may become <code>null</code>.
     *
     * @return the referent.
     */
    @Nullable T get();
}
