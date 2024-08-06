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
package net.automatalib.commons.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An delegating output stream that does not close but flush the delegate output stream. This is mainly useful for
 * scenarios where we want to close wrappers (to free their resources) but do not want to close the source stream.
 *
 * @author frohme
 */
public class NonClosingOutputStream extends OutputStream {

    private final OutputStream delegate;

    public NonClosingOutputStream(OutputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(int b) throws IOException {
        this.delegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.delegate.flush();
    }

    @Override
    public void close() throws IOException {
        this.delegate.flush();
    }
}
