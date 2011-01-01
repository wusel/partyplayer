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
package de.wusel.partyplayer.cli;

import de.wusel.partyplayer.settings.Settings;
import java.io.File;
import java.util.List;

/**
 *
 * @author wusel
 */
public class FileSearcher {

    private static final String[] supportedExtensions = {"mp3", "flac", "ogg"};
    private final Settings settings;

    public FileSearcher(Settings settings) {
        this.settings = settings;
    }

    public void search(FileCallback callback) {
        List<File> searchDirectories = this.settings.getSearchDirectories();
        for (File file : searchDirectories) {
            traverseDirectory(file, callback);
        }
    }

    private void traverseDirectory(File file, FileCallback callback) {
        File[] directoryFiles = file.listFiles();
        for (File child : directoryFiles) {
            if (child.isDirectory()) {
                traverseDirectory(child, callback);
            } else if (isSupported(child.getName())) {
                callback.fileFound(child);
            }
        }
    }

    private boolean isSupported(String fileName) {
        for (String string : supportedExtensions) {
            if (fileName.toLowerCase().endsWith(string))
                return true;
        }
        return false;
    }
}
