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
package net.automatalib.visualization.dot;

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import net.automatalib.AutomataLibProperty;
import net.automatalib.AutomataLibSettings;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.process.ProcessUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to simplify operating the GraphVIZ "dot" utility. Please note that all of the provided methods require
 * GraphVIZ to be installed on the system, and that the "dot" binary resides in the execution path.
 *
 * @author Malte Isberner
 */
public final class DOT {

    private static final Logger LOGGER = LoggerFactory.getLogger(DOT.class);

    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 600;

    private static String dotExe;

    static {
        AutomataLibSettings settings = AutomataLibSettings.getInstance();

        String dotExePath = settings.getProperty(AutomataLibProperty.DOT_EXE_DIR);
        String dotExeName = settings.getProperty(AutomataLibProperty.DOT_EXE_NAME, "dot");

        String dotExe = dotExeName;
        if (dotExePath != null) {
            Path dotBasePath = FileSystems.getDefault().getPath(dotExePath);
            Path resolvedDotPath = dotBasePath.resolve(dotExeName);
            dotExe = resolvedDotPath.toString();
        }

        DOT.dotExe = dotExe;
    }

    private DOT() {}

    public static void setDotExe(String dotExe) {
        DOT.dotExe = dotExe;
    }

    public static boolean checkUsable() {
        try {
            final String[] dotCheck = buildRawDOTCommand("-V");
            return ProcessUtil.invokeProcess(dotCheck) == 0;
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("Error executing dot", ex);
        }
        return false;
    }

    public static String[] buildRawDOTCommand(String... opts) {
        String[] dotArgs = new String[1 + opts.length];
        dotArgs[0] = dotExe;
        System.arraycopy(opts, 0, dotArgs, 1, opts.length);

        return dotArgs;
    }

    public static String[] buildDOTCommand(String format, String... additionalOpts) {
        String[] dotArgs = new String[1 + additionalOpts.length];
        dotArgs[0] = "-T" + format;
        System.arraycopy(additionalOpts, 0, dotArgs, 1, additionalOpts.length);

        return buildRawDOTCommand(dotArgs);
    }

    /**
     * Invokes the DOT utility on a string. Convenience method, see {@link #runDOT(Reader, String, String...)}
     */
    public static InputStream runDOT(String dotText, String format, String... additionalOpts) throws IOException {
        try (StringReader sr = new StringReader(dotText)) {
            return runDOT(sr, format, additionalOpts);
        }
    }

    /**
     * Invokes the GraphVIZ DOT utility for rendering graphs.
     *
     * @param r
     *         the reader from which the GraphVIZ description is obtained.
     * @param format
     *         the output format, as understood by the dot utility, e.g., png, ps, ...
     *
     * @return an input stream from which the image data can be read.
     *
     * @throws IOException
     *         if reading from the specified reader fails.
     */
    public static InputStream runDOT(Reader r, String format, String... additionalOpts) throws IOException {
        String[] dotCommand = buildDOTCommand(format, additionalOpts);

        Process p = ProcessUtil.buildProcess(dotCommand, r, null, LOGGER::warn);

        return p.getInputStream();
    }

    /**
     * Invokes the DOT utility on a file. Convenience method, see {@link #runDOT(Reader, String, String...)}.
     */
    public static InputStream runDOT(File dotFile, String format, String... additionalOpts) throws IOException {
        return runDOT(IOUtil.asBufferedUTF8Reader(dotFile), format, additionalOpts);
    }

    /**
     * Invokes the DOT utility on a string, producing an output file. Convenience method, see {@link #runDOT(Reader,
     * String, File)}.
     */
    public static void runDOT(String dotText, String format, File out) throws IOException {
        runDOT(new StringReader(dotText), format, out);
    }

    /**
     * Invokes the GraphVIZ DOT utility for rendering graphs, writing output to the specified file.
     *
     * @param r
     *         the reader from which the GraphVIZ description is read.
     * @param format
     *         the output format to produce.
     * @param out
     *         the file to which the output is written.
     *
     * @throws IOException
     *         if an I/O error occurs reading from the given input or writing to the output file.
     */
    public static void runDOT(Reader r, String format, File out) throws IOException {
        String[] dotCommand = buildDOTCommand(format, "-o" + out.getAbsolutePath());

        try {
            ProcessUtil.invokeProcess(dotCommand, r, LOGGER::warn);
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted while waiting for 'dot' process to exit.", ex);
        }
    }

    /**
     * Invokes the DOT utility on a file, producing an output file. Convenience method, see {@link #runDOT(Reader,
     * String, File)}.
     */
    public static void runDOT(File dotFile, String format, File out) throws IOException {
        runDOT(IOUtil.asBufferedUTF8Reader(dotFile), format, out);
    }

    /**
     * Renders a GraphVIZ description from a string, using an external program for displaying. Convenience method, see
     * {@link #renderDOTExternal(Reader, String)}.
     */
    public static void renderDOTExternal(String dotText, String format) {
        renderDOTExternal(new StringReader(dotText), format);
    }

