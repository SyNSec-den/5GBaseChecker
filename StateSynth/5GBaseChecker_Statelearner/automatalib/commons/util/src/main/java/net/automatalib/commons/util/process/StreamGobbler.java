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
package net.automatalib.commons.util.process;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to consume a given {@link InputStream} inside a standalone thread.
 *
 * @author frohme
 */
final class StreamGobbler extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamGobbler.class);

    private final InputStream inputStream;
    private final InputStreamConsumer outputStream;

    StreamGobbler(InputStream inputStream, InputStreamConsumer outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        try {
            outputStream.consume(inputStream);
        } catch (IOException ioex) {
            LOGGER.error("Error while consuming input stream", ioex);
        }
    }
}
