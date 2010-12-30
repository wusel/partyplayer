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

/**
 *
 * @author wusel
 */
class SongWrapper {
    private final long addTime;
    private final Song song;
    private int count = 1;

    public SongWrapper(long addTime, Song song) {
        this.addTime = addTime;
        this.song = song;
    }
    
    public void inc() {
        count ++;
    }
    
    public int getRequestCount() {
        return count;
    }

    long getAddTime() {
        return addTime;
    }

    
    Song getSong() {
        return song;
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
