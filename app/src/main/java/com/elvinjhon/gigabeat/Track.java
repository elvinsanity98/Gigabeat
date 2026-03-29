package com.elvinjhon.gigabeat;

import java.io.Serializable;

public class Track implements Serializable {
    private long id;
    private String title;
    private int duration;
    private String preview;
    private String artistName;
    private String albumTitle;
    private String coverMedium;
    private String coverBig;

    public Track(long id, String title, int duration, String preview,
                 String artistName, String albumTitle,
                 String coverMedium, String coverBig) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.preview = preview;
        this.artistName = artistName;
        this.albumTitle = albumTitle;
        this.coverMedium = coverMedium;
        this.coverBig = coverBig;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public int getDuration() { return duration; }
    public String getPreview() { return preview; }
    public String getArtistName() { return artistName; }
    public String getAlbumTitle() { return albumTitle; }
    public String getCoverMedium() { return coverMedium; }
    public String getCoverBig() { return coverBig; }

    public String getFormattedDuration() {
        int min = duration / 60;
        int sec = duration % 60;
        return String.format("%d:%02d", min, sec);
    }
}
