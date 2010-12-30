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

import de.wusel.partyplayer.library.Library;
import de.wusel.partyplayer.util.PathUtil;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import org.apache.log4j.Logger;

/**
 *
 * @author wusel
 */
public class TagReader {

    private static final Logger log = Logger.getLogger(TagReader.class);

    public TrackInfo read(File file) throws IOException {
        if (Library.INSTANCE.containsSong(file.getAbsolutePath())) {
            return null;
        }
       
        ProcessBuilder processBuilder = new ProcessBuilder("./mutagen-inspect", file.getAbsolutePath());
        File workingDirectory = new File(PathUtil.getBinDirectory(), "/mutagen/");
        processBuilder.directory(workingDirectory);
        Process proccess = processBuilder.start();
        BufferedInputStream bis = new BufferedInputStream(proccess.getInputStream());
        StringBuilder builder = new StringBuilder();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = bis.read(buffer, 0, buffer.length)) != -1) {
            builder.append(new String(buffer, 0, len));
        }
        TrackInfo trackInfo = parseSong(builder.toString());
        trackInfo.setFile(file);
        proccess.destroy();
        return trackInfo;
    }

    private TrackInfo parseSong(String message) throws IOException {
        /**
         *"-- /home/wusel/Musik/Dendemann/Die Pfütze des Eisbergs (2006)/09 - LaLaLabernich.flac 
         * - FLAC, 216.49 seconds, 44100 Hz (audio/x-flac) 
         * artist=Dendemann 
         * album=Die Pfütze des Eisbergs 
         * title=LaLaLabernich 
         * date=2006 
         * genre=HipHop 
         * tracknumber=09 
         * cddb=a60c990f  " 
         */
        /**
         * "-- /home/wusel/defects/01 - Kings of Leon - Closer.mp3 
         * - MPEG 1 layer 3, 192000 bps, 44100 Hz, 239.60 seconds (audio/mp3) 
         * COMM=iTunNORM='eng'= 00001A1D 000019A8 00008F80 00008EF9 00026D05 000191D6 00008DA1 00008FA1 0002EBA7 0002F6E0
         * TPE1=Kings Of Leon 
         * TDRC=2008 
         * TIT2=Closer 
         * TENC=iTunes v7.7.1.11 
         * TRCK=1/11 
         * COMM=iTunPGAP='eng'=0 /  
         * TPOS=1/1 
         * TALB=Only By The Night 
         * COMM=iTunes_CDDB_IDs='eng'=11+065AC398D537B1DE2AEAEBC736DF11FF+11604504 
         * COMM=iTunSMPB='eng'= 00000000 00000210 00000AC8 0000000000A131A8 00000000 0057ACE6 00000000 00000000 00000000 00000000 00000000 00000000 
         * TCON=Alternative & Punk 
         * TCOM=Caleb Followill/Jared Followill/Matthew Followill/Nathan Followill"
         */
        TrackInfo info = new TrackInfo();
        BufferedReader reader = new BufferedReader(new StringReader(message));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("--")) {
            } else if (line.startsWith("-")) {
                if (line.contains("seconds")) {
                    String timeString = line.substring(0, line.indexOf("seconds"));
                    timeString = timeString.substring(timeString.lastIndexOf(",") + 2);
                    info.setDuration(timeString.trim());
                }
            } else if (isArtistTag(line) != -1) {
                info.setArtist(line.substring(isArtistTag(line)));
            } else if (isAlbumTag(line) != -1) {
                info.setAlbum(line.substring(isAlbumTag(line)));
            } else if (isTitleTag(line) != -1) {
                info.setTitle(line.substring(isTitleTag(line)));
            } else if (isDateTag(line) != -1) {
                info.setDate(line.substring(isDateTag(line)));
            } else if (isGenreTag(line) != -1) {
                info.setGenre(line.substring(isGenreTag(line)));
            } else if (isNumberTag(line) != -1) {
                info.setNumber(line.substring(isNumberTag(line)));
            } else if (isCDDBTag(line) != -1) {
                info.setCddb(line.substring(isCDDBTag(line)));
            }
        }
        reader.close();
        return info;
    }

    private int isArtistTag(String line) {
        String[] args = {"artist=", "TPE1="};
        for (String string : args) {
            if (line.startsWith(string)) {
                return string.length();
            }
        }
        return -1;
    }

    private int isAlbumTag(String line) {
        String[] args = {"album=", "TALB="};
        for (String string : args) {
            if (line.startsWith(string)) {
                return string.length();
            }
        }
        return -1;
    }

    private int isTitleTag(String line) {
        String[] args = {"title=", "TIT2="};
        for (String string : args) {
            if (line.startsWith(string)) {
                return string.length();
            }
        }
        return -1;
    }

    private int isDateTag(String line) {
        String[] args = {"date=", "TDRC="};
        for (String string : args) {
            if (line.startsWith(string)) {
                return string.length();
            }
        }
        return -1;
    }

    private int isGenreTag(String line) {
        String[] args = {"genre=", "TCON="};
        for (String string : args) {
            if (line.startsWith(string)) {
                return string.length();
            }
        }
        return -1;
    }

    private int isNumberTag(String line) {
        String[] args = {"tracknumber=", "TRCK="};
        for (String string : args) {
            if (line.startsWith(string)) {
                return string.length();
            }
        }
        return -1;
    }

    private int isCDDBTag(String line) {
        String[] args = {"cddb="};
        for (String string : args) {
            if (line.startsWith(string)) {
                return string.length();
            }
        }
        return -1;
    }
}
