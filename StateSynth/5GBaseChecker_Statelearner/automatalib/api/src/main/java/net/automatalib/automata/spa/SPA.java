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
package net.automatalib.automata.spa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.concepts.FiniteRepresentation;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.ts.simple.SimpleTS;
import net.automatalib.words.SPAAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A system of procedural automata. An {@link SPA} is a context-free system where each non-terminal (procedure) is
 * represented by a {@link DFA} that accepts the language of right-hand sides of its respective production rules.
 * <p>
 * Take, for example, the following context-free palindrome system over {@code a,b,c} using two non-terminals {@code
 * S,T}:
 * <pre>
 *     S -&gt; a | a S a | b | b S b | T | ε
 *     T -&gt; c | c T c | S
 * </pre>
 * The corresponding {@link SPA} would consist of {@link DFA procedures} (for {@code S} and {@code T}), accepting the
 * regular languages {@code {a,aSa,b,bSb,T,ε}} and {@code {c,cTc,S}} respectively.
 * <p>
 * In {@link SPA}s, calls to and returns from procedures are visible which make {@link SPA}s a special kind of visibly
 * push-down automata. For the above example, a possible word accepted by the respective {@link SPA} (when using {@code
 * S} as {@link #getInitialProcedure() initial procedure}) would be {@code SaSTcRRaR} (where {@code R} denotes the
 * designated {@link SPAAlphabet#getReturnSymbol() return symbol}.
 * <p>
 * This interface makes no assumptions about how the semantics are implemented. One may use a stack-based approach,
 * graph expansion, or else. However, {@link SPA}s should be <i>consistent</i> with their alphabet definitions, i.e. an
 * {@link SPA} should be able to {@link #accepts(Iterable) parse} words over the {@link #getInputAlphabet() specified
 * alphabet} and each {@link #getProcedures() procedure} should be able to {@link DFA#accepts(Iterable) parse} words
 * over the {@link #getProceduralInputs() procedural inputs}.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public interface SPA<S, I> extends DeterministicAcceptorTS<S, I>,
                                   SuffixOutput<I, Boolean>,
                                   InputAlphabetHolder<I>,
                                   FiniteRepresentation,
                                   GraphViewable {

    /**
     * Refinement of {@link InputAlphabetHolder#getInputAlphabet()}' to add the constraint that an {@link SPA} operates
     * on {@link SPAAlphabet}s.
     *
     * @return the input alphabet
     */
    @Override
    SPAAlphabet<I> getInputAlphabet();

    /**
     * Returns the initial procedure of this {@link SPA}, i.e. the call symbol with which each accepted word has to
     * start.
     *
     * @return the initial procedure
     */
    @Nullable I getInitialProcedure();

    /**
     * In a complete {@link SPA} every {@link #getInputAlphabet() call symbol} should be mapped to a corresponding
     * procedure.
     *
     * @return the procedures of this {@link SPA}
     */
    Map<I, DFA<?, I>> getProcedures();

    /**
     * Convenience method for {@link #getProceduralInputs(SPAAlphabet)} which uses the {@link #getInputAlphabet() input
     * alphabet} of {@code this} {@link SPA} as {@code constraints}.
     *
     * @return a collection of defined inputs for {@code this} {@link SPA}'s procedures.
     */
    default Collection<I> getProceduralInputs() {
        return getProceduralInputs(this.getInputAlphabet());
    }

    /**
     * Returns a collection of input symbols which the procedural automata can process. The collection is computed by
     * the union of this {@link SPA}s {@link SPAAlphabet#getInternalAlphabet() internal symbols} and the {@link
     * #getProcedures() available procedure keys}.
     * <p>
     * This collection can be further constrained via the {@code constraints} parameter which is used in a final
     * intersection operation with the previous collection.
     *
     * @param constraints
     *         an {@link SPAAlphabet} for additionally constraining the returned procedural inputs.
     *
     * @return the (constrained) procedural inputs
     */
    default Collection<I> getProceduralInputs(SPAAlphabet<I> constraints) {
        final List<I> symbols = new ArrayList<>(constraints.size() - 1);

        for (I i : getInputAlphabet()) {
            if (constraints.isInternalSymbol(i)) {
                symbols.add(i);
            }
        }

        for (I procedure : getProcedures().keySet()) {
            if (constraints.isCallSymbol(procedure)) {
                symbols.add(procedure);
            }
        }

        return symbols;
    }

    /**
     * Return the size of this {@link SPA} which is given by the sum of the sizes of all {@link #getProcedures()
     * procedures}. Note that this value does not necessarily correspond to the classical notion of {@link
     * SimpleAutomaton#size()}, since semantically an {@link SPA}s may be infinite-sized {@link SimpleTS}.
     *
     * @return the size of this {@link SPA}
     */
    @Override
    default int size() {
        int size = 0;

        for (DFA<?, I> p : getProcedures().values()) {
            size += p.size();
        }

        return size;
    }

    @Override
    default Boolean computeOutput(Iterable<? extends I> input) {
        return this.accepts(input);
    }

    @Override
    default Graph<?, ?> graphView() {
        final SPAAlphabet<I> alphabet = this.getInputAlphabet();
        // explicit type specification is required by checker-framework
        return new SPAGraphView<@Nullable Object, I>(alphabet.getCallAlphabet(),
                                                     this.getProceduralInputs(alphabet),
                                                     this.getProcedures());
    }

}
