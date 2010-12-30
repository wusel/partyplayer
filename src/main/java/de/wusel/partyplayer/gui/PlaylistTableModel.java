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

import de.wusel.partyplayer.library.Playlist;
import de.wusel.partyplayer.library.PlaylistListener;
import de.wusel.partyplayer.library.Song;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author wusel
 */
class PlaylistTableModel extends AbstractTableModel {

    private final Playlist playlist;
    private final PlaylistListener listener = new PlaylistListener() {

        @Override
        public void songAdded(Song song) {
            fireTableDataChanged();
        }

        @Override
        public void songOrderChanged(Song song) {
            fireTableDataChanged();
        }

        @Override
        public void songRemoved(Song song) {
            fireTableDataChanged();
        }
    };

    public PlaylistTableModel(Playlist playList) {
        this.playlist = playList;
        this.playlist.addListener(listener);
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Titel";
            case 1:
                return "Votes";
            default:
                throw new RuntimeException("unknown columnIndex");
        }
    }

    @Override
    public int getRowCount() {
        return playlist.getSongCount();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Song song = playlist.getSongs().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return song.getTitle();
            case 1:
                return playlist.getSongrequests(song);
            default:
                throw new RuntimeException("unknown columnIndex");
        }
    }
}
