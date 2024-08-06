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
package net.automatalib.commons.util.strings;

import java.io.IOException;

/**
 * Interface that allows outputting to an {@link Appendable} (e.g., a {@link StringBuilder}) instead of simply using
 * {@link Object#toString()}.
 *
 * @author Malte Isberner
 */
public interface Printable {

    static String toString(Printable p) {
        StringBuilder sb = new StringBuilder();
        try {
            p.print(sb);
        } catch (IOException e) {
            throw new AssertionError("Unexpected IOException thrown during operation on StringBuilder.", e);
            // THIS SHOULD NOT HAPPEN
            // since the StringBuilder methods do not throw.
        }
        return sb.toString();
    }

    /**
     * Outputs the current object.
     *
     * @param a
     *         the appendable.
     *
     * @throws IOException
     *         if an error occurs during appending.
     */
    void print(Appendable a) throws IOException;
}
