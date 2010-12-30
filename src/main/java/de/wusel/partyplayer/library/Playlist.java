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
package de.wusel.partyplayer.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author wusel
 */
public class Playlist {

    private static final Logger log = Logger.getLogger(Playlist.class);
    private List<SongWrapper> currentSongs = new ArrayList<SongWrapper>();
    private final List<PlaylistListener> listeners = new ArrayList<PlaylistListener>();
    private final Map<Song, Long> allTimeRequests = new HashMap<Song, Long>();

    public void putSong(Song song, boolean user) {
        SongWrapper wrapper = new SongWrapper(System.currentTimeMillis(), song);
        if (currentSongs.contains(wrapper)) {
            log.debug("song contained will inc");

            if (user) {
                allTimeRequests.put(song, allTimeRequests.get(song) + 1);
            }
            currentSongs.get(currentSongs.indexOf(wrapper)).inc();

            Collections.sort(currentSongs, new SongWrapperComparator());

            for (PlaylistListener playlistListener : listeners) {
                playlistListener.songOrderChanged(song);
            }
        } else {
            currentSongs.add(wrapper);
            if (user) {
                if (!allTimeRequests.containsKey(song)) {
                    allTimeRequests.put(song, 1L);
                } else {
                    allTimeRequests.put(song, allTimeRequests.get(song) + 1);
                }
            }
            for (PlaylistListener playlistListener : listeners) {
                playlistListener.songAdded(song);
            }
        }
    }

    public void removeSong(Song song) {
        this.currentSongs.remove(new SongWrapper(System.currentTimeMillis(), song));
        for (PlaylistListener playlistListener : listeners) {
            playlistListener.songRemoved(song);
        }

    }

    public int getSongCount() {
        return this.currentSongs.size();
    }

    public int getSongrequests(Song song) {
        SongWrapper wrapper = new SongWrapper(System.currentTimeMillis(), song);
        if (currentSongs.contains(wrapper)) {
            return currentSongs.get(currentSongs.indexOf(wrapper)).getRequestCount();
        } else {
            return 0;
        }
    }

    public Song getNext() {
        if (currentSongs.isEmpty()) {
            return null;
        } else {
            SongWrapper next = currentSongs.iterator().next();
            for (PlaylistListener playlistListener : listeners) {
                playlistListener.songRemoved(next.getSong());
            }
            this.currentSongs.remove(next);
            return next.getSong();
        }
    }

    public List<Song> getSongs() {
        ArrayList<Song> result = new ArrayList<Song>();
        for (SongWrapper songWrapper : currentSongs) {
            result.add(songWrapper.getSong());
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
        for (Map.Entry<Song, Long> entry : allTimeRequests.entrySet()) {
            log.info("| " + entry.getValue() + " | " + entry.getKey().getTitle());
        }
    }

    private static class SongWrapperComparator implements Comparator<SongWrapper> {

        @Override
        public int compare(SongWrapper o1, SongWrapper o2) {
            if (o1.getRequestCount() == o2.getRequestCount()) {
                return (int) (o1.getAddTime() - o2.getAddTime());
            } else {
                return o2.getRequestCount() - o1.getRequestCount();
            }
        }
    }
}
