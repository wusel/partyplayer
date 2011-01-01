/*
 *  Copyright (C) 2011 wusel
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
package de.wusel.partyplayer.gui;

import de.wusel.partyplayer.gui.dialog.ChangePasswordDialog;
import de.wusel.partyplayer.gui.dialog.DialogStatus;
import de.wusel.partyplayer.gui.dialog.SettingsDialog;
import de.wusel.partyplayer.settings.Settings;
import de.wusel.partyplayer.util.PathUtil;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.prompt.PromptSupport;

/**
 *
 * @author wusel
 */
public class LockingStatusbar extends JXStatusBar {

    private static final Logger log = Logger.getLogger(LockingStatusbar.class);
    private JProgressBar fileReaderProgressBar;
    private JLabel statusLabel;
    private JToggleButton lockButton;
    private JButton settingsButton;
    private JPasswordField pinCodeInputField;
    private final Application application;
    private final Settings settings;
    private final List<LockingListener> listeners = new ArrayList<LockingListener>();
    private final PropertyChangeListener listener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("message")) {
                statusLabel.setText(evt.getNewValue().toString());
            } else if (evt.getPropertyName().equals("progress")) {
                log.debug(evt.getNewValue());
                fileReaderProgressBar.setValue((Integer) evt.getNewValue());
            }
        }
    };
    private final JFrame mainFrame;

    public LockingStatusbar(Application application, final JFrame mainFrame, final Settings settings) {
        this.application = application;
        this.settings = settings;
        this.application.getContext().getTaskMonitor().addPropertyChangeListener(listener);
        statusLabel = new JLabel("Ready");
        fileReaderProgressBar = new JProgressBar(0, 100);

        pinCodeInputField = new JPasswordField();
        PromptSupport.setPrompt("pin-code", pinCodeInputField);
        PromptSupport.setForeground(Color.GRAY, pinCodeInputField);

        pinCodeInputField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!pinCodeInputField.isEnabled()) {
                    ChangePasswordDialog dialog = new ChangePasswordDialog(mainFrame, settings);
                    dialog.setVisible(true);
                    if (dialog.getStatus() == DialogStatus.CONFIRMED) {
                        settings.setNewPassword(dialog.getPassDigest());
                        settings.backup(PathUtil.getSettingsFile());
                    }
                }
            }
        });
        pinCodeInputField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean unlocked = unlock(new String(pinCodeInputField.getPassword()));
                if (unlocked) {
                    pinCodeInputField.transferFocus();
                    for (LockingListener lockingListener : listeners) {
                        lockingListener.unlock();
                    }
                }
                pinCodeInputField.setText(null);
            }
        });

        lockButton = new JToggleButton();
        lockButton.setIcon(getIcon("lock"));
        lockButton.setSelectedIcon(getIcon("lock_open"));
        lockButton.setEnabled(false);
        lockButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                lock();
            }
        });

        this.settingsButton = new JButton(getIcon("cog_edit"));
        this.settingsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showSettings();
            }
        });
        add(statusLabel, new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL));
        add(fileReaderProgressBar, new JXStatusBar.Constraint(200));
        add(pinCodeInputField, new JXStatusBar.Constraint(100));
        add(lockButton, new JXStatusBar.Constraint());
        add(settingsButton, new JXStatusBar.Constraint());
        this.mainFrame = mainFrame;
    }

    private void lock() {
        this.lockButton.setEnabled(false);
        this.pinCodeInputField.setEnabled(true);
        this.settingsButton.setEnabled(false);
        PromptSupport.setPrompt("pin-code", pinCodeInputField);
    }

    private boolean unlock(String password) {
        if (password == null || settings.isPasswordValid(DigestUtils.md5Hex(password))) {
            this.lockButton.setEnabled(true);
            this.lockButton.setSelected(true);
            this.pinCodeInputField.setEnabled(false);
            this.settingsButton.setEnabled(true);
            PromptSupport.setPrompt("Click here to change password", pinCodeInputField);
            return true;
        }
        return false;
    }

    private ImageIcon getIcon(String iconName) {
        try {
            return new ImageIcon(ImageIO.read(LockingStatusbar.class.getResourceAsStream("/icons/" + iconName + ".png")));
        } catch (IOException ex) {
            log.fatal("could not load icon", ex);
            return null;
        }
    }

    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(mainFrame, settings.getSearchDirectories());
        dialog.setVisible(true);
        if (dialog.getStatus() == DialogStatus.CONFIRMED) {
            settings.setSearchDirectories(dialog.getSelectedDirectories());
            settings.backup(PathUtil.getSettingsFile());
            for (LockingListener lockingListener : listeners) {
                lockingListener.settingsChanged();
            }
        }
    }

    public void addLockingListener(LockingListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(LockingListener listener) {
        this.listeners.remove(listener);
    }
}
