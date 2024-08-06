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
package net.automatalib.ts.modal.transition;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ModalContractEdgePropertyImpl extends ModalEdgePropertyImpl implements MutableModalContractEdgeProperty {

    private boolean tau;
    private EdgeColor color;

    public ModalContractEdgePropertyImpl(ModalType type, boolean tau, EdgeColor color) {
        super(type);
        this.tau = tau;
        this.color = color;
    }

    @Override
    public boolean isTau() {
        return tau;
    }

    @Override
    public void setTau(boolean tau) {
        this.tau = tau;
    }

    @Override
    public EdgeColor getColor() {
        return color;
    }

    @Override
    public void setColor(EdgeColor color) {
        this.color = color;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }

        final ModalContractEdgePropertyImpl that = (ModalContractEdgePropertyImpl) o;

        return this.tau == that.tau && this.color == that.color;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(tau);
        result = 31 * result + Objects.hashCode(color);
        return result;
    }
}
