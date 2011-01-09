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

import de.wusel.partyplayer.library.Library;
import de.wusel.partyplayer.library.LibraryListener;
import de.wusel.partyplayer.library.Playlist;
import de.wusel.partyplayer.library.Song;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.application.Application;

/**
 *
 * @author wusel
 */
class SongsTableModel extends AbstractTableModel {
    private final Library library;
    private final Playlist playlist;
    private final Application application;

    public SongsTableModel(Library library, Playlist playlist, Application application) {
        this.library = library;
        this.library.addListener(new LibraryListener(){

            @Override
            public void songAdded(Song song, int index) {
                fireTableRowsInserted(index, index);
            }

            @Override
            public void songRemoved(Song song, int oldIndex) {
                fireTableRowsDeleted(oldIndex, oldIndex);
            }
        });
        this.playlist = playlist;
        this.application = application;
    }

    @Override
    public int getRowCount() {
        return library.getSongCount();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0: return Integer.class;
            case 1: return String.class;
            case 2: return String.class;
            case 3: return Song.class;
            case 4: return String.class;
            case 5: return Double.class;
            default: throw new RuntimeException("unknown columnIndex");
        }
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0: return application.getContext().getResourceMap().getString("table.songs.column.usable.label");
            case 1: return application.getContext().getResourceMap().getString("table.songs.column.artist.label");
            case 2: return application.getContext().getResourceMap().getString("table.songs.column.title.label");
            case 3: return application.getContext().getResourceMap().getString("table.songs.column.number.label");
            case 4: return application.getContext().getResourceMap().getString("table.songs.column.album.label");
            case 5: return application.getContext().getResourceMap().getString("table.songs.column.duration.label");
            default: throw new RuntimeException("unknown columnIndex");
        }
    }

    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Song song = library.getSongFromList(rowIndex);
        switch(columnIndex) {
            case 0: return playlist.getWaitTime(song);
            case 1: return song.getArtist().getName();
            case 2: return song.getTitle();
            case 3: return song;
            case 4: return song.getAlbum().getTitle();
            case 5: return song.getDuration();
            default: throw new RuntimeException("unknown columnIndex");
        }
    }


}
