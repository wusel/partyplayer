/*
 *  Copyright (C) 2010 wusel
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.wusel.partyplayer.gui.dialog;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;

/**
 *
 * @author wusel
 */
public class SettingsDialog extends JDialog {

    private static final Logger log = Logger.getLogger(SettingsDialog.class);
    private DialogStatus status = DialogStatus.CANCELED;
    private final FileListModel listModel;

    public SettingsDialog(Window owner, List<File> files) {
        super(owner, "Settings", ModalityType.APPLICATION_MODAL);
        listModel = new FileListModel(files);
        initUI();
        pack();
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new MigLayout("fill", "[] [grow] []", "[] [] [] [grow] []"));
        add(new JLabel("Ordner"));
        add(new JSeparator(), "grow, span, wrap");
        final JList fileList = new JList(listModel);
        add(fileList, "span 2 3, grow");

        final JButton addButton = new JButton(getIcon("folder_add"));
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(rootPane);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    for (File file : selectedFiles) {
                        listModel.addFile(file);
                    }
                }

            }
        });

        final JButton removeButton = new JButton(getIcon("folder_delete"));
        removeButton.setEnabled(false);

        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedValues = fileList.getSelectedIndices();
                for (int i = selectedValues.length - 1; i >= 0; i--) {
                    listModel.removeFile(selectedValues[i]);
                }
                pack();
            }
        });
        
        fileList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    removeButton.setEnabled(fileList.getSelectedIndex() != -1);
                }
            }
        });
        add(addButton, "wrap");
        add(removeButton, "wrap, grow");
        add(new JSeparator(), "newline push, span, grow, aligny top, wrap");
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0 0 0 0, fill"));
        final JButton cancelButton = new JButton("cancel");
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        buttonPanel.add(cancelButton, "split 2, tag cancel");
        final JButton okButton = new JButton("ok");
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        buttonPanel.add(okButton, "tag ok");
        add(buttonPanel, "span 3, growx");
    }

    public List<File> getSelectedDirectories() {
        return Collections.unmodifiableList(listModel.files);
    }

    private void ok() {
        status = DialogStatus.CONFIRMED;
        dispose();
    }

    private void cancel() {
        status = DialogStatus.CANCELED;
        dispose();
    }

    public DialogStatus getStatus() {
        return status;
    }

    public static final class FileListModel extends AbstractListModel {

        private final List<File> files = new ArrayList<File>();

        private FileListModel(List<File> files) {
            this.files.addAll(files);
        }

        @Override
        public int getSize() {
            return files.size();
        }

        @Override
        public Object getElementAt(int index) {
            return files.get(index);
        }

        public void addFile(File file) {
            this.files.add(file);
            fireIntervalAdded(file, this.files.size(), this.files.size());
        }

        public void removeFile(int index) {
            this.files.remove(index);
            fireIntervalRemoved(this, index, index);
        }
    }

    private ImageIcon getIcon(String iconName) {
        try {
            return new ImageIcon(ImageIO.read(SettingsDialog.class.getResourceAsStream("/icons/" + iconName + ".png")));
        } catch (IOException ex) {
            log.fatal("could not load icon", ex);
            return null;
        }
    }
}
