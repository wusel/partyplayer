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

import de.wusel.picotask.xml.XMLExportSupport;
import java.io.File;
import java.util.Map;

/**
 *
 * @author wusel
 */
class Song {
    private final Artist artist;
    private final Album album;
    private final String title;
    private final int trackNumber;
    private final String backupTitle;
    private final String fileName;
    private final String md5;
    private final double duration;
    
    public Song(Artist artist, Album album, String title, int trackNumber, double duration, File fileName, String md5) {
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.trackNumber = trackNumber;
        this.duration = duration;
        this.fileName = fileName.getAbsolutePath();
        this.backupTitle = fileName.getName();
        this.md5 = md5;
    }

    public Album getAlbum() {
        return album;
    }

    public Artist getArtist() {
        return artist;
    }

    public String getFileName() {
        return fileName;
    }

    public double getDuration() {
        return duration;
    }

    public String getMd5() {
        return md5;
    }

    public String getTitle() {
        return title != null ? title : backupTitle;
    }

    public int getTrackNumber() {
        return trackNumber;
    }
    
    public int getYear() {
        return this.album.getYear();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Song other = (Song) obj;
        if ((this.fileName == null) ? (other.fileName != null) : !this.fileName.equals(other.fileName)) {
            return false;
        }
        if ((this.md5 == null) ? (other.md5 != null) : !this.md5.equals(other.md5)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
        hash = 29 * hash + (this.md5 != null ? this.md5.hashCode() : 0);
        return hash;
    }

    void export(XMLExportSupport exportSupport, Map<Album, Long> albumIds, Map<Artist, Long> artistIds) {
        exportSupport.addAttribute("md5", md5);
        exportSupport.addAttribute("fileName", fileName);

        if(title != null)
            exportSupport.addAttribute("title", title);
        
        exportSupport.addAttribute("trackNumber", trackNumber);
        exportSupport.addAttribute("albumID", albumIds.get(album));
        exportSupport.addAttribute("artistID", artistIds.get(artist));
        exportSupport.addAttribute("duration", duration);
        exportSupport.writeElement("Song");
    }

}
