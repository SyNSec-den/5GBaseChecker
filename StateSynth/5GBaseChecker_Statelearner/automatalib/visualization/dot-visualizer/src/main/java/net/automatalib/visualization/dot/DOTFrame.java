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

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Reader;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class DOTFrame extends JFrame {

    private final DOTPanel dotPanel = new DOTPanel();

    public DOTFrame() {
        this("Graphs");
    }

    public DOTFrame(String title) {
        super(title);
        setContentPane(dotPanel);

        JMenu menu = new JMenu("File");
        menu.add(dotPanel.getSavePngAction());
        menu.add(dotPanel.getSaveDotAction());
        menu.add(dotPanel.getRenameAction());
        menu.addSeparator();
        menu.add(dotPanel.getClearAction());
        menu.add(new AbstractAction("Close") {

            @Override
            public void actionPerformed(ActionEvent e) {
                DOTFrame.this.dispatchEvent(new WindowEvent(DOTFrame.this, WindowEvent.WINDOW_CLOSING));
            }
        });
        JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(menu);
        setJMenuBar(jMenuBar);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
    }

    public void addGraph(String name, Reader dotText) throws IOException {
        dotPanel.addGraph(name, dotText);
    }

    public void addGraph(String name, String dotText) {
        dotPanel.addGraph(name, dotText);
    }

}
