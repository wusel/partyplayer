/*
 *  Copyright (C) 2011 wusel
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
package de.wusel.partyplayer.model;

import de.wusel.partyplayer.cli.TrackInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author wusel
 */
public class PlayerModel {

    private final Library library;
    private final Playlist playlist;
    private Map<Song, SongWrapper> wrapper = new LinkedHashMap<Song, SongWrapper>();

    public PlayerModel() {
        this.playlist = new Playlist();
        this.library = new Library();
    }

    public void removeSong(SongWrapper song) {
        this.library.removeSong(song);
    }

    public int getPlaylistSongCount() {
        return this.playlist.getSongCount();
    }

    public int getSongCount() {
        return this.library.getSongCount();
    }

    public List<SongWrapper> getAvailableSongs() {
        return new ArrayList<SongWrapper>(library.getSongs());
    }

    public SongWrapper getSongFromList(int rowIndex) {
        return library.getSong(rowIndex);
    }

    public SongWrapper getNextSong() {
        return playlist.next();
    }

    public void removeLibraryListener(LibraryListener listener) {
        library.removeListener(listener);
    }

    public void addLibraryListener(LibraryListener listener) {
        library.addListener(listener);
    }

    public void removePlaylistListener(PlaylistListener listener) {
        playlist.removeListener(listener);
    }

    public void addPlaylistListener(PlaylistListener listener) {
        playlist.addListener(listener);
    }

    public void importXML(File libraryFile) {
        this.library.importBackup(libraryFile);
    }

    public void exportXML(File libraryFile) {
        this.library.backup(libraryFile);
    }

    public void logUserFavorites() {
        this.playlist.logUserFavorites();
    }

    public List<SongWrapper> getPlaylistSongs() {
        return playlist.getSongs();
    }

    public void addToPlaylist(SongWrapper song, boolean user) {
        playlist.putSong(song, user);
    }

    public boolean containsSong(String absolutePath) {
        return this.library.containsSong(absolutePath);
    }

    public void addTrackInfo(TrackInfo trackInfo) {
        this.library.addTrackInfo(trackInfo);
    }

    public void addRandomSongToPlaylist() {
        int song = (int) (Math.random() * getSongCount());
        this.playlist.putSong(this.library.getSong(song), false);
    }
}
