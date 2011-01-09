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
package de.wusel.partyplayer.gui;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.logging.LogManager;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;

/**
 *
 * @author wusel
 */
public class Main {

    public static void main(final String[] args) throws IOException {
        //Disable JAudioTagger logging
        LogManager.getLogManager().readConfiguration(new StringBufferInputStream("log4j.logger.com.foo=WARN"));
        
        SwingUtilities.invokeLater(new Runnable()  {
            @Override
            public void run() {
                Application.launch(PartyPlayer.class, args);
            }
        });
    }
}
