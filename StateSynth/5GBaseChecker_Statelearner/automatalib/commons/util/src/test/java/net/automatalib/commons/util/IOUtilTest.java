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
package net.automatalib.commons.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IOUtilTest {

    @Test
    public void uncompressedInputStreamTest() throws IOException {

        final String msg = "Hello World";

        try (ByteArrayOutputStream plain = new ByteArrayOutputStream();
             ByteArrayOutputStream compressed = new ByteArrayOutputStream()) {

            try (Writer plainWriter = IOUtil.asBufferedUTF8Writer(plain);
                 Writer compressedWriter = IOUtil.asBufferedUTF8Writer(new GZIPOutputStream(compressed))) {
                plainWriter.append(msg);
                compressedWriter.append(msg);
            }

            try (BufferedReader plainReader = buildReader(plain.toByteArray());
                 BufferedReader compressedReader = buildReader(compressed.toByteArray())) {
                Assert.assertEquals(plainReader.readLine(), msg);
                Assert.assertEquals(compressedReader.readLine(), msg);
            }
        }
    }

    private static BufferedReader buildReader(byte[] src) throws IOException {
        return new BufferedReader(IOUtil.asUTF8Reader(IOUtil.asUncompressedInputStream(new ByteArrayInputStream(src))));
    }
}
