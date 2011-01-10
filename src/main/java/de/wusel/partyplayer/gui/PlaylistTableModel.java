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

import de.wusel.partyplayer.model.PlayerModel;
import de.wusel.partyplayer.model.PlaylistListener;
import de.wusel.partyplayer.model.SongWrapper;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.application.Application;

/**
 *
 * @author wusel
 */
class PlaylistTableModel extends AbstractTableModel {

    private final PlayerModel playerModel;
    private final PlaylistListener listener = new PlaylistListener() {

        @Override
        public void songAdded(SongWrapper song) {
            fireTableDataChanged();
        }

        @Override
        public void songOrderChanged() {
            fireTableDataChanged();
        }

        @Override
        public void songRemoved(SongWrapper song) {
            fireTableDataChanged();
        }
    };
    private final Application application;

    public PlaylistTableModel(PlayerModel playerModel, Application application) {
        this.playerModel = playerModel;
        this.playerModel.addPlaylistListener(listener);
        this.application = application;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return application.getContext().getResourceMap().getString("table.playlist.column.title.label");
            case 1:
                return application.getContext().getResourceMap().getString("table.playlist.column.votes.label");
            default:
                throw new RuntimeException("unknown columnIndex");
        }
    }

    @Override
    public int getRowCount() {
        return playerModel.getPlaylistSongCount();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SongWrapper song = playerModel.getPlaylistSongs().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return song.getTitle();
            case 1:
                return song.getCurrentRequestCount();
            default:
                throw new RuntimeException("unknown columnIndex");
        }
    }
}
