package de.wusel.partyplayer.library;

import java.util.Comparator;

public class SongComparator implements Comparator<Song> {

    @Override
    public int compare(Song o1, Song o2) {
        if (o1.getArtist().equals(o2.getArtist()) || o1.getAlbum().isMulti() && o2.getAlbum().isMulti()) {
            if (o1.getAlbum().equals(o2.getAlbum())) {
                return o1.getTrackNumber() - o2.getTrackNumber();
            } else {
                return o1.getAlbum().getTitle().compareTo(o2.getAlbum().getTitle());
            }
        } else {
            return o1.getArtist().getName().compareTo(o2.getArtist().getName());
        }
    }
}
