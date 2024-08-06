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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

import net.automatalib.exception.ModelCheckingException;
import net.automatalib.modelchecking.ModelChecker;

/**
 * An LTSmin model checker.
 *
 * @param <I> the input type.
 * @param <A> the automaton type.
 * @param <R> the type of a counterexample
 *
 * @author Jeroen Meijer
 */
public interface LTSmin<I, A, R> extends ModelChecker<I, A, String, R> {

    /**
     * Writes the given {@code automaton} to the given {@code etf} file.
     *
     * @param automaton
     *         the automaton to write.
     * @param inputs
     *         the alphabet.
     * @param etf
     *         the file to write to.
     *
     * @throws IOException
     *         when the given {@code automaton} can not be written to {@code etf}.
     * @throws ModelCheckingException
     *         when the given {@code automaton} cannot be transformed into a valid LTS.
     */
    void automaton2ETF(A automaton, Collection<? extends I> inputs, File etf) throws IOException;

    /**
     * Returns whether intermediate files should be kept, e.g. etfs, gcfs, etc.
     *
     * @return the boolean
     */
    boolean isKeepFiles();

    /**
     * Returns the function that transforms edges in FSM files to actual input.
     *
     * @return the Function.
     */
    Function<String, I> getString2Input();
}
