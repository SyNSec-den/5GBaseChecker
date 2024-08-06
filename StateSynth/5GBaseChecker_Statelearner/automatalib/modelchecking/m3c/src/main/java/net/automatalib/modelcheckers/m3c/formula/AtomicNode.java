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
package net.automatalib.modelcheckers.m3c.formula;

import java.io.IOException;
import java.util.Objects;

import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Java representation of an "atomic proposition" (sub-)formula.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public class AtomicNode<L, AP> extends AbstractFormulaNode<L, AP> {

    private final AP proposition;

    public AtomicNode(AP proposition) {
        this.proposition = proposition;
    }

    public AP getProposition() {
        return proposition;
    }

    @Override
    public void print(Appendable a) throws IOException {
        a.append('\"');
        a.append(Objects.toString(proposition));
        a.append('\"');
    }

    @Override
    public <T> T accept(FormulaNodeVisitor<T, L, AP> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hash(proposition);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }

        final AtomicNode<?, ?> that = (AtomicNode<?, ?>) o;
        return Objects.equals(this.proposition, that.proposition);
    }

}
