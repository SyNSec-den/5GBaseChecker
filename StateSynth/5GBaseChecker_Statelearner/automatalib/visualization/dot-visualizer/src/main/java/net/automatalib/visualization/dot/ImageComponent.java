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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Component that displays a {@link BufferedImage}.
 *
 * @author Malte Isberner
 */
public class ImageComponent extends JComponent {

    private static final int DEFAULT_WIDTH = 320, DEFAULT_HEIGHT = 240;

    private @Nullable BufferedImage img;
    private final Action savePngAction = new AbstractAction("Save PNG") {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(DOTMisc.PNG_FILTER);
            int res = chooser.showSaveDialog(ImageComponent.this);
            if (res != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File f = chooser.getSelectedFile();
            if (f == null) {
                throw new IllegalStateException("No file has been selected");
            }
            if (img == null) {
                throw new IllegalStateException("No image has been set");
            }
            try {
                ImageIO.write(img, "png", f);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ImageComponent.this,
                                              "Couldn't save image: " + ex.getMessage(),
                                              "Couldn't save image",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    };
    private boolean scale;

    /**
     * Default constructor.
     */
    public ImageComponent() {
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    /**
     * Constructor. Initializes the component to display the given image.
     *
     * @param img
     *         the image to be displayed
     */
    public ImageComponent(BufferedImage img) {
        this.img = img;
        Dimension dim = new Dimension(img.getWidth(), img.getHeight());
        setSize(dim);
        setPreferredSize(dim);
    }

    public void listActions(List<Action> actions) {
        actions.add(savePngAction);
    }

    /**
     * Retrieves the image to be displayed.
     *
     * @return the image to be displayed
     */
    public @Nullable BufferedImage getImage() {
        return img;
    }

    /**
     * Sets the image to be displayed.
     *
     * @param img
     *         the image to be displayed
     */
    public void setImage(@UnknownInitialization(ImageComponent.class) ImageComponent this,
                         @Nullable BufferedImage img) {
        this.img = img;
        Dimension dim;
        if (img != null) {
            dim = new Dimension(img.getWidth(), img.getHeight());
        } else {
            dim = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }

        setSize(dim);
        setPreferredSize(dim);
        repaint();
    }

    /**
     * Retrieves an {@link Action} to save the image in a PNG file.
     *
     * @return the action
     */
    public Action getSavePngAction() {
        return savePngAction;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (img != null) {
            if (!scale) {
                g.drawImage(img, 0, 0, null);
            } else {
                g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }

    public void toggleScale() {
        setScale(!scale);
    }

    public void setScale(boolean scale) {
        this.scale = scale;
        repaint();
    }
}
