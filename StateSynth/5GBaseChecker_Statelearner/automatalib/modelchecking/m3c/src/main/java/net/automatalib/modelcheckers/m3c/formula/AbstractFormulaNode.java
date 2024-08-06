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

import net.automatalib.commons.util.strings.AbstractPrintable;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract super-class for (sub-) formulas.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public abstract class AbstractFormulaNode<L, AP> extends AbstractPrintable implements FormulaNode<L, AP> {

    private int varNumber;

    @Override
    public int getVarNumber() {
        return varNumber;
    }

    @Override
    public void setVarNumber(int varNumber) {
        this.varNumber = varNumber;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(varNumber);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractFormulaNode<?, ?> that = (AbstractFormulaNode<?, ?>) o;

        return this.varNumber == that.varNumber;
    }

}
