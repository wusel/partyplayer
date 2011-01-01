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
package de.wusel.partyplayer.settings;

import de.wusel.picotask.xml.XMLExportSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public class Settings {

    private static final Logger log = Logger.getLogger(Settings.class);
    private final List<File> searchDirectories = new ArrayList<File>();
    private String exitPassword;
    private int maxThreads = 1;
    private int folderCheckInterval = 30000;
    private int songPlayThreshold = 30000;
    private int songVoteThreshold = 30000;

    public void addSearchDirectory(File file) {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("file [" + file.getName() + "] is not a directory");
        }
        searchDirectories.add(file);
    }

    public List<File> getSearchDirectories() {
        return Collections.unmodifiableList(searchDirectories);
    }

    public boolean isPasswordValid(String check) {
        if (check == null) {
            return exitPassword == null;
        }
        if (exitPassword == null) {
            return check == null;
        }
        return check.equals(this.exitPassword);
    }

    public void setNewPassword(String newPassword) {
        this.exitPassword = newPassword;
    }

    private void setExitPassword(String exitPass) {
        if (this.exitPassword == null && exitPass != null) {
            this.exitPassword = exitPass;
        }
    }

    public int getFolderCheckInterval() {
        return folderCheckInterval;
    }

    public void setFolderCheckInterval(int folderCheckInterval) {
        if (folderCheckInterval < 0) {
            throw new IllegalArgumentException("should be positive");
        }

        this.folderCheckInterval = folderCheckInterval;
    }

    public int getSongPlayThreshold() {
        return songPlayThreshold;
    }

    public void setSongPlayThreshold(int songPlayThreshold) {
        if (songPlayThreshold < 0) {
            throw new IllegalArgumentException("should be positive");
        }
        this.songPlayThreshold = songPlayThreshold;
    }

    public int getSongVoteThreshold() {
        return songVoteThreshold;
    }

    public void setSongVoteThreshold(int songVoteThreshold) {
        if (songVoteThreshold < 0) {
            throw new IllegalArgumentException("should be positive");
        }
        this.songVoteThreshold = songVoteThreshold;
    }

    public void setSearchDirectories(List<File> selectedDirectories) {
        this.searchDirectories.clear();
        this.searchDirectories.addAll(selectedDirectories);
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            throw new IllegalArgumentException("thread count should be more than zero ...");
        }
        this.maxThreads = maxThreads;
    }

    public void backup(File settingsFile) {
        try {
            XMLExportSupport exportSupport = new XMLExportSupport(settingsFile, "settings");
            exportSupport.startElement("searchDirectories");
            for (File file : searchDirectories) {
                exportSupport.addAttribute("path", file.getAbsolutePath());
                exportSupport.writeElement("file");
            }

            exportSupport.endElement();
            exportSupport.startElement("configuration");
            exportSupport.writeElement("folderCheckInterval", folderCheckInterval);
            exportSupport.writeElement("maxThreads", maxThreads);
            exportSupport.writeElement("songVoteThreshold", songVoteThreshold);
            exportSupport.writeElement("songPlayThreshold", songPlayThreshold);
            exportSupport.endElement();

            exportSupport.startElement("security");
            if (exitPassword != null) {
                exportSupport.addAttribute("exitPassword", exitPassword);
                exportSupport.writeElement("exit");
            }
            exportSupport.endElement();
            exportSupport.endElement();
            exportSupport.closeFile();
        } catch (SAXException ex) {
            log.error(ex);
        } catch (IOException ex) {
            log.error(ex);
        }
    }

    public void importXML(File settingsFile) {
        try {
            final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser xmlReader = parserFactory.newSAXParser();
            xmlReader.parse(settingsFile, new XMLHandler(this));
        } catch (SAXException e) {
            log.fatal(e);
        } catch (IOException e) {
            log.fatal(e);
        } catch (ParserConfigurationException e) {
            log.fatal(e);
        }
    }

    private static final class XMLHandler extends DefaultHandler {

        private final List<File> files = new ArrayList<File>();
        private String exitPass;
        private String folderCheckInterval;
        private String maxThreads;
        private String songVoteThreshold;
        private String songPlayThreshold;
        private final Settings settings;
        private String currentElementName;

        public XMLHandler(Settings settings) {
            this.settings = settings;
        }

        @Override
        public void endDocument() throws SAXException {
            for (File file : files) {
                settings.addSearchDirectory(file);
            }
            settings.setExitPassword(exitPass);
            if (maxThreads != null) {
                settings.setMaxThreads(Integer.parseInt(maxThreads));
            }
            if (songVoteThreshold != null) {
                settings.setSongVoteThreshold(Integer.parseInt(songVoteThreshold));
            }
            if (songPlayThreshold != null) {
                settings.setSongPlayThreshold(Integer.parseInt(songPlayThreshold));
            }
            if (folderCheckInterval != null) {
                settings.setFolderCheckInterval(Integer.parseInt(folderCheckInterval));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            this.currentElementName = null;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if ("folderCheckInterval".equals(currentElementName)) {
                this.folderCheckInterval = new String(ch, start, length);
            } else if ("maxThreads".equals(currentElementName)) {
                this.maxThreads = new String(ch, start, length);
            } else if ("songVoteThreshold".equals(currentElementName)) {
                this.songVoteThreshold = new String(ch, start, length);
            } else if ("songPlayThreshold".equals(currentElementName)) {
                this.songPlayThreshold = new String(ch, start, length);
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("file")) {
                this.files.add(new File(attributes.getValue("path")));
            } else if (qName.equals("exit")) {
                this.exitPass = attributes.getValue("exitPassword");
            }
            this.currentElementName = qName;
        }
    }
}
