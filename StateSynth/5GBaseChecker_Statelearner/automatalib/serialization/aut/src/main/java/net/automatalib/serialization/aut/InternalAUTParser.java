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
package net.automatalib.serialization.aut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.serialization.InputModelData;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
class InternalAUTParser {

    private int initialState;
    private int numStates;
    private final Set<String> alphabetSymbols = new HashSet<>();
    private final Map<Integer, Map<String, Integer>> transitionMap = new HashMap<>();

    private final InputStream inputStream;

    private char[] currentLineContent;
    private int currentLine;
    private int currentPos;

    InternalAUTParser(InputStream is) {
        this.inputStream = is;
    }

    public <I> InputModelData<I, SimpleAutomaton<Integer, I>> parse(Function<String, I> inputTransformer)
            throws IOException {
        try (BufferedReader br = new BufferedReader(IOUtil.asUncompressedBufferedNonClosingUTF8Reader(inputStream))) {

            // parsing
            parseHeader(br);
            while (parseTransition(br)) {}

            // automaton construction
            final Map<String, I> inputMap = Maps.asMap(alphabetSymbols, inputTransformer::apply);
            final Alphabet<I> alphabet = Alphabets.fromCollection(inputMap.values());

            final CompactNFA<I> result = new CompactNFA<>(alphabet, numStates);

            for (int i = 0; i < numStates; i++) {
                result.addState();
            }

            for (final Map.Entry<Integer, Map<String, Integer>> outgoing : transitionMap.entrySet()) {
                final Integer src = outgoing.getKey();
                for (final Map.Entry<String, Integer> targets : outgoing.getValue().entrySet()) {
                    final String input = targets.getKey();
                    final Integer dest = targets.getValue();
                    result.addTransition(src, inputMap.get(input), dest);
                }
            }
            result.setInitial(initialState, true);

            return new InputModelData<>(result, alphabet);
        }
    }

    private void parseHeader(BufferedReader reader) throws IOException {
        final String line = reader.readLine();

        if (line == null) {
            throw new IllegalArgumentException(buildErrorMessage("Missing description"));
        }

        currentLineContent = line.toCharArray();
        currentPos = 0;

        shiftToNextNonWhitespace();
        verifyDesAndShift();
        verifyLBracketAndShift();
        initialState = parseNumberAndShift();
        verifyCommaAndShift();
        parseNumberAndShift(); // ignore number of transitions
        verifyCommaAndShift();
        numStates = parseNumberAndShift(); // store number of states
        if (numStates < 1) {
            throw new IllegalArgumentException("Number of states must be >= 1");
        }
        verifyRBracketAndShift();
    }

    private boolean parseTransition(BufferedReader reader) throws IOException {
        final String line = reader.readLine();

        if (line == null) {
            return false;
        }

        currentLineContent = line.toCharArray();
        currentLine++;
        currentPos = 0;

        final int start;
        final String label;
        final int dest;

        shiftToNextNonWhitespace();
        verifyLBracketAndShift();
        start = parseNumberAndShift();
        verifyCommaAndShift();
        label = parseLabelAndShift();
        verifyCommaAndShift();
        dest = parseNumberAndShift();
        verifyRBracketAndShift();

        alphabetSymbols.add(label);
        transitionMap.computeIfAbsent(start, k -> new HashMap<>()).put(label, dest);

        return true;
    }

    private void verifyDesAndShift() {

        if (currentLineContent[currentPos] != 'd' || currentLineContent[currentPos + 1] != 'e' ||
            currentLineContent[currentPos + 2] != 's') {
            throw new IllegalArgumentException(buildErrorMessage("Missing 'des' keyword"));
        }

        currentPos += 3;
        shiftToNextNonWhitespace();
    }

    private void verifyLBracketAndShift() {
        verifySymbolAndShift('(');
    }

    private void verifyRBracketAndShift() {
        verifySymbolAndShift(')');
    }

    private void verifyCommaAndShift() {
        verifySymbolAndShift(',');
    }

    private void verifySymbolAndShift(char symbol) {

        if (currentLineContent[currentPos] != symbol) {
            throw new IllegalArgumentException(buildErrorMessage("Expected: " + symbol));
        }

        currentPos++;
        shiftToNextNonWhitespace();
    }

    private void shiftToNextNonWhitespace() {
        for (int i = currentPos; i < currentLineContent.length; i++) {
            switch (currentLineContent[i]) {
                case ' ':
                case '\t':
                case '\r': // probably already filtered by readline
                case '\n': // probably already filtered by readline
                    break;
                default:
                    currentPos = i;
                    return;
            }
        }
    }

    private int parseNumberAndShift() {

        final StringBuilder sb = new StringBuilder();

        char sym = currentLineContent[currentPos];

        while (Character.isDigit(sym)) {
            sb.append(sym);
            currentPos++;
            sym = currentLineContent[currentPos];
        }

        if (sb.length() == 0){
            throw new IllegalArgumentException(buildErrorMessage("Expected a positive number"));
        }

        // forward pointer
        shiftToNextNonWhitespace();
        return Integer.parseInt(sb.toString());
    }

    private String parseLabelAndShift() {

        if (currentLineContent[currentPos] == '"') {
            return parseQuotedLabelAndShift();
        } else {
            return parseNormalLabelAndShift();
        }
    }

    private String parseQuotedLabelAndShift() {
        int openingIndex = currentPos;
        int closingIndex = currentLineContent.length - 1;

        // find terminating "
        while (currentLineContent[closingIndex--] != '"') {}

        // skip terminating " as well
        currentPos = closingIndex + 2;
        shiftToNextNonWhitespace();

        return new String(currentLineContent, openingIndex + 1, closingIndex - openingIndex);
    }

    private String parseNormalLabelAndShift() {

        final char firstChar = currentLineContent[currentPos];

        if (currentLineContent[currentPos] == '*') {
            currentPos++;
            shiftToNextNonWhitespace();
            return "*";
        } else if (Character.isLetter(firstChar)) {
            int startIdx = currentPos;

            while (isValidIdentifier()) {
                currentPos++;
            }

            int endIdx = currentPos;

            shiftToNextNonWhitespace();
            return new String(currentLineContent, startIdx, endIdx - startIdx);
        } else {
            throw new IllegalArgumentException(buildErrorMessage("Invalid unquoted label"));
        }
    }

    private boolean isValidIdentifier() {
        final char currentChar = currentLineContent[currentPos];
        return Character.isLetterOrDigit(currentChar) || currentChar == '_';
    }

    private String buildErrorMessage(String desc) {
        return "In line " + currentLine + ", col " + currentPos + ": " + desc;
    }

}
