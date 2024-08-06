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
package net.automatalib.modelcheckers.m3c.formula.modalmu;

import java.io.IOException;
import java.util.Objects;

import net.automatalib.modelcheckers.m3c.formula.AbstractUnaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract super-class for fix-point (sub-) formulas.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public abstract class AbstractFixedPointFormulaNode<L, AP> extends AbstractUnaryFormulaNode<L, AP> {

    private final String variable;

    public AbstractFixedPointFormulaNode(String variable, FormulaNode<L, AP> child) {
        super(child);
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(variable);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }

        final AbstractFixedPointFormulaNode<?, ?> that = (AbstractFixedPointFormulaNode<?, ?>) o;

        return this.variable.equals(that.variable);
    }

    protected void printMuCalcNode(Appendable a, String fixedPoint) throws IOException {
        a.append('(');
        a.append(fixedPoint);
        a.append(' ');
        a.append(variable);
        a.append(".(");
        getChild().print(a);
        a.append("))");
    }

}
