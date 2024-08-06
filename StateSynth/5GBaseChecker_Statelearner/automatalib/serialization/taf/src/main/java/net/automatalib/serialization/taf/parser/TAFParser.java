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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.IOUtil;

/**
 * Facade for TAF parsing. This class provides several static methods to read TAF descriptions for DFA and Mealy
 * machines.
 *
 * @author Malte Isberner
 */
public final class TAFParser {

    private TAFParser() {}

    public static CompactDFA<String> parseDFA(InputStream stream, TAFParseDiagnosticListener listener)
            throws IOException {
        try (Reader r = IOUtil.asUncompressedBufferedNonClosingUTF8Reader(stream)) {
            return parseDFA(r, listener);
        }
    }

    public static CompactDFA<String> parseDFA(Reader reader, TAFParseDiagnosticListener listener) {
        InternalTAFParser parser = new InternalTAFParser(reader);
        parser.setDiagnosticListener(listener);
        DefaultTAFBuilderDFA builder = new DefaultTAFBuilderDFA(parser);
        try {
            parser.dfa(builder);
        } catch (ParseException ex) {
            throw new TAFFormatException(ex);
        }
        return builder.finish();
    }

    public static CompactDFA<String> parseDFA(File file, TAFParseDiagnosticListener listener) throws IOException {
        try (InputStream is = IOUtil.asBufferedInputStream(file)) {
            return parseDFA(is, listener);
        }
    }

    public static CompactDFA<String> parseDFA(String string, TAFParseDiagnosticListener listener) {
        return parseDFA(new StringReader(string), listener);
    }

    public static CompactMealy<String, String> parseMealy(InputStream stream, TAFParseDiagnosticListener listener)
            throws IOException {
        try (Reader r = IOUtil.asUncompressedBufferedNonClosingUTF8Reader(stream)) {
            return parseMealy(r, listener);
        }
    }

    public static CompactMealy<String, String> parseMealy(Reader reader, TAFParseDiagnosticListener listener) {
        InternalTAFParser parser = new InternalTAFParser(reader);
        parser.setDiagnosticListener(listener);
        DefaultTAFBuilderMealy builder = new DefaultTAFBuilderMealy(parser);
        try {
            parser.mealy(builder);
        } catch (ParseException ex) {
            throw new TAFFormatException(ex);
        }
        return builder.finish();
    }

    public static CompactMealy<String, String> parseMealy(File file, TAFParseDiagnosticListener listener)
            throws IOException {
        try (InputStream is = IOUtil.asBufferedInputStream(file)) {
            return parseMealy(is, listener);
        }
    }

    public static CompactMealy<String, String> parseMealy(String string, TAFParseDiagnosticListener listener) {
        return parseMealy(new StringReader(string), listener);
    }

    public static FiniteAlphabetAutomaton<?, String, ?> parseAny(InputStream stream,
                                                                 TAFParseDiagnosticListener listener)
            throws IOException {
        try (Reader r = IOUtil.asUncompressedBufferedNonClosingUTF8Reader(stream)) {
            return parseAny(r, listener);
        }
    }

    public static FiniteAlphabetAutomaton<?, String, ?> parseAny(Reader reader, TAFParseDiagnosticListener listener) {
        InternalTAFParser parser = new InternalTAFParser(reader);
        parser.setDiagnosticListener(listener);
        try {
            Type type = parser.type();
            switch (type) {
                case DFA: {
                    DefaultTAFBuilderDFA builder = new DefaultTAFBuilderDFA(parser);
                    parser.dfaBody(builder);
                    return builder.finish();
                }
                case MEALY: {
                    DefaultTAFBuilderMealy builder = new DefaultTAFBuilderMealy(parser);
                    parser.mealyBody(builder);
                    return builder.finish();
                }
                default:
                    throw new IllegalStateException("Unknown type " + type);
            }
        } catch (ParseException ex) {
            throw new TAFFormatException(ex);
        }
    }

    public static FiniteAlphabetAutomaton<?, String, ?> parseAny(File file, TAFParseDiagnosticListener listener)
            throws IOException {
        try (InputStream is = IOUtil.asBufferedInputStream(file)) {
            return parseAny(is, listener);
        }
    }

    public static FiniteAlphabetAutomaton<?, String, ?> parseAny(String string, TAFParseDiagnosticListener listener) {
        return parseAny(new StringReader(string), listener);
    }
}
