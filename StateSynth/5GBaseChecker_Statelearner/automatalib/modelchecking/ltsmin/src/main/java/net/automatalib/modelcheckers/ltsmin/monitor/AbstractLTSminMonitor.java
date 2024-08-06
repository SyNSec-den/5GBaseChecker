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
package net.automatalib.modelcheckers.ltsmin.monitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.automatalib.modelcheckers.ltsmin.AbstractLTSmin;
import net.automatalib.modelcheckers.ltsmin.LTSminVersion;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;

/**
 * An LTSmin model checker for monitors.
 *
 * @author Jeroen Meijer
 * @see AbstractLTSmin
 * @see <a href="https://spot.lrde.epita.fr/tut11.html">https://spot.lrde.epita.fr/tut11.html</a>
 * @see <a href="http://ltsmin.utwente.nl/assets/man/etf2lts-mc.html">man etf2lts-mc</a>
 */
public abstract class AbstractLTSminMonitor<I, A, R> extends AbstractLTSmin<I, A, R> {

    public static final LTSminVersion REQUIRED_VERSION = LTSminVersion.of(3, 1, 0);

    /**
     * Constructs a new AbstractLTSminMonitor.
     *
     * @see AbstractLTSmin#AbstractLTSmin(boolean, Function)
     */
    protected AbstractLTSminMonitor(boolean keepFiles, Function<String, I> string2Input) {
        super(keepFiles, string2Input);
    }

    @Override
    protected LTSminVersion getMinimumRequiredVersion(@UnknownInitialization(AbstractLTSmin.class) AbstractLTSminMonitor<I, A, R> this) {
        return REQUIRED_VERSION;
    }

    @Override
    protected List<String> getExtraCommandLineOptions() {
        return Collections.singletonList("--buchi-type=monitor"); // use a monitor created by spot
    }
}
