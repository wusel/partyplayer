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

import de.wusel.partyplayer.cli.FileCallback;
import de.wusel.partyplayer.cli.FileSearcher;
import de.wusel.partyplayer.cli.Player;
import de.wusel.partyplayer.cli.PlayerListener;
import de.wusel.partyplayer.settings.Settings;
import de.wusel.partyplayer.cli.TagReader;
import de.wusel.partyplayer.cli.TrackInfo;
import de.wusel.partyplayer.gui.dialog.ChangePasswordDialog;
import de.wusel.partyplayer.gui.dialog.DialogStatus;
import de.wusel.partyplayer.gui.dialog.SettingsDialog;
import de.wusel.partyplayer.library.Library;
import de.wusel.partyplayer.library.LibraryListener;
import de.wusel.partyplayer.library.Playlist;
import de.wusel.partyplayer.library.PlaylistListener;
import de.wusel.partyplayer.library.Song;
import de.wusel.partyplayer.library.SongComparator;
import de.wusel.partyplayer.util.PathUtil;
import de.wusel.partyplayer.util.Util;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jdesktop.swingx.sort.TableSortController;

/**
 *
 * @author wusel
 */
public class PartyPlayer extends SingleFrameApplication {

    private static final Logger log = Logger.getLogger(PartyPlayer.class);
    private final Player player = new Player();
    private final Playlist playList = new Playlist();
    private PlayerPanel playerPanel;
    private JProgressBar fileReaderProgressBar;
    private JLabel statusLabel;
    private Timer timer;
    private final Settings settings = new Settings();
    private JToggleButton lockButton;
    private JButton settingsButton;
    private JPasswordField pinCodeInputField;
    private JXTable table;
    private boolean started = false;
    private long startTime;

    private void readBackupFile(File backupFile) {
        Library.INSTANCE.importBackup(backupFile);
    }

    private void readSettingsFile(File settingsFile) {
        this.settings.importXML(settingsFile);
    }

    private void checkBinaries() {
        if(!PathUtil.checkBinaries()) {
            System.exit(-1);
        }
    }

    private static enum MessageType {

        STARTED,
        STOPPED,
        CHANGED
    }

