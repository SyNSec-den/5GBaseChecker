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
package net.automatalib.serialization.taf.parser;

import java.text.MessageFormat;

/**
 * Diagnostic listener for non-fatal errors and warnings during parsing of a TAF file. The parser will usually recover
 * from these errors and produce a valid automaton anyway. Fatal errors are not reported to a diagnostic listener, but
 * instead a {@link TAFFormatException} is thrown.
 *
 * @author Malte Isberner
 */
public interface TAFParseDiagnosticListener {

    /**
     * Called when a non-fatal error is encountered during parsing.
     * <p>
     * A non-fatal error could be, for example, the usage of an input symbol that was not declared in the alphabet. In
     * this case, the respective transition is simply ignored.
     *
     * @param line
     *         the line where the error occurred
     * @param col
     *         the column where the error occurred
     * @param msgFmt
     *         a format string of the message (see {@link MessageFormat})
     * @param args
     *         the arguments of the message
     */
    void error(int line, int col, String msgFmt, Object... args);

    /**
     * Called when a warning is raised during parsing.
     * <p>
     * A warning could be raised when, for example, an unrecognized option is used for a state.
     *
     * @param line
     *         the line where the warning was raised
     * @param col
     *         the column where the warning was raised
     * @param msgFmt
     *         a format string of the message (see {@link MessageFormat})
     * @param args
     *         the arguments of the message
     */
    void warning(int line, int col, String msgFmt, Object... args);
}
