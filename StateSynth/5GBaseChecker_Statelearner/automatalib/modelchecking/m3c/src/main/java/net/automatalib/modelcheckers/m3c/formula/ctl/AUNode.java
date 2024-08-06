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
package net.automatalib.modelcheckers.m3c.formula.ctl;

import java.io.IOException;

import net.automatalib.modelcheckers.m3c.formula.AbstractBinaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;

/**
 * Java representation of a "AU" (sub-)formula.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 *
 * @author murtovi
 */
public class AUNode<L, AP> extends AbstractBinaryFormulaNode<L, AP> {

    public AUNode(FormulaNode<L, AP> leftChild, FormulaNode<L, AP> rightChild) {
        super(leftChild, rightChild);
    }

    @Override
    public void print(Appendable a) throws IOException {
        printUntilNode(a, 'A', 'U');
    }

    @Override
    public <T> T accept(FormulaNodeVisitor<T, L, AP> visitor) {
        return visitor.visit(this);
    }

}
