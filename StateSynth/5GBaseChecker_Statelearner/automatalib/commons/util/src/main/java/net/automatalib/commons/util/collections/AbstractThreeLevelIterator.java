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
package net.automatalib.commons.util.collections;

import java.util.Iterator;

public abstract class AbstractThreeLevelIterator<L1, L2, L3, O> implements Iterator<O> {

    private final InnerIterator innerIterator;

    public AbstractThreeLevelIterator(Iterator<L1> l1Iterator) {
        OuterIterator outerIterator = new OuterIterator(l1Iterator);
        this.innerIterator = new InnerIterator(outerIterator);
    }

    protected abstract Iterator<L2> l2Iterator(L1 l1Object);

    protected abstract Iterator<L3> l3Iterator(L1 l1Object, L2 l2Object);

    protected abstract O combine(L1 l1Object, L2 l2Object, L3 l3Object);

    @Override
    public boolean hasNext() {
        return innerIterator.hasNext();
    }

    @Override
    public O next() {
        return innerIterator.next();
    }

    @Override
    public void remove() {
        innerIterator.remove();
    }

    private static class Outer<L1, L2> {

        private L1 l1Item;
        private L2 l2Item;
    }

    private class OuterIterator extends AbstractTwoLevelIterator<L1, L2, Outer<L1, L2>> {

        private final Outer<L1, L2> value = new Outer<>();

        OuterIterator(Iterator<L1> l1Iterator) {
            super(l1Iterator);
        }

        @Override
        protected Iterator<L2> l2Iterator(L1 l1Object) {
            return AbstractThreeLevelIterator.this.l2Iterator(l1Object);
        }

        @Override
        protected Outer<L1, L2> combine(L1 l1Object, L2 l2Object) {
            value.l1Item = l1Object;
            value.l2Item = l2Object;
            return value;
        }
    }

    private class InnerIterator extends AbstractTwoLevelIterator<Outer<L1, L2>, L3, O> {

        InnerIterator(Iterator<Outer<L1, L2>> outerIterator) {
            super(outerIterator);
        }

        @Override
        protected Iterator<L3> l2Iterator(Outer<L1, L2> outer) {
            return AbstractThreeLevelIterator.this.l3Iterator(outer.l1Item, outer.l2Item);
        }

        @Override
        protected O combine(Outer<L1, L2> outer, L3 l3Object) {
            return AbstractThreeLevelIterator.this.combine(outer.l1Item, outer.l2Item, l3Object);
        }
    }

}
