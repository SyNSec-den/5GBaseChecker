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
package net.automatalib.automata.vpda;

import net.automatalib.words.VPDAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract class for k-SEVPAs that implements functionality shared across different subtypes.
 *
 * @param <L>
 *         location type
 * @param <I>
 *         input alphabet type
 *
 * @author Malte Isberner
 */
public abstract class AbstractSEVPA<L, I> implements SEVPA<L, I> {

    protected final VPDAlphabet<I> alphabet;

    public AbstractSEVPA(final VPDAlphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public VPDAlphabet<I> getInputAlphabet() {
        return alphabet;
    }

    @Override
    public @Nullable State<L> getTransition(final State<L> state, final I input) {
        final L loc = state.getLocation();
        final VPDAlphabet.SymbolType type = alphabet.getSymbolType(input);
        switch (type) {
            case CALL:
                final int newStackElem = encodeStackSym(loc, input);
                return new State<>(getModuleEntry(input),
                                   StackContents.push(newStackElem, state.getStackContents()));
            case RETURN: {
                final StackContents contents = state.getStackContents();
                if (contents == null) {
                    return null;
                }
                final int stackElem = contents.peek();
                final L succ = getReturnSuccessor(loc, input, stackElem);
                if (succ == null) {
                    return null;
                }
                return new State<>(succ, contents.pop());
            }
            case INTERNAL: {
                final L succ = getInternalSuccessor(loc, input);
                if (succ == null) {
                    return null;
                }
                return new State<>(succ, state.getStackContents());
            }
            default:
                throw new IllegalStateException("Unknown symbol type " + type);
        }
    }

    @Override
    public int encodeStackSym(final L srcLoc, final I callSym) {
        return encodeStackSym(srcLoc, alphabet.getCallSymbolIndex(callSym));
    }

    public int encodeStackSym(final L srcLoc, final int callSymIdx) {
        return alphabet.getNumCalls() * getLocationId(srcLoc) + callSymIdx;
    }

    @Override
    public int getNumStackSymbols() {
        return size() * alphabet.getNumCalls();
    }

    public L getStackLoc(final int stackSym) {
        return getLocation(stackSym / alphabet.getNumCalls());
    }

    public I getCallSym(final int stackSym) {
        return alphabet.getCallSymbol(stackSym % alphabet.getNumCalls());
    }

}