    @Override
    protected void startup() {
        startTime = System.currentTimeMillis();
        log.info("Application started @[" + startTime + "]");
        playList.addListener(playListListener);
        addExitListener(new ExitListener() {

            @Override
            public boolean canExit(EventObject eo) {
                if (settings.isPasswordValid(null) || lockButton.isSelected()) {
                    return true;
                } else {
                    final String showInputDialog = JOptionPane.showInputDialog(getMainFrame(), "PIN-Code eingeben!");
                    return showInputDialog != null && settings.isPasswordValid(DigestUtils.md5Hex(showInputDialog));
                }
            }

            @Override
            public void willExit(EventObject eo) {
                Library.INSTANCE.backup(PathUtil.getLibraryFile());
                settings.backup(PathUtil.getSettingsFile());
                playList.logUserFavorites();
                log.info("Application stopped @[" + System.currentTimeMillis() + "] running for [" + (System.currentTimeMillis() - startTime) / 1000 + "]seconds");
            }
        });

        FrameView mainView = getMainView();
        mainView.setComponent(createMainComponent());
        JXStatusBar bar = new JXStatusBar();
        statusLabel = new JLabel("Ready");
        fileReaderProgressBar = new JProgressBar(0, 0);

        pinCodeInputField = new JPasswordField();
        PromptSupport.setPrompt("pin-code", pinCodeInputField);
        PromptSupport.setForeground(Color.GRAY, pinCodeInputField);

        pinCodeInputField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!pinCodeInputField.isEnabled()) {
                    ChangePasswordDialog dialog = new ChangePasswordDialog(getMainFrame(), settings);
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
        bar.add(statusLabel, new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL));
        bar.add(fileReaderProgressBar, new JXStatusBar.Constraint(200));
        bar.add(pinCodeInputField, new JXStatusBar.Constraint(100));
        bar.add(lockButton, new JXStatusBar.Constraint());
        bar.add(settingsButton, new JXStatusBar.Constraint());
        mainView.setStatusBar(bar);
        show(mainView);
    }

    private ImageIcon getIcon(String iconName) {
        try {
            return new ImageIcon(ImageIO.read(PartyPlayer.class.getResourceAsStream("/icons/" + iconName + ".png")));
        } catch (IOException ex) {
            log.fatal("could not load icon", ex);
            return null;
        }
    }

    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(getMainFrame(), settings.getSearchDirectories());
        dialog.setVisible(true);
        if (dialog.getStatus() == DialogStatus.CONFIRMED) {
            settings.setSearchDirectories(dialog.getSelectedDirectories());
            settings.backup(PathUtil.getSettingsFile());
            timer.restart();

        }
        log.debug(dialog.getStatus());
    }

    private void lock() {
        this.lockButton.setEnabled(false);
        this.pinCodeInputField.setEnabled(true);
        this.settingsButton.setEnabled(false);
        this.playerPanel.lock();
        PromptSupport.setPrompt("pin-code", pinCodeInputField);
        getMainFrame().setAlwaysOnTop(true);
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(getMainFrame());
    }

    private boolean unlock(String password) {
        if (password == null || settings.isPasswordValid(DigestUtils.md5Hex(password))) {
            this.lockButton.setEnabled(true);
            this.lockButton.setSelected(true);
            this.pinCodeInputField.setEnabled(false);
            this.settingsButton.setEnabled(true);
            this.playerPanel.unlock();
            PromptSupport.setPrompt("Click here to change password", pinCodeInputField);
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
            getMainFrame().setAlwaysOnTop(false);
            getMainFrame().pack();
            getMainFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
            return true;
        }
        return false;
    }

    @Override
    protected void ready() {

        checkBinaries();
        Library.INSTANCE.addListener(new LibraryListener() {

            @Override
            public void songRemoved(Song song, int oldIndex) {
                if (playList.getSongrequests(song) != 0) {
                    playList.removeSong(song);
                }
            }
        });
        
        readSettingsFile(PathUtil.getSettingsFile());
        readBackupFile(PathUtil.getLibraryFile());

        if (settings.isPasswordValid(null)) {
            unlock(null);
        } else {
            lock();
        }
        player.addListener(playerListener);

        timer = new Timer(settings.getFolderCheckInterval(), new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!started) {
                    started = true;
                    setStatusbarMessage("[start] checking");
                    executeSongCheck();
                    exceuteFileCheck();
                    setStatusbarMessage("[finished] checking");
                    setStatusbarMessage("[" + Library.INSTANCE.getSongCount() + "] available");
                }
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private void executeSongCheck() {

        new SwingWorker<Void, Song>() {

            @Override
            protected Void doInBackground() throws Exception {
                final List<Song> songs = Library.INSTANCE.getSongs();
                for (Song song : songs) {
                    setStatusbarMessage("[" + song.getFileName() + "] check");
                    boolean exists = new File(song.getFileName()).exists();
                    boolean remove = exists && !belongsToSearchDirectories(song.getFileName());
                    if (remove) {
                        setStatusbarMessage("[" + song.getFileName() + "] removed");
                        publish(song);
                    }
                }
                return null;
            }

            private boolean belongsToSearchDirectories(String path) {
                for (File dirctory : settings.getSearchDirectories()) {
                    if (path.startsWith(dirctory.getAbsolutePath())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void process(List<Song> chunks) {
                for (Song song : chunks) {
                    Library.INSTANCE.removeSong(song);
                }
            }

            @Override
            protected void done() {
                setStatusbarMessage("[" + Library.INSTANCE.getSongCount() + "] available");
            }
        }.execute();
    }

    private void exceuteFileCheck() {
        new SwingWorker<Void, TrackInfo>() {

            private int maxFiles;
            private int currentFiles;

            @Override
            protected Void doInBackground() throws Exception {

                final ExecutorService service = Executors.newFixedThreadPool(10);
                FileSearcher searcher = new FileSearcher(settings);
                searcher.search(new FileCallback() {

                    @Override
                    public void fileFound(final File file) {
                        maxFiles++;
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                setStatusbarMessage("reading [" + currentFiles + "/" + maxFiles + "] files");
                                fileReaderProgressBar.setMaximum(maxFiles);
                            }
                        });
                        service.execute(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    publish(new TagReader().read(file));
                                    setStatusbarMessage("[" + Library.INSTANCE.getSongCount() + "] available");
                                } catch (IOException ex) {
                                    log.fatal("could not read file:", ex);
                                }
                            }
                        });
                    }
                });

                service.shutdown();
                service.awaitTermination(100, TimeUnit.MINUTES);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        fileReaderProgressBar.setValue(0);
                        fileReaderProgressBar.setMaximum(0);
                        setStatusbarMessage("[" + Library.INSTANCE.getSongCount() + "] available");
                    }
                });
                return null;
            }

            @Override
            protected void process(List<TrackInfo> chunks) {
                for (TrackInfo trackInfo : chunks) {
                    currentFiles++;
                    if (trackInfo != null) {
                        Library.INSTANCE.addTrackInfo(trackInfo);
                    }
                    fileReaderProgressBar.setValue(currentFiles);
                }
            }

            @Override
            protected void done() {
                started = false;
            }
        }.execute();
    }

    private void setStatusbarMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                statusLabel.setText(message);
            }
        });
    }

    private void play(final Song song) {
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
        mainPanel.add(new JLabel("Current"));
        mainPanel.add(new JSeparator(), "growx");
        mainPanel.add(new JLabel("Next"));
        mainPanel.add(new JSeparator(), "growx, wrap");
        mainPanel.add(createPlayerPanel(), "grow, span 2");
        mainPanel.add(createPlayListPanel(), "grow, hmax 100, span 2, wrap");
        mainPanel.add(new JLabel("Available"));
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
        mainPanel.add(new JLabel("Suche:"));
        mainPanel.add(searchField, "span, growx, wrap");
        mainPanel.add(createSongPanel(), "span, grow");
        return mainPanel;
    }

    private Component createPlayerPanel() {
        playerPanel = new PlayerPanel(player);
        return playerPanel;
    }

    private Component createPlayListPanel() {
        final PlaylistTableModel model = new PlaylistTableModel(playList);
        final JTable playlistTable = new JTable(model);
        playlistTable.getColumn("Votes").setMaxWidth(40);
        playlistTable.getColumn("Votes").setResizable(false);
        JScrollPane scrollPane = new JScrollPane(playlistTable);
        playlistTable.setFillsViewportHeight(true);
        return scrollPane;
    }

    private Component createSongPanel() {
        final SongsTableModel model = new SongsTableModel(Library.INSTANCE);

        table = new JXTable(model) {

            @Override
            public String getToolTipText(MouseEvent event) {
                int viewRowIndex = rowAtPoint(event.getPoint());
                if (viewRowIndex != -1) {
                    int modelIndex = convertRowIndexToModel(viewRowIndex);
                    Song songFromList = Library.INSTANCE.getSongFromList(modelIndex);
                    return songFromList.getFileName();
                }
                return super.getToolTipText(event);
            }
        };

        table.setAutoCreateRowSorter(true);
        table.getColumn("#").setMaxWidth(25);
        table.getColumn("#").setResizable(false);
        TableSortController sorter = (TableSortController) table.getRowSorter();
        sorter.setComparator(2, new SongComparator());
        table.getColumn("#").setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setText(((Song) value).getTrackNumber() + "");
                return label;
            }
        });

        table.getColumn("Dauer").setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setText(Util.getTimeString((Double) value));
                return label;
            }
        });

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSongToPlaylist(Library.INSTANCE.getSongFromList(table.convertRowIndexToModel(table.getSelectedRow())));
                }
            }
        });

        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addSongToPlaylist(Library.INSTANCE.getSongFromList(table.convertRowIndexToModel(table.getSelectedRow())));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        return scrollPane;
    }

    private void addSongToPlaylist(Song song) {
        this.playList.putSong(song, true);
    }
    
    private final PlaylistListener playListListener = new PlaylistListener() {

        @Override
        public void songAdded(Song song) {
            if (!player.isPlaying()) {
                play(playList.getNext());
            }
        }
    };

    private final PlayerListener playerListener = new PlayerListener() {

        @Override
        public void progessChanged(double percent) {
            if (playList.getSongCount() == 0 && percent > 90) {
                putRandomSongInPlaylist();
            }
        }

        private void putRandomSongInPlaylist() {
            int playListCount = Library.INSTANCE.getSongCount();
            int songNumber = (int) (Math.random() * playListCount);
            playList.putSong(Library.INSTANCE.getSongFromList(songNumber), false);
        }

        @Override
        public void songStoped(Song song) {
            Song next = playList.getNext();
            if (next == null) {
                putRandomSongInPlaylist();
                next = playList.getNext();
            }
            play(next);

        }
    };
}
