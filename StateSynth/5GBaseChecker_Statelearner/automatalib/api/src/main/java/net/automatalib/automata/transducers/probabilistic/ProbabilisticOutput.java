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
package net.automatalib.automata.transducers.probabilistic;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProbabilisticOutput<O> {

    private final float probability;
    private final O output;

    public ProbabilisticOutput(float probability, O output) {
        this.probability = probability;
        this.output = output;
    }

    public float getProbability() {
        return probability;
    }

    public O getOutput() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProbabilisticOutput)) {
            return false;
        }

        final ProbabilisticOutput<?> that = (ProbabilisticOutput<?>) o;
        return Float.compare(probability, that.probability) == 0 && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Float.hashCode(probability);
        result = 31 * result + Objects.hashCode(output);
        return result;
    }
}
