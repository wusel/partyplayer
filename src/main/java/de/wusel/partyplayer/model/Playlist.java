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
package de.wusel.partyplayer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author wusel
 */
class Playlist {

    private static final Logger log = Logger.getLogger(Playlist.class);
    private List<SongWrapper> currentSongs = new ArrayList<SongWrapper>();
    private final List<PlaylistListener> listeners = new ArrayList<PlaylistListener>();
    private final List<SongWrapper> allTimeSongs = new ArrayList<SongWrapper>();

    public Playlist() {
    }

    public void putSong(SongWrapper wrapper, boolean user) {
        if (currentSongs.contains(wrapper)) {
            log.debug("song contained will inc");
            wrapper.inc(user);
            Collections.sort(currentSongs, new SongWrapperComparator());
            for (PlaylistListener playlistListener : listeners) {
                playlistListener.songOrderChanged();
            }
        } else {
            currentSongs.add(wrapper);
            wrapper.inc(user);
            for (PlaylistListener playlistListener : listeners) {
                playlistListener.songAdded(wrapper);
            }
        }
    }

    public void removeSong(SongWrapper song) {
        song.reset();
        this.currentSongs.remove(song);
        for (PlaylistListener playlistListener : listeners) {
            playlistListener.songRemoved(song);
        }

    }

    public int getSongCount() {
        return this.currentSongs.size();
    }

    public SongWrapper next() {
        if (currentSongs.isEmpty()) {
            return null;
        } else {
            SongWrapper next = currentSongs.get(0);
            removeSong(next);
            return next;
        }
    }

    public List<SongWrapper> getSongs() {
        ArrayList<SongWrapper> result = new ArrayList<SongWrapper>();
        for (SongWrapper songWrapper : currentSongs) {
            result.add(songWrapper);
        }
        return result;
    }

    public void addListener(PlaylistListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(PlaylistListener listener) {
        this.listeners.remove(listener);
    }

    public void logUserFavorites() {
        log.info("-------  Userfavorites ------");
        for (SongWrapper songWrapper : allTimeSongs) {
            log.info("| " + songWrapper + " | " + songWrapper.getAllTimeRequestCount());
        }
    }

    private static class SongWrapperComparator implements Comparator<SongWrapper> {

        @Override
        public int compare(SongWrapper o1, SongWrapper o2) {
            if (o1.getCurrentRequestCount() == o2.getCurrentRequestCount()) {
                return (int) (o1.getAddTime() - o2.getAddTime());
            } else {
                return o2.getCurrentRequestCount() - o1.getCurrentRequestCount();
            }
        }
    }
}
