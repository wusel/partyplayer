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

import de.wusel.partyplayer.cli.TrackInfo;
import de.wusel.picotask.xml.XMLExportSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author wusel
 */
public class Library {

    private final Logger log = Logger.getLogger(Library.class);
    private final Map<String, Album> albumByName = new HashMap<String, Album>();
    private final Map<String, Song> songByFileName = new HashMap<String, Song>();
    private final Map<String, Artist> artistByName = new HashMap<String, Artist>();
    private final List<Song> songsAsList = new ArrayList<Song>();
    private final List<LibraryListener> listeners = new ArrayList<LibraryListener>();

    public synchronized void addTrackInfo(TrackInfo trackInfo) {
        if (songByFileName.containsKey(trackInfo.getFile().getAbsolutePath())) {
            log.trace("song [" + trackInfo + "] already contained in library");
            return;
        }
        Artist artist = artistByName.get(trackInfo.getArtist());
        if (artist == null) {
            artist = trackInfo.getArtist() != null ? new Artist(trackInfo.getArtist()) : Artist.UNKNOWN;
            artistByName.put(artist.getName(), artist);
        }

        Album album = albumByName.get(trackInfo.getAlbum());
        if (album == null) {
            int year = 0;
            try {
                year = Integer.parseInt(trackInfo.getDate());
            } catch (NumberFormatException ex) {
                log.trace("could not parse a date from [" + trackInfo + "]");
            }
            album = trackInfo.getAlbum() != null && !trackInfo.getAlbum().trim().equals("") ? new Album(year, trackInfo.getAlbum(), trackInfo.getCddb()) : Album.UNKNOWN;
            albumByName.put(trackInfo.getAlbum(), album);
        }
        artist.addAlbum(album);

        int trackNumber = 0;
        try {
            if (trackInfo.getNumber() != null && trackInfo.getNumber().contains("/")) {
                trackNumber = Integer.parseInt(trackInfo.getNumber().substring(0, trackInfo.getNumber().indexOf("/")));
            } else {
                trackNumber = Integer.parseInt(trackInfo.getNumber());
            }
        } catch (NumberFormatException ex) {
            log.trace("could not parse a trackNumber from [" + trackInfo + "]");

        }
        Song song = new Song(artist, album, trackInfo.getTitle(), trackNumber, Double.parseDouble(trackInfo.getDuration()), trackInfo.getFile(), trackInfo.getMd5());
        album.addSong(song);
        addSong(song);
    }
    
    private synchronized void addSong(Song song) {
        songByFileName.put(song.getFileName(), song);
        this.songsAsList.add(song);
        Collections.sort(this.songsAsList, new SongComparator());
        for (LibraryListener libraryListener : listeners) {
            libraryListener.songAdded(song, this.songsAsList.indexOf(song));
        }
    }

    public void removeSong(Song song) {
        Album songAlbum = song.getAlbum();
        Artist songArtist = song.getArtist();
        songByFileName.remove(song.getFileName());

        boolean albumRemoved = false;
        if (songAlbum.getNumberOfTracks() == 1) {
            this.albumByName.remove(songAlbum.getTitle());
            albumRemoved = true;
        }
        if (albumRemoved && songArtist.getAlbumByName().size() == 1) {
            songArtist.removeAlbum(songAlbum);
            this.artistByName.remove(songArtist.getName());
        }
        songAlbum.removeSong(song);
        int oldIndex = songsAsList.indexOf(song);
        for (LibraryListener libraryListener : listeners) {
            libraryListener.songRemoved(song, oldIndex);
        }
        songsAsList.remove(song);
    }

    public List<Album> getAlbums(String artistName) {
        if (this.artistByName.containsKey(artistName)) {
            return this.artistByName.get(artistName).getAlbumByYear();
        } else {
            return Collections.emptyList();
        }
    }

    public boolean containsSong(String fileName) {
        return this.songByFileName.containsKey(fileName);
    }

