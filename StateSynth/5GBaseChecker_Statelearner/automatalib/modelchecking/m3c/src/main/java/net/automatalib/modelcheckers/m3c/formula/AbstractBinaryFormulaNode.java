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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract super-class for binary (sub-) formulas.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public abstract class AbstractBinaryFormulaNode<L, AP> extends AbstractFormulaNode<L, AP> {

    private final FormulaNode<L, AP> leftChild;
    private final FormulaNode<L, AP> rightChild;

    public AbstractBinaryFormulaNode(FormulaNode<L, AP> leftChild, FormulaNode<L, AP> rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public FormulaNode<L, AP> getRightChild() {
        return this.rightChild;
    }

    public int getVarNumberRight() {
        return this.rightChild.getVarNumber();
    }

    public FormulaNode<L, AP> getLeftChild() {
        return this.leftChild;
    }

    public int getVarNumberLeft() {
        return this.leftChild.getVarNumber();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }

        final AbstractBinaryFormulaNode<?, ?> that = (AbstractBinaryFormulaNode<?, ?>) o;

        return this.leftChild.equals(that.leftChild) && this.rightChild.equals(that.rightChild);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + leftChild.hashCode();
        result = 31 * result + rightChild.hashCode();
        return result;
    }

    protected void printBinaryFormulaNode(Appendable a, String operator) throws IOException {
        a.append('(');
        getLeftChild().print(a);
        a.append(' ');
        a.append(operator);
        a.append(' ');
        getRightChild().print(a);
        a.append(')');
    }

    protected void printUntilNode(Appendable a, char quantifier, char weakOrStrong) throws IOException {
        a.append('(');
        a.append(quantifier);
        a.append('(');
        getLeftChild().print(a);
        a.append(' ');
        a.append(weakOrStrong);
        a.append(' ');
        getRightChild().print(a);
        a.append("))");
    }

}
