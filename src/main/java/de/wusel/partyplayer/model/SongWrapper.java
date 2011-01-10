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

/**
 *
 * @author wusel
 */
public class SongWrapper {

    private final Song song;
    private int currentSongRequests = 0;
    private int allRequests = 0;
    private long addTime;
    private long lastPlaytime = 0;
    private long lastRequestTime;

    SongWrapper(Song song) {
        this.song = song;
    }

    public void inc(boolean user) {
        currentSongRequests++;
        long currentTimeMillis = System.currentTimeMillis();
        lastRequestTime = currentTimeMillis;
        if(addTime == 0)
            addTime = currentTimeMillis;
        if (user) {
            allRequests++;
        }
    }

    public long getLastPlaytime() {
        return lastPlaytime;
    }

    public long getLastRequestTime() {
        return lastRequestTime;
    }

    public int getAllTimeRequestCount() {
        return this.allRequests;
    }

    public int getCurrentRequestCount() {
        return currentSongRequests;
    }

    void reset() {
        this.currentSongRequests = 0;
        this.addTime = 0;
        this.lastPlaytime = 0;
        this.lastRequestTime = 0;
    }

    long getAddTime() {
        return addTime;
    }

    Song getSong() {
        return song;
    }

    public int getYear() {
        return song.getYear();
    }

    public int getTrackNumber() {
        return song.getTrackNumber();
    }

    public String getTitle() {
        return song.getTitle();
    }

    public String getMd5() {
        return song.getMd5();
    }

    public String getFileName() {
        return song.getFileName();
    }

    public double getDuration() {
        return song.getDuration();
    }

    public Artist getArtist() {
        return song.getArtist();
    }

    public Album getAlbum() {
        return song.getAlbum();
    }

    public long getWaitTime(long voteThreshold) {
        long diff = System.currentTimeMillis() - this.lastRequestTime;
        if (diff >= voteThreshold) {
            return 0;
        } else {
            return voteThreshold - diff;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SongWrapper other = (SongWrapper) obj;
        if (this.song != other.song && (this.song == null || !this.song.equals(other.song))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.song != null ? this.song.hashCode() : 0);
        return hash;
    }
}