    /**
     * Renders a GraphVIZ description, using an external program for displaying. The program is determined by the
     * system's file type associations, using the {@link Desktop#open(File)} method.
     *
     * @param r
     *         the reader from which the GraphVIZ description is read.
     * @param format
     *         the output format, as understood by the dot utility, e.g., png, ps, ...
     */
    public static void renderDOTExternal(Reader r, String format) {
        try {
            File image = File.createTempFile("dot", format);
            runDOT(r, format, image);
            Desktop.getDesktop().open(image);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                                          "Error rendering DOT: " + e.getMessage(),
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Renders a GraphVIZ description from a file, using an external program for displaying. Convenience method, see
     * {@link #renderDOTExternal(Reader, String)}.
     *
     * @throws IOException
     *         if opening the file resulted in errors.
     */
    public static void renderDOTExternal(File dotFile, String format) throws IOException {
        renderDOTExternal(IOUtil.asBufferedUTF8Reader(dotFile), format);
    }

    /**
     * Renders a GraphVIZ description from a string and displays it in a Swing window. Convenience method, see {@link
     * #renderDOT(Reader, boolean)}.
     *
     * @throws FileNotFoundException
     *         if opening the file resulted in errors.
     */
    public static void renderDOT(File dotFile, boolean modal) throws IOException {
        renderDOT(IOUtil.asBufferedUTF8Reader(dotFile), modal);
    }

    /**
     * Renders a GraphVIZ description and displays it in a Swing window.
     *
     * @param r
     *         the reader from which the description is obtained.
     * @param modal
     *         whether the dialog should be modal.
     */
    public static void renderDOT(Reader r, boolean modal) {
        final DOTComponent cmp = createDOTComponent(r);
        if (cmp == null) {
            return;
        }

        final JDialog frame = new JDialog((Dialog) null, modal);
        JScrollPane scrollPane = new JScrollPane(cmp);
        frame.setContentPane(scrollPane);
        frame.setMaximumSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
        frame.pack();
        JMenu menu = new JMenu("File");
        menu.add(cmp.getSavePngAction());
        menu.add(cmp.getSaveDotAction());
        menu.addSeparator();
        menu.add(new AbstractAction("Close") {

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        frame.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }
            }
        });
    }

    /**
     * Renders a GraphVIZ description from a string and displays it in a Swing window. Convenience method, see {@link
     * #renderDOT(Reader, boolean)}.
     */
    public static void renderDOT(String dotText, boolean modal) {
        renderDOT(new StringReader(dotText), modal);
    }

    /**
     * Creates a {@link DOTComponent} that displays the result of rendering a DOT description read from a {@link
     * Reader}.
     *
     * @param r
     *         the reader to read from
     *
     * @return the DOT component
     */
    public static @Nullable DOTComponent createDOTComponent(Reader r) {
        try {
            return new DOTComponent(r);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                                          "Could not run DOT: " + e.getMessage(),
                                          "Failed to run DOT",
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Reads a DOT description from a string and returns the PNG rendering result as a {@link BufferedImage}.
     *
     * @param dotText
     *         the DOT description
     *
     * @return the rendering result
     *
     * @throws IOException
     *         if the pipe to the DOT process breaks.
     */
    public static BufferedImage renderDOTImage(String dotText) throws IOException {
        return renderDOTImage(new StringReader(dotText));
    }

    /**
     * Reads a DOT description from a reader and returns the PNG rendering result as a {@link BufferedImage}.
     *
     * @param dotReader
     *         the reader from which to read the description
     *
     * @return the rendering result
     *
     * @throws IOException
     *         if reading from the reader fails, or the pipe to the DOT process breaks.
     */
    public static BufferedImage renderDOTImage(Reader dotReader) throws IOException {
        try (InputStream pngIs = runDOT(dotReader, "png")) {
            return ImageIO.read(pngIs);
        }
    }

    /**
     * Reads a DOT description from a file and returns the PNG rendering result as a {@link BufferedImage}.
     *
     * @param dotFile
     *         the file containing the DOT description
     *
     * @return the rendering result
     *
     * @throws IOException
     *         if reading from the file fails or the pipe to the DOT process breaks.
     */
    public static BufferedImage renderDOTImage(File dotFile) throws IOException {
        return renderDOTImage(IOUtil.asBufferedUTF8Reader(dotFile));
    }

    /**
     * Creates a Writer that can be used to write a DOT description to. Upon closing the writer, a window with the
     * rendering result appears.
     *
     * @param modal
     *         whether this window is modal (if set to {@code true}, calls to {@link Writer#close()} will block.
     *
     * @return the writer
     */
    public static Writer createDotWriter(final boolean modal) {
        return new StringWriter() {

            @Override
            public void close() throws IOException {
                renderDOT(toString(), modal);
                super.close();
            }
        };
    }

}
