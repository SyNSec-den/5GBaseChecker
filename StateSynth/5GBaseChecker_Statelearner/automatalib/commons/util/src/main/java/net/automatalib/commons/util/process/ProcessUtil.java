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
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.io.CharStreams;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.process.InputStreamConsumer.DelegatingConsumer;
import net.automatalib.commons.util.process.InputStreamConsumer.NOPConsumer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class for invoking system processes.
 *
 * @author frohme
 */
public final class ProcessUtil {

    private ProcessUtil() {
        // prevent instantiation
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Discards any output of the process.
     *
     * @param commandLine
     *         the list of command line arguments to run
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(String[] commandLine) throws IOException, InterruptedException {
        return invokeProcess(commandLine, null, new NOPConsumer());
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Discards any output of the process.
     *
     * @param commandLine
     *         the list of command line arguments to run
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(List<String> commandLine) throws IOException, InterruptedException {
        return invokeProcess(commandLine, null, new NOPConsumer());
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally allows to supply an input stream to the invoked program. Discards any output of the
     * process.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(String[] commandLine, Reader input) throws IOException, InterruptedException {
        return invokeProcess(commandLine, input, new NOPConsumer());
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally allows to supply an input stream to the invoked program. Discards any output of the
     * process.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(List<String> commandLine, Reader input) throws IOException, InterruptedException {
        return invokeProcess(commandLine, input, new NOPConsumer());
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Outputs of the process (both normal and error) are passed to the {@code consumer}.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param consumer
     *         the consumer for the program's output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(String[] commandLine, Consumer<String> consumer)
            throws IOException, InterruptedException {
        return invokeProcess(commandLine, null, new DelegatingConsumer(consumer));
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Outputs of the process (both normal and error) are passed to the {@code consumer}.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param consumer
     *         the consumer for the program's output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(List<String> commandLine, Consumer<String> consumer)
            throws IOException, InterruptedException {
        return invokeProcess(commandLine, null, new DelegatingConsumer(consumer));
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally allows to supply an input stream to the invoked program. Outputs of the process (both
     * normal and error) are passed to the {@code consumer}.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     * @param consumer
     *         the consumer for the program's output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(String[] commandLine, Reader input, Consumer<String> consumer)
            throws IOException, InterruptedException {
        return invokeProcess(commandLine, input, new DelegatingConsumer(consumer));
    }

    /**
     * Runs the given set of command line arguments as a system process and returns the exit value of the spawned
     * process. Additionally allows to supply an input stream to the invoked program. Outputs of the process (both
     * normal and error) are passed to the {@code consumer}.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program
     * @param consumer
     *         the consumer for the program's output
     *
     * @return the exit code of the process
     *
     * @throws IOException
     *         if an exception occurred while reading the process' outputs, or writing the process' inputs
     * @throws InterruptedException
     *         if an exception occurred during process exception
     */
    public static int invokeProcess(List<String> commandLine, Reader input, Consumer<String> consumer)
            throws IOException, InterruptedException {
        return invokeProcess(commandLine, input, new DelegatingConsumer(consumer));
    }

    private static int invokeProcess(String[] commandLine, @Nullable Reader input, InputStreamConsumer consumer)
            throws IOException, InterruptedException {
        return invokeProcess(new ProcessBuilder(commandLine), input, consumer);
    }

    private static int invokeProcess(List<String> commandLine, @Nullable Reader input, InputStreamConsumer consumer)
            throws IOException, InterruptedException {
        return invokeProcess(new ProcessBuilder(commandLine), input, consumer);
    }

    private static int invokeProcess(ProcessBuilder processBuilder,
                                     @Nullable Reader input,
                                     InputStreamConsumer consumer) throws IOException, InterruptedException {

        processBuilder.redirectErrorStream(true);
        final Process process = processBuilder.start();

        writeProcessInput(process, input);

        // consume process output to prevent blocking from full buffers
        consumer.consume(process.getInputStream());

        try {
            return process.waitFor();
        } finally {
            // cleanup
            process.destroy();
        }
    }

    /**
     * Builds and starts a system process for the given set of command line arguments. Additionally allows to supply an
     * input stream to the invoked program, as well as independent consumers for the process' standard and error output.
     * <p>
     * The consumers for the process' outputs run in separate threads, preventing potential deadlock scenarios where
     * client code waits for the process' termination (e.g. {@link Process#waitFor()}) which is blocked by full system
     * buffers.
     *
     * @param commandLine
     *         the list of command line arguments to run
     * @param input
     *         the input passed to the program, maybe be {@code null} if the process expects no input
     * @param stdOutConsumer
     *         the consumer for the programs output
     * @param stdErrConsumer
     *         the consumer for the programs output
     *
     * @return the reference to the running process
     *
     * @throws IOException
     *         if an exception occurred while reading the process outputs
     */
    public static Process buildProcess(String[] commandLine,
                                       @Nullable Reader input,
                                       @Nullable Consumer<String> stdOutConsumer,
                                       @Nullable Consumer<String> stdErrConsumer) throws IOException {

        final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        final Process process = processBuilder.start();

        writeProcessInput(process, input);

        if (stdOutConsumer != null) {
            final Thread stdOutThread =
                    new StreamGobbler(process.getInputStream(), new DelegatingConsumer(stdOutConsumer));
            stdOutThread.start();
        }

        if (stdErrConsumer != null) {
            final Thread stdErrThread =
                    new StreamGobbler(process.getErrorStream(), new DelegatingConsumer(stdErrConsumer));
            stdErrThread.start();
        }

        return process;
    }

    private static void writeProcessInput(Process process, @Nullable Reader input) throws IOException {
        if (input != null) {
            try (OutputStream processInput = process.getOutputStream();
                 Writer writer = IOUtil.asBufferedUTF8Writer(processInput)) {
                CharStreams.copy(input, writer);
            }
        }
    }

}
