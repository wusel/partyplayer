/*
 *  Copyright (C) 2010 wusel
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
package de.wusel.partyplayer.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 *
 * @author wusel
 */
public class PathUtil {

    private static final Logger log = Logger.getLogger(PathUtil.class);
    private static File binDirectory;
    private static File jarDirectory;
    private static File backupFile;
    private static File settingsFile;

    public synchronized static File getBinDirectory() {
        if (binDirectory == null) {
            try {
                File jarFile = new File(PathUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                log.trace("jarFile: " + jarFile.getAbsolutePath());
                if (jarFile.isDirectory()) {
                    binDirectory = new File("bin/");
                } else {
                    jarDirectory = jarFile.getParentFile();
                    binDirectory = new File(jarDirectory, "bin/");
                }
            } catch (URISyntaxException ex) {
                log.fatal(ex);
                binDirectory = new File("bin/");
            }
        }
        return binDirectory;
    }

    public static boolean checkBinaries() {
        boolean result = false;
        try {
            ProcessBuilder mplayerBuilder = new ProcessBuilder(getMPlayerPath());
            Process mplayer = mplayerBuilder.start();
            mplayer.destroy();
            result = true;
        } catch (IOException ex) {
            log.error("could not start mplayer", ex);
            JOptionPane.showMessageDialog(null, "Bitte stellen Sie sicher das mplayer auf ihrem System verfügbar ist!");
            result = false;
        }
        return result;
    }

    public static String getMPlayerPath() {
        String osName = System.getProperty("os.name");
        log.trace("reading os.name [" + osName + "]");
        if (osName.equalsIgnoreCase("linux")) {
            return "mplayer";
        } else if (osName.toLowerCase().startsWith("windows")) {
            return getBinDirectory().getAbsolutePath() + File.separatorChar + "mplayer" + File.separatorChar + "mplayer.exe";
        } else {
            throw new RuntimeException("unknown os.name [" + osName + "]");
        }
    }

    public static File getSettingsFile() {
        if (jarDirectory == null) {
            getBinDirectory();
        }
        if (jarDirectory == null) {
            return new File("partyPlayer.settings");
        } else if (settingsFile == null) {
            settingsFile = new File(jarDirectory, "partyPlayer.settings");
        }

        return settingsFile;
    }

    public static File getLibraryFile() {
        if (jarDirectory == null) {
            getBinDirectory();
        }
        if (jarDirectory == null) {
            return new File("partyPlayer.lib");
        } else if (backupFile == null) {
            backupFile = new File(jarDirectory, "partyPlayer.lib");
        }

        return backupFile;
    }
}
