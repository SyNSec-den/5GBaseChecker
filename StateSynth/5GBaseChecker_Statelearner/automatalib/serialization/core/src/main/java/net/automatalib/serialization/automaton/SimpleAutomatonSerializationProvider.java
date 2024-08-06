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
package net.automatalib.serialization.automaton;

/**
 * A utility interface that unions {@link SimpleAutomatonSerializer} and {@link SimpleAutomatonDeserializer}.
 * <p>
 * <b>Note:</b> These model-specific interfaces may be omitted if Java starts supporting higher-kinded generics (or we
 * switch to a language that supports these).
 *
 * @param <S>
 *         The state type of the de-serialized hypothesis
 * @param <I>
 *         The default input symbol type
 *
 * @author frohme
 */
public interface SimpleAutomatonSerializationProvider<S, I>
        extends SimpleAutomatonSerializer<I>, SimpleAutomatonDeserializer<S, I> {}
