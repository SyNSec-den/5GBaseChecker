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
package net.automatalib.serialization;

import net.automatalib.ts.simple.SimpleTS;

/**
 * A refinement of the {@link ModelDeserializer} interface for arbitrary models that can react to inputs. Introduces a
 * new type variable for the input symbol type and limits the model type to {@link SimpleTS}s.
 *
 * @param <I>
 *         the type of input symbols
 * @param <M>
 *         the type of objects implementing classes can deserialize
 *
 * @author frohme
 */
public interface InputModelDeserializer<I, M extends SimpleTS<?, I>> extends ModelDeserializer<InputModelData<I, M>> {}
