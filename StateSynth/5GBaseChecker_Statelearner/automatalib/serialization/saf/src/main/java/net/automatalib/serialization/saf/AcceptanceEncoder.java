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
package net.automatalib.serialization.saf;

import java.io.DataOutput;
import java.io.IOException;

final class AcceptanceEncoder implements BlockPropertyEncoder<Boolean> {

    private int currAcc;
    private int mask;

    @Override
    public void start(DataOutput out) {
        currAcc = 0;
        mask = 1;
    }

    @Override
    public void encodeProperty(DataOutput out, Boolean property) throws IOException {
        if (mask == 0) {
            finish(out);
            start(out);
        }
        if (property) {
            currAcc |= mask;
        }
        mask <<= 1;
    }

    @Override
    public void finish(DataOutput out) throws IOException {
        out.writeInt(currAcc);
    }
}