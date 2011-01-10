/*
 * Copyright (C) 2010 wusel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.wusel.partyplayer.gui;

import de.wusel.partyplayer.cli.Player;
import de.wusel.partyplayer.cli.PlayerListener;
import de.wusel.partyplayer.settings.Settings;
import de.wusel.partyplayer.model.PlayerModel;
import de.wusel.partyplayer.model.PlaylistListener;
import de.wusel.partyplayer.model.SongComparator;
import de.wusel.partyplayer.model.SongWrapper;
import de.wusel.partyplayer.tasks.CheckAvailableSongsTask;
import de.wusel.partyplayer.tasks.CheckSearchDirectoryTask;
import de.wusel.partyplayer.util.PathUtil;
import de.wusel.partyplayer.util.Util;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventObject;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.sort.TableSortController;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 *
 * @author wusel
 */
public class PartyPlayer extends SingleFrameApplication {

    private static final Logger log = Logger.getLogger(PartyPlayer.class);
    private final Player player = new Player();
    private final Settings settings = new Settings();
    private final PlayerModel playerModel = new PlayerModel();
    private LockingStatusbar statusbar;
    private PlayerPanel playerPanel;
    private Timer timer;
    private JXTable table;
    private boolean started = false;
    private long startTime;
    private boolean unlocked = false;

    @Override
    protected void startup() {
        statusbar = new LockingStatusbar(this, getMainFrame(), settings);
        statusbar.addLockingListener(lockingListener);
        startTime = System.currentTimeMillis();
        log.info("Application started @[" + startTime + "]");
        player.addListener(playerListener);
        
        playerModel.addPlaylistListener(playListListener);

        addExitListener(new ExitListener() {

            @Override
            public boolean canExit(EventObject eo) {
                if (settings.isPasswordValid(null) || unlocked) {
                    return true;
                } else {
                    final String showInputDialog = JOptionPane.showInputDialog(getMainFrame(), getText("exit.requestPin.label"));
                    return showInputDialog != null && settings.isPasswordValid(DigestUtils.md5Hex(showInputDialog));
                }
            }

            @Override
            public void willExit(EventObject eo) {
                playerModel.exportXML(PathUtil.getLibraryFile());
                settings.backup(PathUtil.getSettingsFile());
                playerModel.logUserFavorites();
                log.info("Application stopped @[" + System.currentTimeMillis() + "] running for [" + (System.currentTimeMillis() - startTime) / 1000 + "]seconds");
            }
        });

        FrameView mainView = getMainView();
        mainView.setComponent(createMainComponent());
        mainView.setStatusBar(statusbar);
        show(mainView);
    }

