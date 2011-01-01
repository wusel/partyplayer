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

import de.wusel.partyplayer.settings.Settings;
import de.wusel.partyplayer.library.Library;
import de.wusel.partyplayer.library.LibraryListener;
import de.wusel.partyplayer.library.Song;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author wusel
 */
public class CLITest {

    private static final Logger log = Logger.getLogger(CLITest.class);

    public static void main(String[] args) throws ExecutionException, IOException {
        Library.INSTANCE.addListener(new LibraryListener() {

            @Override
            public void songAdded(Song song, int index) {
                log.debug("Song [" + song.getTitle() + "] added");
            }
        });
        long startTime = System.nanoTime();
//        executeSingleThread();
        long endTime = System.nanoTime();
//        log.debug("singleThread needed: [" + ((double) endTime - startTime) / 1000 / 1000 / 1000 + "s]");

        startTime = System.nanoTime();
        executeMultiThread();
        endTime = System.nanoTime();
        log.debug("multiThread needed: [" + ((double) endTime - startTime) / 1000 / 1000 / 1000 + "s]");
//        Set<Song> songs = Library.INSTANCE.getAlbums("Dendemann").get(0).getSongs();
        
//        new Player().play(songs.iterator().next());
//        startTime = System.nanoTime();
//        executeMultiThread();
//        endTime = System.nanoTime();
//        log.debug("multiThread needed: [" + ((double) endTime - startTime) / 1000 / 1000 / 1000 + "s]");
    }

    private static void executeSingleThread() {
        Settings settings = new Settings();
        settings.addSearchDirectory(new File("/home/wusel/testMusik1"));
        FileSearcher searcher = new FileSearcher(settings);
        searcher.search(new FileCallback()      {

            public void fileFound(File file) {
                try {
                    log.debug(new TagReader().read(file));
                } catch (IOException ex) {
                    log.error(ex);
                }
            }
        });
    }

    private static void executeMultiThread() throws ExecutionException {
        final ExecutorService service = Executors.newCachedThreadPool();
        Settings settings = new Settings();
        settings.addSearchDirectory(new File("/home/wusel/testMusik1"));
        FileSearcher searcher = new FileSearcher(settings);
        final List<Future<TrackInfo>> futures = new ArrayList<Future<TrackInfo>>();

        searcher.search(new FileCallback()  {

            public void fileFound(final File file) {
                futures.add(service.submit(new Callable<TrackInfo>()   {

                    public TrackInfo call() throws Exception {
                        return new TagReader().read(file);
                    }
                }));
            }
        });
        service.shutdown();
        try {
            service.awaitTermination(1000, TimeUnit.DAYS);
            for (Future<TrackInfo> future : futures) {
                TrackInfo result = future.get();
                if (result != null)
                    Library.INSTANCE.addTrackInfo(result);
            }
        } catch (InterruptedException ex) {
            log.error(ex);
        }

    }
}
