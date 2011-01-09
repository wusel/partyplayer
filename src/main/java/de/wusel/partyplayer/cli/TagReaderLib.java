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
package de.wusel.partyplayer.cli;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
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
import org.jaudiotagger.tag.TagField;

/**
 *
 * @author wusel
 */
public class TagReaderLib {

    private static final Logger log = Logger.getLogger(TagReaderLib.class);

    public static TrackInfo read(File file) throws IOException {
        try {
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            AudioHeader header = f.getAudioHeader();
            log.debug(tag.getFields(FieldKey.ALBUM_ARTIST));
        } catch (CannotReadException ex) {
            log.fatal(ex);
        } catch (TagException ex) {
            log.fatal(ex);
        } catch (ReadOnlyFileException ex) {
            log.fatal(ex);
        } catch (InvalidAudioFrameException ex) {
            log.fatal(ex);
        }
        throw new RuntimeException();
    }
}
