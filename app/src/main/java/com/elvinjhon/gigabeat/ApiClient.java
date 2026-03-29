package com.elvinjhon.gigabeat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {

    private static final String BASE_URL = "https://deezerdevs-deezer.p.rapidapi.com";
    private static final String API_KEY = "405f0583aemsh02aec90cff81358p1b9755jsna34b2aca8e61";
    private static final String API_HOST = "deezerdevs-deezer.p.rapidapi.com";

    public static List<Track> search(String query) throws Exception {
        String encoded = URLEncoder.encode(query, "UTF-8");
        URL url = new URL(BASE_URL + "/search?q=" + encoded);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-RapidAPI-Key", API_KEY);
        conn.setRequestProperty("X-RapidAPI-Host", API_HOST);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Server returned " + code);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        return parseTracks(sb.toString());
    }

    private static List<Track> parseTracks(String json) throws Exception {
        List<Track> list = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray data = root.optJSONArray("data");
        if (data == null) return list;

        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            String preview = obj.optString("preview", "");
            if (preview.isEmpty()) continue;

            JSONObject artist = obj.optJSONObject("artist");
            JSONObject album = obj.optJSONObject("album");

            list.add(new Track(
                    obj.optLong("id"),
                    obj.optString("title", "Unknown"),
                    obj.optInt("duration", 0),
                    preview,
                    artist != null ? artist.optString("name", "Unknown") : "Unknown",
                    album != null ? album.optString("title", "Unknown") : "Unknown",
                    album != null ? album.optString("cover_medium", "") : "",
                    album != null ? album.optString("cover_big", "") : ""
            ));
        }
        return list;
    }
}