    public void addListener(LibraryListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(LibraryListener listener) {
        this.listeners.remove(listener);
    }

    public List<Song> getSongs() {
        return Collections.unmodifiableList(this.songsAsList);
    }

    public int getSongCount() {
        return this.songsAsList.size();
    }

    public Song getSongFromList(int rowIndex) {
        return (Song) this.songsAsList.get(rowIndex);
    }

    public void importBackup(File importFile) {
        try {
            final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser xmlReader = parserFactory.newSAXParser();
            xmlReader.parse(importFile, new XMLHandler());
        } catch (SAXException e) {
            log.fatal(e);
        } catch (IOException e) {
            log.fatal(e);
        } catch (ParserConfigurationException e) {
            log.fatal(e);
        }
    }

    private final class XMLHandler extends DefaultHandler {

        private Artist currentArtist;
        private Album currentAlbum;
        private Song currentSong;
        private Map<Long, Album> albumByTempID = new HashMap<Long, Album>();
        private Map<Artist, Set<Long>> albumIDsForArtist = new HashMap<Artist, Set<Long>>();
        private Map<Long, Artist> artistByTempID = new HashMap<Long, Artist>();

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("Artist")) {
                
                Set<Long> albums = albumIDsForArtist.get(currentArtist);
                for (Long albumID : albums) {
                    final Album album = albumByTempID.get(albumID);
                    currentArtist.addAlbum(album);
                }
                addArtist(currentArtist);
                currentArtist = null;
            } else if (qName.equals("Album") && currentAlbum != null) {
                addAlbum(currentAlbum);
                currentAlbum = null;
            } else if (qName.equals("Song")) {
                addSong(currentSong);
                currentSong = null;
            }
            super.endElement(uri, localName, qName);
        }
        private void addAlbum(Album album) {
            albumByName.put(album.getTitle(), album);
        }

        private void addArtist(Artist artist) {
            artistByName.put(artist.getName(), artist);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("Artist")) {
                long tmpID = Long.parseLong(attributes.getValue("artistID"));
                currentArtist = new Artist(attributes.getValue("name"));
                artistByTempID.put(tmpID, currentArtist);

            } else if (qName.equals("Album") && currentArtist != null) {
                Set<Long> albumIDs = albumIDsForArtist.get(currentArtist);
                if (albumIDs == null) {
                    albumIDs = new HashSet<Long>();
                }
                albumIDs.add(Long.parseLong(attributes.getValue("albumID")));
                albumIDsForArtist.put(currentArtist, albumIDs);

            } else if (qName.equals("Album") && currentArtist == null) {
                long tmpID = Long.parseLong(attributes.getValue("albumID"));
                int year = Integer.parseInt(attributes.getValue("year"));
                String title = attributes.getValue("title");
                String cddb = attributes.getValue("cddb");
                currentAlbum = new Album(year, title, cddb);
                albumByTempID.put(tmpID, currentAlbum);
            } else if (qName.equals("Song")) {
                long albumID = Long.parseLong(attributes.getValue("albumID"));
                long artistID = Long.parseLong(attributes.getValue("artistID"));
                String title = attributes.getValue("title");
                String fileName = attributes.getValue("fileName");
                int trackNumber = Integer.parseInt(attributes.getValue("trackNumber"));
                double duration = Double.parseDouble(attributes.getValue("duration"));
                String md5 = attributes.getValue("md5");
                final Album album = albumByTempID.get(albumID);
                currentSong = new Song(artistByTempID.get(artistID), album, title, trackNumber, duration, new File(fileName), md5);
                album.addSong(currentSong);
            }
        }
    }

    public void backup(File backupFile) {
        try {
            XMLExportSupport exportSupport = new XMLExportSupport(backupFile, "library");
            long currentID = 0;
            Map<Album, Long> albumIds = new HashMap<Album, Long>();
            Map<Artist, Long> artistIds = new HashMap<Artist, Long>();
            for (Album album : albumByName.values()) {
                album.export(exportSupport, currentID);
                albumIds.put(album, currentID);
                currentID++;

            }

            for (Artist artist : artistByName.values()) {
                artist.export(exportSupport, currentID, albumIds);
                artistIds.put(artist, currentID++);
            }
            for (Song song : songsAsList) {
                song.export(exportSupport, albumIds, artistIds);
            }
            exportSupport.endElement();
            exportSupport.closeFile();
        } catch (SAXException ex) {
            log.fatal(ex);
        } catch (IOException ex) {
            log.fatal(ex);
        }
    }
}
