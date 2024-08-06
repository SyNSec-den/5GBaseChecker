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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.google.common.io.CharStreams;
import net.automatalib.commons.util.IOUtil;

public class DOTComponent extends ImageComponent {

    private final String dot;

    private final Action saveDotAction;

    public DOTComponent(Reader dotReader) throws IOException {
        StringBuilder sb = new StringBuilder();

        CharStreams.copy(dotReader, sb);
        String dot = sb.toString();

        BufferedImage img = DOT.renderDOTImage(dot);

        super.setImage(img);
        this.dot = dot;
        this.saveDotAction = new AbstractAction("Save DOT") {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser saveDlg = new JFileChooser();
                saveDlg.setFileFilter(DOTMisc.DOT_FILTER);
                int result = saveDlg.showSaveDialog(DOTComponent.this);
                if (result != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                try (Writer w = IOUtil.asBufferedUTF8Writer(saveDlg.getSelectedFile())) {
                    w.write(dot);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(DOTComponent.this,
                                                  "Could not save DOT file: " + ex.getMessage(),
                                                  "Cannot save DOT",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }
        };
    }

    @Override
    public void listActions(List<Action> actions) {
        super.listActions(actions);
        actions.add(saveDotAction);
    }

    public String getDot() {
        return dot;
    }

    public Action getSaveDotAction() {
        return saveDotAction;
    }

}
