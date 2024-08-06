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

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.VPDAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Basic functionality for {@link Location}-based SEVPA implementations.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 * @author frohme
 */
public abstract class AbstractDefaultSEVPA<I> extends AbstractSEVPA<Location, I> {

    protected static final int DEFAULT_SIZE = 10;

    private final List<Location> locations;
    private Location initLoc;

    public AbstractDefaultSEVPA(final VPDAlphabet<I> alphabet, final int capacity) {
        super(alphabet);
        this.locations = new ArrayList<>(capacity);
    }

    public Location addInitialLocation(final boolean accepting) {
        final Location loc = addLocation(accepting);
        setInitialLocation(loc);
        return loc;
    }

    public Location addLocation(final boolean accepting) {
        final Location loc = new Location(alphabet, locations.size(), accepting);
        locations.add(loc);
        return loc;
    }

    @Override
    public int size() {
        return locations.size();
    }

    public void setInternalSuccessor(final Location loc, final I intSym, final Location succ) {
        loc.setInternalSuccessor(alphabet.getInternalSymbolIndex(intSym), succ);
    }

    public void setReturnSuccessor(final Location loc, final I retSym, final int stackSym, final Location succ) {
        loc.setReturnSuccessor(alphabet.getReturnSymbolIndex(retSym), stackSym, succ);
    }

    @Override
    public Location getInternalSuccessor(final Location loc, final I intSym) {
        return loc.getInternalSuccessor(alphabet.getInternalSymbolIndex(intSym));
    }

    @Override
    public Location getLocation(final int id) {
        return locations.get(id);
    }

    @Override
    public int getLocationId(final Location loc) {
        return loc.getIndex();
    }

    @Override
    public List<Location> getLocations() {
        return locations;
    }

    @Override
    public @Nullable Location getReturnSuccessor(final Location loc, final I retSym, final int stackSym) {
        return loc.getReturnSuccessor(alphabet.getReturnSymbolIndex(retSym), stackSym);
    }

    @Override
    public boolean isAcceptingLocation(final Location loc) {
        return loc.isAccepting();
    }

    @Override
    public Location getInitialLocation() {
        return initLoc;
    }

    public void setInitialLocation(final Location loc) {
        this.initLoc = loc;
    }

}
