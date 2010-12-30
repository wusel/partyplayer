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

    public void backup(File settingsFile) {
        try {
            XMLExportSupport exportSupport = new XMLExportSupport(settingsFile, "settings");
            exportSupport.startElement("searchDirectories");
            for (File file : searchDirectories) {
                exportSupport.addAttribute("path", file.getAbsolutePath());
                exportSupport.writeElement("file");
            }
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

    private void setExitPassword(String exitPass) {
        if (this.exitPassword == null && exitPass != null) {
            this.exitPassword = exitPass;
        }
    }

    public void setSearchDirectories(List<File> selectedDirectories) {
        this.searchDirectories.clear();
        this.searchDirectories.addAll(selectedDirectories);
    }

    private static final class XMLHandler extends DefaultHandler {

        private final List<File> files = new ArrayList<File>();
        private String exitPass;
        private final Settings settings;

        public XMLHandler(Settings settings) {
            this.settings = settings;
        }

        @Override
        public void endDocument() throws SAXException {
            for (File file : files) {
                settings.addSearchDirectory(file);
            }
            settings.setExitPassword(exitPass);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("file")) {
                this.files.add(new File(attributes.getValue("path")));
            } else if (qName.equals("exit")) {
                this.exitPass = attributes.getValue("exitPassword");
            }
        }
    }
}