    @Override
    protected void ready() {

        readSettingsFile(PathUtil.getSettingsFile());
        checkBinaries();
        this.statusbar.init();
        readBackupFile(PathUtil.getLibraryFile());

        timer = new Timer(settings.getFolderCheckInterval(), new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!started) {
                    started = true;
                    getContext().getTaskService().execute(new CheckAvailableSongsTask(getInstance(), playerModel, settings));
                    getContext().getTaskService().execute(new CheckSearchDirectoryTask(getInstance(), playerModel, settings));
                    started = false;
                }
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private void readBackupFile(File backupFile) {
        playerModel.importXML(backupFile);
    }

    private void readSettingsFile(File settingsFile) {
        this.settings.importXML(settingsFile);
    }

    private void checkBinaries() {
        if (!PathUtil.checkBinaries()) {
            System.exit(-1);
        }
    }

    private static enum MessageType {

        STARTED,
        STOPPED,
        CHANGED
    }

    private void play(final SongWrapper song) {
        SwingWorker worker = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                if (player.isPlaying()) {
                    return null;
                }
                player.play(song);

                return null;
            }
        };
        worker.execute();
    }

    private JComponent createMainComponent() {
        JPanel mainPanel = new JPanel(new MigLayout("fill", "[][50%][][50%]", "[] [] [] [] [grow]"));
        mainPanel.add(new JLabel(getText("layout.current.title")));
        mainPanel.add(new JSeparator(), "growx");
        mainPanel.add(new JLabel(getText("layout.next.title")));
        mainPanel.add(new JSeparator(), "growx, wrap");
        mainPanel.add(createPlayerPanel(), "grow, span 2");
        mainPanel.add(createPlayListPanel(), "grow, hmax 100, span 2, wrap");
        mainPanel.add(new JLabel(getText("layout.available.title")));
        mainPanel.add(new JSeparator(), "growx, span, wrap");
        final JTextField searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    searchField.setText(null);
                    table.setRowFilter(null);
                }
                try {
                    table.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(searchField.getText()), 0, 1, 3));
                } catch (PatternSyntaxException ex) {
                    table.setRowFilter(null);
                    //do nothing
                }
            }
        });
        mainPanel.add(new JLabel(getText("layout.search.label")));
        mainPanel.add(searchField, "span, growx, wrap");
        mainPanel.add(createSongPanel(), "span, grow");
        return mainPanel;
    }

    private Component createPlayerPanel() {
        playerPanel = new PlayerPanel(player);
        return playerPanel;
    }

    private Component createPlayListPanel() {
        final PlaylistTableModel model = new PlaylistTableModel(playerModel, this);
        final JTable playlistTable = new JTable(model);
        playlistTable.getColumn(getText("table.playlist.column.votes.label")).setMaxWidth(40);
        playlistTable.getColumn(getText("table.playlist.column.votes.label")).setResizable(false);
        JScrollPane scrollPane = new JScrollPane(playlistTable);
        playlistTable.setFillsViewportHeight(true);
        return scrollPane;
    }

    private Component createSongPanel() {
        final SongsTableModel model = new SongsTableModel(playerModel, settings, this);

        table = new JXTable(model) {

            @Override
            public String getToolTipText(MouseEvent event) {
                int viewRowIndex = rowAtPoint(event.getPoint());
                if (viewRowIndex != -1) {
                    int modelIndex = convertRowIndexToModel(viewRowIndex);
                    SongWrapper songFromList = playerModel.getSongFromList(modelIndex);
                    return songFromList.getFileName();
                }
                return super.getToolTipText(event);
            }
        };

        table.setAutoCreateRowSorter(true);
        String numberColumnName = getText("table.songs.column.number.label");
        table.getColumn(numberColumnName).setMaxWidth(25);
        table.getColumn(numberColumnName).setResizable(false);
        TableSortController sorter = (TableSortController) table.getRowSorter();
        sorter.setComparator(2, new SongComparator());

        table.getColumn(numberColumnName).setCellRenderer(new SubstanceDefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if(value !=null)
                    label.setText(((SongWrapper) value).getTrackNumber() + "");
                return label;
            }
        });

        table.getColumn(getText("table.songs.column.duration.label")).setCellRenderer(new SubstanceDefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if(value !=null)
                    label.setText(Util.getTimeString((Double) value));
                return label;
            }
        });

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSongToPlaylist(playerModel.getSongFromList(table.convertRowIndexToModel(table.getSelectedRow())));
                }
            }
        });

        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addSongToPlaylist(playerModel.getSongFromList(table.convertRowIndexToModel(table.getSelectedRow())));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        return scrollPane;
    }

    private void addSongToPlaylist(SongWrapper song) {
        if (song.getWaitTime(settings.getSongVoteThreshold()) == 0) {
            this.playerModel.addToPlaylist(song, true);
        } else {
            log.trace(song + " allready voted in last [" + settings.getSongVoteThreshold() / 1000 + "]s");
        }
    }

    private final PlaylistListener playListListener = new PlaylistListener() {

        @Override
        public void songAdded(SongWrapper song) {
            if (!player.isPlaying()) {
                play(playerModel.getNextSong());
            }
        }
    };

    private final PlayerListener playerListener = new PlayerListener() {

        @Override
        public void progessChanged(double percent) {
            if(playerModel.getPlaylistSongCount() == 0 && percent > 90) {
                playerModel.addRandomSongToPlaylist();
            }
        }

        @Override
        public void songStoped(SongWrapper song) {
            if(playerModel.getPlaylistSongCount() == 0) {
                playerModel.addRandomSongToPlaylist();
            }
            play(playerModel.getNextSong());
        }

    };
    private final LockingListener lockingListener = new LockingListener() {

        @Override
        public void lock() {
            unlocked = false;
            getMainFrame().setAlwaysOnTop(true);
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(getMainFrame());
        }

        @Override
        public void unlock() {
            unlocked = true;
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
            getMainFrame().setAlwaysOnTop(false);
            getMainFrame().pack();
            getMainFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        @Override
        public void settingsChanged() {
            timer.stop();
            timer.setInitialDelay(0);
            timer.start();
        }
    };

    private String getText(String key, Object... args) {
        return getContext().getResourceMap(PartyPlayer.class).getString(key, args);
    }
}
