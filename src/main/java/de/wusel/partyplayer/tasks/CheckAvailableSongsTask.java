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
package de.wusel.partyplayer.tasks;

import de.wusel.partyplayer.model.PlayerModel;
import de.wusel.partyplayer.model.SongWrapper;
import de.wusel.partyplayer.settings.Settings;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

/**
 *
 * @author wusel
 */
public class CheckAvailableSongsTask extends Task<Void, SongWrapper> {

    private final PlayerModel playerModel;
    private final Settings settings;

    public CheckAvailableSongsTask(Application application, PlayerModel library, Settings settings) {
        super(application);
        this.playerModel = library;
        this.settings = settings;
    }

    @Override
    protected Void doInBackground() throws Exception {
        message("start", playerModel.getSongCount());
        final List<SongWrapper> songs = new ArrayList<SongWrapper>(playerModel.getAvailableSongs());
        for (SongWrapper song : songs) {
            boolean exists = new File(song.getFileName()).exists();
            boolean remove = exists && !belongsToSearchDirectories(song.getFileName());
            if (remove) {
                message("removed", song.getFileName());
                publish(song);
            }
        }
        message("finished", playerModel.getSongCount());
        return null;
    }

    @Override
    protected void process(List<SongWrapper> values) {
        for (SongWrapper song : values) {
            playerModel.removeSong(song);
        }
    }

    private boolean belongsToSearchDirectories(String path) {
        for (File dirctory : settings.getSearchDirectories()) {
            if (path.startsWith(dirctory.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }
}
