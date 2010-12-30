package de.wusel.partyplayer.gui;

import de.wusel.partyplayer.cli.Player;
import de.wusel.partyplayer.cli.PlayerListener;
import de.wusel.partyplayer.library.Song;
import de.wusel.partyplayer.util.Util;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;

final class PlayerPanel extends JPanel {

    private static final Logger log = Logger.getLogger(PlayerPanel.class);
    private static final Font titleFont = new Font("Dialog", Font.BOLD, 22);
    private static final Font albumFont = new Font("Monospaced", Font.BOLD, 13);
    private static final Font artistFont = new Font("Monospaced", Font.BOLD, 13);
    private final RichJLabel artistLabel = new RichJLabel("-");
    private final RichJLabel titleLabel = new RichJLabel("stopped");
    private final RichJLabel albumLabel = new RichJLabel("-");
    private final RichJLabel yearLabel = new RichJLabel("-");
    private final RichJLabel timeLabel = new RichJLabel("--:--/--:--");
    private final JProgressBar songProgress = new JProgressBar();
    private final JButton nextButton = new JButton(getIcon("control_fastforward_blue"));
    private Song song;
    private final Player player;
    private final PlayerListener listener = new PlayerListener() {

        @Override
        public void progessChanged(final double percent) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    songProgressChanged(percent);
                }
            });

        }

        @Override
        public void songStarted(final Song song) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setSongStarted(song);
                }
            });
        }

        @Override
        public void songStoped(Song song) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setSongStopped();
                }
            });
        }
    };

    public PlayerPanel(Player player) {
        setLayout(new MigLayout("fill, hidemode 2", "[grow][]", "[][][][]"));
        this.player = player;
        this.player.addListener(listener);
        initGUI();
    }

    private void initGUI() {
        this.nextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                player.stop();
            }
        });
        titleLabel.setFont(titleFont);
        albumLabel.setFont(albumFont);
        yearLabel.setFont(albumFont);
        artistLabel.setFont(artistFont);
        timeLabel.setFont(artistFont);
        add(titleLabel, "span, grow, wrap");
        add(albumLabel, "grow");
        add(yearLabel, " align right, wrap");
        add(artistLabel, "growx");
        add(timeLabel, "align right, wrap");
        add(songProgress, "split 2, growx, span 2");
        add(nextButton);
    }

    private ImageIcon getIcon(String iconName) {
        try {
            return new ImageIcon(ImageIO.read(PlayerPanel.class.getResourceAsStream("/icons/" + iconName + ".png")));
        } catch (IOException ex) {
            log.fatal("could not load icon", ex);
            return null;
        }
    }

    public void setSongStarted(Song song) {
        this.song = song;
        songProgress.setMaximum(100);
        songProgress.setValue(0);
        artistLabel.setText(song.getArtist().getName());
        titleLabel.setText(song.getTitle());
        albumLabel.setText(song.getAlbum().getTitle());
        yearLabel.setText(song.getYear() + "");
    }

    public void setSongStopped() {
        song = null;
        songProgress.setMaximum(100);
        songProgress.setValue(0);
        artistLabel.setText("-");
        titleLabel.setText("-");
        albumLabel.setText("-");
        yearLabel.setText("-");
        timeLabel.setText("--:--/--:--");
    }

    private void songProgressChanged(double percent) {
        songProgress.setValue((int) (percent));
        if (song != null) {
            double current = song.getDuration() / 100 * percent;
            timeLabel.setText(Util.getTimeString(current) + "/" + Util.getTimeString(song.getDuration()));
        }
    }

    void lock() {
        this.nextButton.setEnabled(false);
        this.nextButton.setVisible(false);
    }

    void unlock() {
        this.nextButton.setEnabled(true);
        this.nextButton.setVisible(true);
    }
}
