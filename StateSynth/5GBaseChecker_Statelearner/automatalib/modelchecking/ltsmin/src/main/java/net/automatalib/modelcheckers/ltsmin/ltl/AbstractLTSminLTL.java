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
package net.automatalib.modelcheckers.ltsmin.ltl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.automatalib.modelcheckers.ltsmin.AbstractLTSmin;
import net.automatalib.modelcheckers.ltsmin.LTSminVersion;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.ModelCheckerLasso;
import net.automatalib.modelchecking.modelchecker.AbstractUnfoldingModelChecker;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An LTSmin model checker for full LTL.
 *
 * @author Jeroen Meijer
 * @see <a href="http://ltsmin.utwente.nl/assets/man/etf2lts-mc.html">man etf2lts-mc</a>
 * @see AbstractLTSmin
 */
public abstract class AbstractLTSminLTL<I, A, L extends Lasso<I, ?>> extends AbstractLTSmin<I, A, L>
        implements ModelCheckerLasso<I, A, String, L> {

    public static final LTSminVersion REQUIRED_VERSION = LTSminVersion.of(3, 0, 0);

    private final AbstractUnfoldingModelChecker<I, A, String, L> unfolder;

    /**
     * Constructs a new AbstractLTSminLTL.
     *
     * @param multiplier
     *         the multiplier.
     * @param minimumUnfolds
     *         the minimum number of unfolds.
     *
     * @see AbstractLTSmin#AbstractLTSmin(boolean, Function)
     */
    protected AbstractLTSminLTL(boolean keepFiles,
                                Function<String, I> string2Input,
                                int minimumUnfolds,
                                double multiplier) {
        super(keepFiles, string2Input);
        unfolder = new AbstractUnfoldingModelChecker<I, A, String, L>(minimumUnfolds, multiplier) {

            @Override
            public @Nullable L findCounterExample(A automaton, Collection<? extends I> inputs, String property) {
                return null;
            }
        };
    }

    @Override
    protected LTSminVersion getMinimumRequiredVersion(@UnknownInitialization(AbstractLTSmin.class) AbstractLTSminLTL<I, A, L> this) {
        return REQUIRED_VERSION;
    }

    @Override
    protected List<String> getExtraCommandLineOptions() {
        return Arrays.asList(// use Buchi automata created by spot
                             "--buchi-type=spotba",
                             // use the Union-Find strategy
                             "--strategy=ufscc");
    }

    @Override
    public double getMultiplier() {
        return unfolder.getMultiplier();
    }

    @Override
    public void setMultiplier(double multiplier) {
        unfolder.setMultiplier(multiplier);
    }

    @Override
    public int getMinimumUnfolds() {
        return unfolder.getMinimumUnfolds();
    }

    @Override
    public void setMinimumUnfolds(int minimumUnfolds) {
        unfolder.setMinimumUnfolds(minimumUnfolds);
    }

    public static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static boolean keepFiles() {
            return false;
        }

        public static int minimumUnfolds() {
            return 3; // super arbitrary number
        }

        public static double multiplier() {
            return 1.0; // quite arbitrary too
        }

        public static <O> Collection<? super O> skipOutputs() {
            return Collections.emptyList();
        }
    }
}
