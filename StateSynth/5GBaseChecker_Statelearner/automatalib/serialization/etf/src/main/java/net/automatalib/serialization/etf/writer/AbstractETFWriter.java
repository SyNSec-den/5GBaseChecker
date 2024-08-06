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
package net.automatalib.serialization.etf.writer;

import java.io.PrintWriter;

import net.automatalib.automata.Automaton;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.words.Alphabet;

/**
 * This class provides methods to write automata in LTSmin's ETF format.
 *
 * @see <a href="http://ltsmin.utwente.nl/assets/man/etf.html">the ETF format</a>
 *
 * @author Jeroen Meijer
 */
public abstract class AbstractETFWriter<I, A extends Automaton<?, I, ?>> implements InputModelSerializer<I, A> {

    /**
     * Write the state vector. The state vector contains one variable of type "id", named "id".
     * Valuations for "id" could be identical to the state names of automata.
     *
     * @param printWriter the Writer.
     */
    private void writeState(PrintWriter printWriter) {
        printWriter.println("begin state");
        printWriter.println("id:id");
        printWriter.println("end state");
    }

    /**
     * Write an edge in the LTS. Edges in specializations could be different; e.g. Mealy machines have two edge
     * labels, and DFAs have one edge.
     *
     * @param printWriter the Writer.
     */
    protected abstract void writeEdge(PrintWriter printWriter);

    /**
     * Write parts of the ETF that are dependent on A.
     *
     * @param printWriter the Writer.
     * @param a the automaton to write.
     * @param inputs the alphabet.
     */
    protected abstract void writeETF(PrintWriter printWriter, A a, Alphabet<I> inputs);

    /**
     * Write the full ETF.
     *
     * @param printWriter the Writer.
     * @param a the automaton to write.
     * @param inputs the alphabet.
     */
    protected final void write(PrintWriter printWriter, A a, Alphabet<I> inputs) {
        writeState(printWriter);
        writeEdge(printWriter);
        writeETF(printWriter, a, inputs);
    }

}
