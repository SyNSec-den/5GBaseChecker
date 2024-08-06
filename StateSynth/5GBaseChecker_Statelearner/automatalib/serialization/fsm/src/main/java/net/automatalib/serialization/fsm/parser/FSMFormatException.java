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
package net.automatalib.serialization.fsm.parser;

import java.io.StreamTokenizer;

import net.automatalib.serialization.FormatException;

/**
 * Exception that may be thrown whenever an FSM is illegal.
 *
 * @author Jeroen Meijer
 */
public class FSMFormatException extends FormatException {

    public static final String MESSAGE = "Unable to parse FSM: %s at line %d";

    public FSMFormatException(String message) {
        super(message);
    }

    public FSMFormatException(final String message, final StreamTokenizer streamTokenizer) {
        super(String.format(MESSAGE, message, streamTokenizer.lineno()));
    }

    public FSMFormatException(Exception e, final StreamTokenizer streamTokenizer) {
        super(String.format(MESSAGE, e.getMessage(), streamTokenizer.lineno()));
    }
}
