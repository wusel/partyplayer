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
import de.wusel.partyplayer.library.Song;
import de.wusel.partyplayer.util.Util;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author wusel
 */
class SongsTableModel extends AbstractTableModel {
    private final Library library;

    public SongsTableModel(Library library) {
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
    }

    @Override
    public int getRowCount() {
        return library.getSongCount();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0: return String.class;
            case 1: return String.class;
            case 2: return Song.class;
            case 3: return String.class;
            case 4: return Double.class;
            default: throw new RuntimeException("unknown columnIndex");
        }
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0: return "Künstler";
            case 1: return "Titel";
            case 2: return "#";
            case 3: return "Album";
            case 4: return "Dauer";
            default: throw new RuntimeException("unknown columnIndex");
        }
    }

    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Song song = library.getSongFromList(rowIndex);
        switch(columnIndex) {
            case 0: return song.getArtist().getName();
            case 1: return song.getTitle();
            case 2: return song;
            case 3: return song.getAlbum().getTitle();
            case 4: return song.getDuration();
            default: throw new RuntimeException("unknown columnIndex");
        }
    }


}
