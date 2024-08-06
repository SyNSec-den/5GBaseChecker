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

import java.util.Objects;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.words.VPDAlphabet;

/**
 * Default implementation for n-SEVPAs.
 *
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class DefaultNSEVPA<I> extends AbstractDefaultSEVPA<I> {

    private final Location[] moduleEntries;
    private final ResizingArrayStorage<I> moduleMapping;

    public DefaultNSEVPA(final VPDAlphabet<I> alphabet) {
        this(alphabet, DEFAULT_SIZE);
    }

    public DefaultNSEVPA(final VPDAlphabet<I> alphabet, int capacityHint) {
        super(alphabet, capacityHint);
        this.moduleEntries = new Location[alphabet.getNumCalls()];
        this.moduleMapping = new ResizingArrayStorage<>(Object.class);
    }

    @Override
    public Location addLocation(boolean accepting) {
        final Location loc = super.addLocation(accepting);
        this.moduleMapping.ensureCapacity(loc.getIndex() + 1);
        return loc;
    }

    public Location addLocation(I module, boolean accepting) {
        final Location result = this.addLocation(accepting);
        this.moduleMapping.array[result.getIndex()] = module;
        return result;
    }

    public Location addModuleEntryLocation(I callSym, boolean accepting) {
        final Location loc = this.addLocation(callSym, accepting);
        this.moduleEntries[alphabet.getCallSymbolIndex(callSym)] = loc;
        return loc;
    }

    @Override
    public void setInternalSuccessor(Location loc, I intSym, Location succ) {
        final I srcModule = this.moduleMapping.array[loc.getIndex()];
        final I tgtModule = this.moduleMapping.array[succ.getIndex()];
        if (!Objects.equals(srcModule, tgtModule)) {
            throw new IllegalArgumentException("Cannot set internal success across different modules");
        }
        super.setInternalSuccessor(loc, intSym, succ);
    }

    @Override
    public void setReturnSuccessor(Location loc, I retSym, int stackSym, Location succ) {
        final Location callLoc = super.getStackLoc(stackSym);
        final I srcModule = this.moduleMapping.array[callLoc.getIndex()];
        final I tgtModule = this.moduleMapping.array[succ.getIndex()];
        if (!Objects.equals(srcModule, tgtModule)) {
            throw new IllegalArgumentException("Must return to the module that was called");
        }
        super.setReturnSuccessor(loc, retSym, stackSym, succ);
    }

    @Override
    public Location getModuleEntry(I callSym) {
        return this.moduleEntries[alphabet.getCallSymbolIndex(callSym)];
    }
}
