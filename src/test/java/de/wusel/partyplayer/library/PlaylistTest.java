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

import java.io.File;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author wusel
 */
public class PlaylistTest extends TestCase {

    public PlaylistTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testEmptyPlaylist() {
        Playlist playlist = new Playlist();
        assertNull(playlist.next());
    }

    @Test
    public void testSingleElement() {
        Song song = createSong("testmd5");
        Playlist playlist = new Playlist();
        playlist.putSong(song, true);
        assertEquals(1, playlist.getSongCount());
        assertEquals(playlist.next(), song);
        assertEquals(0, playlist.getSongCount());
    }
    
    @Test
    public void testMultipleInserts() {
        Song song = createSong("testmd5");
        Playlist playlist = new Playlist();
        playlist.putSong(song, true);
        assertEquals(1, playlist.getSongCount());
        assertEquals(1, playlist.getSongrequests(song));
        playlist.putSong(song, true);
        assertEquals(2, playlist.getSongrequests(song));
        playlist.putSong(song, true);
        assertEquals(3, playlist.getSongrequests(song));
    }
    
    @Test
    public void testMultipleSongs() {
        Song song = createSong("testmd5");
        Playlist playlist = new Playlist();
        playlist.putSong(song, true);
        Song song2 = createSong("test2md5");
        playlist.putSong(song2, true);
        
        Song[] firstTestExpected = {song, song2};
        assertEquals(2, playlist.getSongCount());
        assertArrayEquals(firstTestExpected, playlist.getSongs().toArray(new Song[0]));
        playlist.putSong(song2, true);
        Song[] secondTestExpected = {song2, song};
        assertEquals(2, playlist.getSongCount());
        assertArrayEquals(secondTestExpected, playlist.getSongs().toArray(new Song[0]));
        playlist.putSong(song, true);
        playlist.putSong(song, true);
        assertEquals(2, playlist.getSongCount());
        assertArrayEquals(firstTestExpected, playlist.getSongs().toArray(new Song[0]));
        
    }

    private static void assertArrayEquals(Song[] expected, Song[] result) {
        assertEquals("arraylength differs", expected.length, result.length);
        for (int i = 0; i < result.length; i++) {
            Song song = result[i];
            assertEquals(expected[i], song);
        }
    }
    private static Song createSong(String md5) {
        return new Song(new Artist("Test"), new Album(2000, "TestAlbum", "100"), "Testsong", 1, 111d, new File("testFileName"), md5);
    }
}
