package com.elvinjhon.gigabeat;

import java.util.ArrayList;
import java.util.List;

public class TrackListHolder {
    private static List<Track> tracks = new ArrayList<>();

    public static void setTracks(List<Track> list) {
        tracks = new ArrayList<>(list);
    }

    public static List<Track> getTracks() {
        return tracks;
    }

    public static Track getTrack(int index) {
        if (index >= 0 && index < tracks.size()) return tracks.get(index);
        return null;
    }

    public static int size() {
        return tracks.size();
    }
}
