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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author wusel
 */
public class Artist {
    public static Artist UNKNOWN = new Artist("unknown");
    private final String name;
    private final Set<Album> albums = new HashSet<Album>();

    public Artist(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public void addAlbum(Album album) {
        this.albums.add(album);
        album.addArtist(this);
    }
    
    public List<Album> getAlbumByYear() {
        List<Album> result = new ArrayList<Album>(albums);
        Collections.sort(result, new YearComparator());
        return result; 
    }
    
    public List<Album> getAlbumByName() {
        List<Album> result = new ArrayList<Album>(albums);
        Collections.sort(result, new NameComparator());
        return result; 
    }

    void removeAlbum(Album songAlbum) {
        this.albums.remove(songAlbum);
    }

    void export(XMLExportSupport exportSupport, long artistID, Map<Album, Long> albumIds) {
        exportSupport.addAttribute("name", name);
        exportSupport.addAttribute("artistID", artistID);
        exportSupport.startElement("Artist");
        exportSupport.startElement("Albums");
        for (Album album : albums) {
            exportSupport.addAttribute("albumID", albumIds.get(album));
            exportSupport.startElement("Album");
            exportSupport.endElement();
        }
        exportSupport.endElement();
        exportSupport.endElement();
    }
}
