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

import de.wusel.partyplayer.cli.Settings;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author wusel
 */
public class ChangePasswordDialog extends JDialog {

    private static final Logger log = Logger.getLogger(SettingsDialog.class);
    private DialogStatus status = DialogStatus.CANCELED;
    private final Settings setting;

    private final JPasswordField oldPasswordField = new JPasswordField();
    private final JPasswordField newPasswordField = new JPasswordField();

    public ChangePasswordDialog(Window owner, Settings setting) {
        super(owner, "Change password ...", ModalityType.APPLICATION_MODAL);
        initUI();
        pack();
        setResizable(true);
        setLocationRelativeTo(owner);
        this.setting = setting;
        if(this.setting.isPasswordValid(null)) {
            this.oldPasswordField.setEnabled(false);
        }
    }

    private void initUI() {
        setLayout(new MigLayout("fill", "[] [grow] []"));

        add(new JLabel("Altes Passwort:"));
        add(oldPasswordField, "grow, span, wrap");
        add(new JLabel("Neues Passwort:"));
        add(newPasswordField, "grow, span, wrap");
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

    private void ok() {
        if(!oldPasswordField.isEnabled() || setting.isPasswordValid(DigestUtils.md5Hex(new String(oldPasswordField.getPassword())))){
            status = DialogStatus.CONFIRMED;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Das alte Passwort ist ungültig!");
        }
    }

    private void cancel() {
        status = DialogStatus.CANCELED;
        dispose();
    }

    public DialogStatus getStatus() {
        return status;
    }

    public String getPassDigest() {
        return DigestUtils.md5Hex(new String(this.newPasswordField.getPassword()));
    }
}
