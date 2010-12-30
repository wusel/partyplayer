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

import de.wusel.picotask.xml.XMLExportSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author wusel
 */
public class Album {

    public static Album UNKNOWN = new Album(0, "unknown", "");
    private final Set<Song> tracks = new TreeSet<Song>(new TrackNumberComparator());
    private final List<Artist> artists = new ArrayList<Artist>();
    private int year;
    private String title;
    private String cddb;

    Album(int year, String title, String cddb) {
        this.year = year;
        this.title = title;
        this.cddb = cddb;
    }

    public Set<Song> getSongs() {
        return Collections.unmodifiableSet(tracks);
    }

    public List<Artist> getArtists() {
        return Collections.unmodifiableList(artists);
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public int getNumberOfTracks() {
        return tracks.size();
    }

    void addSong(Song song) {
        this.tracks.add(song);
    }

    void removeSong(Song song) {
        this.tracks.remove(song);
    }

    void export(XMLExportSupport exportSupport, long currentID) {
        exportSupport.addAttribute("title", title);
        exportSupport.addAttribute("year", year);
        exportSupport.addAttribute("cddb", cddb);
        exportSupport.addAttribute("albumID", currentID);
        exportSupport.writeElement("Album");
    }

    void addArtist(Artist artist) {
        this.artists.add(artist);
    }

    boolean isMulti() {
        return this.artists.size() > 1;
    }

    private static final class TrackNumberComparator implements Comparator<Song> {

        @Override
        public int compare(Song o1, Song o2) {
            return o1.getTrackNumber() - o2.getTrackNumber();
        }
    }
}
