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
package net.automatalib.modelcheckers.ltsmin;

import java.io.StringReader;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.serialization.etf.writer.DFA2ETFWriter;
import net.automatalib.serialization.etf.writer.Mealy2ETFWriterAlternating;
import net.automatalib.serialization.etf.writer.Mealy2ETFWriterIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parser that verifies the syntax of LTL formulae of LTSmin.
 * <p>
 * The syntax definition is based on the grammar in the paper
 * <a href="https://link.springer.com/chapter/10.1007/978-3-662-46681-0_61">LTSmin: High-Performance
 * Language-Independent Model Checking</a>.
 * <p>
 * This parser offers two flavors of formulae which correspond to the two types of LTSmin model-checker currently
 * available in AutomataLib:
 * <ul>
 *     <li>'letter' checks that each register is named "letter". This is used by {@link DFA}-based hypotheses and {@link MealyMachine}-based hypotheses with alternating input/output labels.</li>
 *     <li>'io' checks that each register is named "input" or "output". This is used by {@link MealyMachine}-based hypotheses with synchronous labeling.</li>
 * </ul>
 *
 * @author frohme
 */
public final class LTSminLTLParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(LTSminLTLParser.class);

    private LTSminLTLParser() {}

    /**
     * Checks if the given formula adheres to LTSmin's expected format ('letter' flavor).
     *
     * @param formula
     *         the formula to parse / validate
     *
     * @return {@code formula}
     *
     * @throws IllegalArgumentException
     *         if the formula does not adhere to LTSmin's expected format ('letter' flavor)
     * @see DFA2ETFWriter
     * @see Mealy2ETFWriterAlternating
     */
    public static String requireValidLetterFormula(String formula) {
        final InternalLTSminLTLParser parser = new InternalLTSminLTLParser(new StringReader(formula));

        try {
            parser.letterFormula();
        } catch (ParseException | TokenMgrError e) {
            throw new IllegalArgumentException("Given formula does not adhere to expected format", e);
        }

        return formula;
    }

    /**
     * Checks if the given formula adheres to LTSmin's expected format ('letter' flavor).
     *
     * @param formula
     *         the formula to parse / validate
     *
     * @return {@code true} if the formula adheres to LTSmin's expected format ('letter' flavor), {@code false}
     * otherwise.
     *
     * @see DFA2ETFWriter
     * @see Mealy2ETFWriterAlternating
     */
    public static boolean isValidLetterFormula(String formula) {
        try {
            requireValidLetterFormula(formula);
        } catch (IllegalArgumentException iae) {
            LOGGER.debug("Couldn't parse formula", iae);
            return false;
        }

        return true;
    }

    /**
     * Checks if the given formula adheres to LTSmin's expected format ('io' flavor).
     *
     * @param formula
     *         the formula to parse / validate
     *
     * @return {@code formula}
     *
     * @throws IllegalArgumentException
     *         if the formula does not adhere to LTSmin's expected format ('io' flavor)
     * @see Mealy2ETFWriterIO
     */
    public static String requireValidIOFormula(String formula) {
        final InternalLTSminLTLParser parser = new InternalLTSminLTLParser(new StringReader(formula));

        try {
            parser.ioFormula();
        } catch (ParseException | TokenMgrError e) {
            throw new IllegalArgumentException("Given formula does not adhere to expected format", e);
        }

        return formula;
    }

    /**
     * Checks if the given formula adheres to LTSmin's expected format ('io' flavor).
     *
     * @param formula
     *         the formula to parse / validate
     *
     * @return {@code true} if the formula adheres to LTSmin's expected format ('io' flavor), {@code false} otherwise.
     *
     * @see Mealy2ETFWriterIO
     */
    public static boolean isValidIOFormula(String formula) {
        try {
            requireValidIOFormula(formula);
        } catch (IllegalArgumentException iae) {
            LOGGER.debug("Couldn't parse formula", iae);
            return false;
        }

        return true;
    }

}
