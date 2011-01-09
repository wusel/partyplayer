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

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

/**
 *
 * @author wusel
 */
public class TagReader {

    private static final Logger log = Logger.getLogger(TagReader.class);

    public static TrackInfo read(File file) throws IOException {
//        ProcessBuilder processBuilder = new ProcessBuilder("./mutagen-inspect", file.getAbsolutePath());
//        File workingDirectory = new File(PathUtil.getBinDirectory(), "/mutagen/");
//        processBuilder.directory(workingDirectory);
//        Process proccess = processBuilder.start();
//        BufferedInputStream bis = new BufferedInputStream(proccess.getInputStream());
//        StringBuilder builder = new StringBuilder();
//        byte[] buffer = new byte[1024];
//        int len = 0;
//        while ((len = bis.read(buffer, 0, buffer.length)) != -1) {
//            builder.append(new String(buffer, 0, len));
//        }
//        TrackInfo trackInfo = parseSong(builder.toString());
//        trackInfo.setFile(file);
//        proccess.destroy();
//        return trackInfo;
        try {
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            AudioHeader header = f.getAudioHeader();
            TrackInfo info = new TrackInfo();
            info.setFileType(header.getFormat());
            info.setDuration(""+header.getTrackLength());
            if (tag != null) {
                info.setArtist(getValueOrNull(tag.getFirst(FieldKey.ARTIST)));
                info.setAlbum(getValueOrNull(tag.getFirst(FieldKey.ALBUM)));
                info.setDate(getValueOrNull(tag.getFirst(FieldKey.YEAR)));
                info.setNumber(getValueOrNull(tag.getFirst(FieldKey.TRACK)));
                info.setTitle(getValueOrNull(tag.getFirst(FieldKey.TITLE)));
                info.setGenre(getValueOrNull(tag.getFirst(FieldKey.GENRE)));
            } else {
                log.debug("tag == null");
            }
            info.setFile(file);
            return info;
        } catch (CannotReadException ex) {
            log.fatal(ex);
        } catch (TagException ex) {
            log.fatal(ex);
        } catch (ReadOnlyFileException ex) {
            log.fatal(ex);
        } catch (InvalidAudioFrameException ex) {
            log.fatal(ex);
        }
        return null;
    }

    private static String getValueOrNull(String input) {
        if(input == null) return null;
        if(input.trim().isEmpty()) return null;
        else return input.trim();
    }
}
