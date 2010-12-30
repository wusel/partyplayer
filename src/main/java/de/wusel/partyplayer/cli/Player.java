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
package de.wusel.partyplayer.cli;

import de.wusel.partyplayer.library.Song;
import de.wusel.partyplayer.util.PathUtil;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author wusel
 */
public class Player {

    private static final Logger log = Logger.getLogger(Player.class);
    private final List<PlayerListener> listeners = new ArrayList<PlayerListener>();
    private Process playerProcess;
    private Song currentSong;

    public void play(Song song) throws IOException {
        this.currentSong = song;
        log.debug("playing now [" + song.getFileName() + "]");
        ProcessBuilder processbuilder = new ProcessBuilder(PathUtil.getMPlayerPath(), song.getFileName());
        playerProcess = processbuilder.start();
        for (PlayerListener playerListener : listeners) {
            playerListener.songStarted(song);
        }
        BufferedInputStream bis = new BufferedInputStream(playerProcess.getInputStream());
        byte[] buffer = new byte[1024];
        double max = 0;
        int len = 0;
        long lastOutput = System.currentTimeMillis();
        while ((len = bis.read(buffer, 0, buffer.length)) != -1) {
            String string = new String(buffer, 0, len);
            if (string.startsWith("A:") && System.currentTimeMillis() - lastOutput > 500) {
                String[] split = string.split("of");
                String first = split[0];
                first = first.substring(3, first.indexOf("(") - 1);
                double current = Double.parseDouble(first);

                if (max == 0) {
                    String last = split[1];
                    last = last.substring(1, last.indexOf("(") - 1);
                    max = Double.parseDouble(last);
                }

                for (PlayerListener playerListener : listeners) {
                    playerListener.progessChanged(current / (max / 100));
                }
                lastOutput = System.currentTimeMillis();
            }
        }
        stop();
    }

    public boolean isPlaying() {
        return playerProcess != null;
    }

    public void stop() {
        if (playerProcess != null) {
            log.debug("stopping");
            playerProcess.destroy();
            playerProcess = null;
            for (PlayerListener playerListener : listeners) {
                playerListener.songStoped(currentSong);
            }
            currentSong = null;

        }
    }

    public void addListener(PlayerListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(PlayerListener listener) {
        this.listeners.remove(listener);
    }
}
