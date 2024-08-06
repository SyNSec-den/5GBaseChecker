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
 * A utility interface that unions {@link InputModelSerializer} and {@link InputModelDeserializer} and extends {@link
 * SerializationProvider}.
 * <p>
 * This interface allows to specify two independent types (one for serialization, one for de-serialization) which allows
 * implementing classes to specify types more precise. This may be useful if the respective types hold generics as well
 * (which are invariant).
 *
 * @param <I>
 *         the input symbol type
 * @param <OUT>
 *         The type of objects that should be serialized
 * @param <IN>
 *         The type of objects that should be de-serialized
 *
 * @author frohme
 */
public interface InputModelSerializationProvider<I, OUT extends SimpleTS<?, I>, IN extends SimpleTS<?, I>> extends
                                                                                                           InputModelSerializer<I, OUT>,
                                                                                                           InputModelDeserializer<I, IN>,
                                                                                                           SerializationProvider<InputModelData<I, OUT>, InputModelData<I, IN>> {}
